package gg.xp.xivapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.clienttypes.XivApiSchemaVersion;
import gg.xp.xivapi.clienttypes.XivApiSettings;
import gg.xp.xivapi.exceptions.XivApiException;
import gg.xp.xivapi.exceptions.XivApiMappingException;
import gg.xp.xivapi.filters.SearchFilter;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.QueryFieldsBuilder;
import gg.xp.xivapi.mappers.RootQueryFieldsBuilder;
import gg.xp.xivapi.mappers.objects.ObjectFieldMapper;
import gg.xp.xivapi.mappers.util.MappingUtils;
import gg.xp.xivapi.pagination.ListOptions;
import gg.xp.xivapi.pagination.XivApiListPaginator;
import gg.xp.xivapi.pagination.XivApiPaginator;
import gg.xp.xivapi.pagination.XivApiSearchPaginator;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

/**
 * The main xivapi client class.
 */
public class XivApiClient implements AutoCloseable {
	private static final Logger log = LoggerFactory.getLogger(XivApiClient.class);

	private static final ExecutorService exs = Executors.newCachedThreadPool();

	private final ObjectMapper mapper = new ObjectMapper();
	private final XivApiSettings settings;
	private final HttpClient client;
	private final @Nullable Semaphore limiter;
	private ListOptions<XivApiObject> defaultListOpts = ListOptions.<XivApiObject>newBuilder().build();

	/**
	 * Constructor with a settings object.
	 *
	 * @param settings The built settings object
	 * @see XivApiSettings.Builder
	 * @see XivApiSettings#newBuilder()
	 */
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

	/**
	 * Constructor that assembles a builder for you and applies your lambda to it.
	 *
	 * @param configurer Configuration to apply to the builder
	 * @see XivApiSettings.Builder
	 */
	public XivApiClient(Consumer<XivApiSettings.Builder> configurer) {
		this(XivApiSettings.newBuilder().configure(configurer).build());
	}

	/**
	 * Constructor with default settings
	 */
	public XivApiClient() {
		this(XivApiSettings.newBuilder().build());
	}

	/**
	 * @return The settings used to construct this XivApiClient
	 */
	public XivApiSettings getSettings() {
		return settings;
	}

	/**
	 * @return The base URI for this client
	 */
	public URI getBaseUri() {
		return settings.getBaseUri();
	}

	/**
	 * Send a raw request
	 *
	 * @param uri The URI
	 * @return The root JSON node.
	 */
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
			if (root.has("code") && root.has("message")) {
				throw new XivApiException("Xivapi returned error. Code %s, message '%s'".formatted(root.get("code"), root.get("message").textValue()));
			}
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

	private final Map<Class<? extends XivApiObject>, Future<ObjectFieldMapper<? extends XivApiObject>>> mappingCache = new ConcurrentHashMap<>();

	/**
	 * Get or create the mapping for an object type
	 *
	 * @param cls The class to assemble a mapping for
	 * @param <X> The type of the mapping
	 * @return The mapping
	 */
	@SuppressWarnings("unchecked")
	private <X extends XivApiObject> ObjectFieldMapper<X> getMapping(Class<X> cls) {

		Future<ObjectFieldMapper<? extends XivApiObject>> existing = mappingCache.computeIfAbsent(cls, (clazz) -> exs.submit(() -> new ObjectFieldMapper<>(cls, mapper)));
		try {
			return (ObjectFieldMapper<X>) existing.get();
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		catch (ExecutionException e) {
			//noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
			throw new XivApiMappingException("Failed to construct mapping for %s".formatted(cls), e.getCause());
		}

	}

	/**
	 * @return Default URL query parameters for all requests
	 */
	private List<NameValuePair> getDefaultParams() {
		List<NameValuePair> out = new ArrayList<>();
		if (settings.getGameVersion() != null) {
			out.add(new BasicNameValuePair("version", settings.getGameVersion()));
		}
		if (settings.getSchemaVersion() != null) {
			out.add(new BasicNameValuePair("schema", settings.getSchemaVersion()));
		}
		return out;
	}

	/**
	 * @return A URI builder, initialized to the base URI plus default query params
	 */
	public URIBuilder buildUri() {
		return new URIBuilder(getBaseUri()).addParameters(getDefaultParams());
	}

	/**
	 * Given a lambda, configure a URIBuilder with that lambda. Starts with {@link #buildUri()}.
	 *
	 * @param func The lambda to use to configure the builder
	 * @return the built URI
	 */
	public URI buildUri(Consumer<URIBuilder> func) {
		URIBuilder builder = buildUri();
		func.accept(builder);
		try {
			return builder.build();
		}
		catch (URISyntaxException e) {
			throw new XivApiException("Error constructing URI", e);
		}
	}

	/**
	 * Retrieve a single item by primary key/row ID
	 *
	 * @param cls The type/sheet to retrieve
	 * @param id  The ID to retrieve
	 * @param <X> The type/sheet to retrieve
	 * @return The mapped object
	 */
	public <X extends XivApiObject> X getById(Class<X> cls, int id) {

		String sheetName = MappingUtils.validateAndGetSheetName(cls);

		ObjectFieldMapper<X> mapping = getMapping(cls);

		RootQueryFieldsBuilder rootQf = new RootQueryFieldsBuilder();
		mapping.buildQueryFields(rootQf);

		URI uri = buildUri(builder -> builder
				.appendPath("sheet")
				.appendPath(sheetName)
				.appendPath(String.valueOf(id))
				.addParameters(rootQf.formatQueryFields()));

		JsonNode root = sendGET(uri);

		XivApiSchemaVersion sv = MappingUtils.makeSchemaVersion(root.get("schema").textValue());

		XivApiContext context = new XivApiContext(root, settings, sv);

		return mapping.getValue(root, context);
	}

	/**
	 * @return Return the default {@link ListOptions}.
	 */
	private ListOptions<XivApiObject> defaultListOptions() {
		return defaultListOpts;
	}

	public void setDefaultListOpts(ListOptions<XivApiObject> defaultListOpts) {
		this.defaultListOpts = defaultListOpts;
	}

	/**
	 * {@link #getListIterator(Class, ListOptions)} with default ListOptions.
	 *
	 * @param cls The element type of the list
	 * @param <X> The element type of the list
	 * @return An iterator for the list.
	 */
	public <X extends XivApiObject> XivApiPaginator<X> getListIterator(Class<X> cls) {
		return getListIterator(cls, defaultListOptions());
	}

	/**
	 * Get a list iterator for an entire sheet. Note that by default, this returns a fairly dumb paginator
	 * implementation. It does not do any prefetching or buffering, so its {@link Iterator#hasNext()} method
	 * will block whenever a new sheet needs to be retrieved.
	 *
	 * @param cls     The element type of the list
	 * @param <X>     The element type of the list
	 * @param options List options to configure number per page, stop condition, and other future settings.
	 * @return An iterator for the list.
	 */
	public <X extends XivApiObject> XivApiPaginator<X> getListIterator(Class<X> cls, ListOptions<? super X> options) {
		String sheetName = MappingUtils.validateAndGetSheetName(cls);

		ObjectFieldMapper<X> mapping = getMapping(cls);

		RootQueryFieldsBuilder rootQf = new RootQueryFieldsBuilder();
		mapping.buildQueryFields(rootQf);

		int perPage = options.getPerPage();

		URI firstPageUri = buildUri(builder -> builder
				.appendPath("sheet")
				.appendPath(sheetName)
				.addParameters(rootQf.formatQueryFields())
				.setParameter("limit", String.valueOf(perPage)));

		JsonNode firstPage = sendGET(firstPageUri);

		return new XivApiListPaginator<>(this, firstPage, firstPageUri, options::shouldStop, mapping, 100);
	}

	public <X extends XivApiObject> XivApiPaginator<X> getSearchIterator(Class<X> cls, SearchFilter filter) {
		return getSearchIterator(cls, filter, defaultListOptions());
	}

	/**
	 * Get a list iterator for a search query. Note that by default, this returns a fairly dumb paginator
	 * implementation. It does not do any prefetching or buffering, so its {@link Iterator#hasNext()} method
	 * will block whenever a new sheet needs to be retrieved.
	 * <p>
	 * This does not support querying across multiple sheets.
	 *
	 * @param cls     The element type of the list
	 * @param filter  The search filter to use for the query.
	 * @param options List options to configure number per page, stop condition, and other future settings.
	 * @param <X>     The element type of the list
	 * @return An iterator for the list.
	 */
	public <X extends XivApiObject> XivApiPaginator<X> getSearchIterator(Class<X> cls, SearchFilter filter, ListOptions<? super X> options) {
		String sheetName = MappingUtils.validateAndGetSheetName(cls);

		ObjectFieldMapper<X> mapping = getMapping(cls);

		RootQueryFieldsBuilder rootQf = new RootQueryFieldsBuilder();
		mapping.buildQueryFields(rootQf);

		int perPage = options.getPerPage();

		URI firstPageUri = buildUri(builder -> builder.appendPath("search")
				// TODO: doesn't support multi-sheet searching yet
				.setParameter("sheets", sheetName)
				.setParameter("query", filter.toFilterString())
				.setParameter("limit", String.valueOf(perPage))
				.addParameters(rootQf.formatQueryFields()));

		JsonNode firstPage = sendGET(firstPageUri);

		return new XivApiSearchPaginator<>(this, firstPage, firstPageUri, options::shouldStop, mapping, 100);
	}

	public List<String> getGameVersions() {
		URI uri = buildUri(builder -> builder.appendPath("version"));
		JsonNode result = sendGET(uri);
		return mapper.convertValue(result, new TypeReference<List<String>>() {
		});
	}

	/**
	 * Shut down this client.
	 */
	@Override
	public void close() {
		client.close();
		mappingCache.clear();
	}
}
