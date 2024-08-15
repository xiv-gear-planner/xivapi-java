package gg.xp.xivapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.clienttypes.XivApiSchemaVersion;
import gg.xp.xivapi.clienttypes.XivApiSettings;
import gg.xp.xivapi.filters.SearchFilter;
import gg.xp.xivapi.mappers.QueryField;
import gg.xp.xivapi.mappers.objects.ObjectFieldMapper;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.util.MappingUtils;
import gg.xp.xivapi.pagination.ListOptions;
import gg.xp.xivapi.pagination.XivApiListPaginator;
import gg.xp.xivapi.pagination.XivApiPaginator;
import gg.xp.xivapi.pagination.XivApiSearchPaginator;
import org.apache.hc.core5.net.URIBuilder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.Semaphore;

public class XivApiClient implements AutoCloseable {
	private static final Logger log = LoggerFactory.getLogger(XivApiClient.class);

	private final ObjectMapper mapper = new ObjectMapper();
	private final XivApiSettings settings;
	private final HttpClient client;
	private final @Nullable Semaphore limiter;

	public XivApiClient(XivApiSettings settings) {
		this.settings = settings;
		// TODO: replace with apache http client
		this.client = HttpClient.newBuilder().build();
		int limit = settings.getConcurrencyLimit();
		if (limit > 0) {
			this.limiter = new Semaphore(limit);
		}
		else {
			this.limiter = null;
		}
	}

	public XivApiClient() {
		this(XivApiSettings.newBuilder().build());
	}

	public XivApiSettings getSettings() {
		return settings;
	}

	public URI getBaseUri() {
		return settings.getBaseUri();
	}

	public JsonNode sendGET(String urlPart) {
		return sendGET(constructUri(urlPart));
	}

	public URI constructUri(String urlPart) {
		// Strip leading slashes
		urlPart = urlPart.replaceFirst("^/*", "");
		final URI uri;
		try {
			uri = new URI("%s/%s".formatted(settings.getBaseUri(), urlPart));
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		return uri;
	}

	public JsonNode sendGET(URI uri) {

		log.info("GET URI: {}", uri);

		HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
		String response;
		JsonNode root;
		try {
			if (limiter != null) {
				limiter.acquire();
			}
			response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
			root = this.mapper.readTree(response);
		}
		catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (limiter != null) {
				limiter.release();
			}
		}
		return root;
	}

	public <X extends XivApiObject> X getById(Class<X> cls, int id) {

		String sheetName = MappingUtils.validateAndGetSheetName(cls);

		ObjectFieldMapper<X> mapping = new ObjectFieldMapper<>(cls, mapper);

		List<QueryField> fields = mapping.getQueryFields();

		URI uri;
		try {
			uri = new URIBuilder(getBaseUri())
					.appendPath("sheet")
					.appendPath(sheetName)
					.appendPath(String.valueOf(id))
					.addParameters(MappingUtils.formatQueryFields(fields))
					.build();
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		JsonNode root = sendGET(uri);

		XivApiSchemaVersion sv = new XivApiSchemaVersion() {
			@Override
			public String fullVersionString() {
				return root.get("schema").textValue();
			}
		};

		XivApiContext context = new XivApiContext(root, settings, sv);

		return mapping.getValue(root, context);
	}

	private <X> ListOptions<X> defaultListOptions() {
		return ListOptions.<X>newBuilder().build();
	}

	public <X extends XivApiObject> XivApiPaginator<X> getListIterator(Class<X> cls) {
		return getListIterator(cls, defaultListOptions());
	}

	public <X extends XivApiObject> XivApiPaginator<X> getListIterator(Class<X> cls, ListOptions<? super X> options) {
		String sheetName = MappingUtils.validateAndGetSheetName(cls);

		ObjectFieldMapper<X> mapping = new ObjectFieldMapper<>(cls, mapper);

		List<QueryField> fields = mapping.getQueryFields();

		int perPage = options.getPerPage();

		URI firstPageUri;
		try {
			firstPageUri = new URIBuilder(getBaseUri())
					.appendPath("sheet")
					.appendPath(sheetName)
					.addParameters(MappingUtils.formatQueryFields(fields))
					.setParameter("limit", String.valueOf(perPage))
					.build();
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		JsonNode firstPage = sendGET(firstPageUri);

		return new XivApiListPaginator<>(this, firstPage, firstPageUri, options::shouldStop, mapping, 100);
	}

	public <X extends XivApiObject> XivApiPaginator<X> getSearchIterator(Class<X> cls, SearchFilter filter) {
		return getSearchIterator(cls, filter, defaultListOptions());
	}

	public <X extends XivApiObject> XivApiPaginator<X> getSearchIterator(Class<X> cls, SearchFilter filter, ListOptions<? super X> options) {
		String sheetName = MappingUtils.validateAndGetSheetName(cls);

		ObjectFieldMapper<X> mapping = new ObjectFieldMapper<>(cls, mapper);

		List<QueryField> fields = mapping.getQueryFields();

		int perPage = options.getPerPage();

		URI firstPageUri;
		try {
			firstPageUri = new URIBuilder(getBaseUri())
					.appendPath("search")
					// TODO: doesn't support multi-sheet searching yet
					.setParameter("sheets", sheetName)
					.setParameter("query", filter.toFilterString())
					.setParameter("limit", String.valueOf(perPage))
					.addParameters(MappingUtils.formatQueryFields(fields))
					.build();
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		JsonNode firstPage = sendGET(firstPageUri);

		return new XivApiSearchPaginator<>(this, firstPage, firstPageUri, options::shouldStop, mapping, 100);
	}

	@Override
	public void close() {
		client.close();
	}
}
