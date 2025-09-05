package gg.xp.xivapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.assets.AssetFormat;
import gg.xp.xivapi.clienttypes.GameVersion;
import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.clienttypes.XivApiSchemaVersion;
import gg.xp.xivapi.clienttypes.XivApiSettings;
import gg.xp.xivapi.clienttypes.XivApiSubrowObject;
import gg.xp.xivapi.exceptions.XivApiException;
import gg.xp.xivapi.exceptions.XivApiMappingException;
import gg.xp.xivapi.filters.SearchFilter;
import gg.xp.xivapi.impl.DedupeCacheImpl;
import gg.xp.xivapi.impl.UrlResolverImpl;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.objects.ObjectFieldMapper;
import gg.xp.xivapi.mappers.objects.RootMapper;
import gg.xp.xivapi.mappers.util.MappingUtils;
import gg.xp.xivapi.mappers.util.ThreadingUtils;
import gg.xp.xivapi.pagination.ListOptions;
import gg.xp.xivapi.pagination.XivApiListPaginator;
import gg.xp.xivapi.pagination.XivApiPaginator;
import gg.xp.xivapi.pagination.XivApiSearchPaginator;
import gg.xp.xivapi.url.XivApiUrlResolver;
import org.apache.hc.core5.net.URIBuilder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

	private static final ExecutorService exs = Executors.newCachedThreadPool(
			ThreadingUtils.namedDaemonThreadFactory("XivApiClient")
	);

	private final ObjectMapper mapper = new ObjectMapper();
	private final XivApiSettings settings;
	private final HttpClient client;
	private final @Nullable Semaphore limiter;
	private final UrlResolverImpl urlResolver;
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
		client = HttpClient.newBuilder().build();
		int limit = settings.getConcurrencyLimit();
		if (limit > 0) {
			limiter = new Semaphore(limit);
		}
		else {
			limiter = null;
		}
		this.urlResolver = new UrlResolverImpl(settings);
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

		HttpRequest request = HttpRequest.newBuilder(uri).GET().header("User-Agent", settings.getUserAgent()).build();
		String response;
		JsonNode root;
		try {
			if (limiter != null) {
				limiter.acquire();
			}
			response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
			root = mapper.readTree(response);
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

	private final Map<Class<? extends XivApiObject>, Future<RootMapper<? extends XivApiObject>>> rootMappingCache = new ConcurrentHashMap<>();

	/**
	 * Get or create the mapping for an object type
	 *
	 * @param cls The class to assemble a mapping for
	 * @param <X> The type of the mapping
	 * @return The mapping
	 */
	@SuppressWarnings("unchecked")
	private <X extends XivApiObject> RootMapper<X> getMapping(Class<X> cls) {

		Future<RootMapper<? extends XivApiObject>> existing = rootMappingCache.computeIfAbsent(cls, (clazz) -> exs.submit(() -> new RootMapper<>(new ObjectFieldMapper<>(cls, mapper))));
		try {
			return (RootMapper<X>) existing.get();
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
	 * @return A URI builder, initialized to the base URI plus default query params
	 */
	public URIBuilder buildUri() {
		return urlResolver.buildUri();
	}

	/**
	 * Given a lambda, configure a URIBuilder with that lambda. Starts with {@link #buildUri()}.
	 *
	 * @param func The lambda to use to configure the builder
	 * @return the built URI
	 */
	public URI buildUri(Consumer<URIBuilder> func) {
		return urlResolver.buildUri(func);
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

		RootMapper<X> mapping = getMapping(cls);

		URI uri = buildUri(builder -> builder
				.appendPath("sheet")
				.appendPath(sheetName)
				.appendPath(String.valueOf(id))
				.addParameters(mapping.getQueryFields()));

		JsonNode root = sendGET(uri);

		XivApiSchemaVersion sv = MappingUtils.makeSchemaVersion(root.get("schema").textValue());

		var cache = new DedupeCacheImpl();
		XivApiContext context = new XivApiContext(root, settings, sv, urlResolver, cache);

		return mapping.getWrappedMapper().getValue(root, context);
	}

	/**
	 * Retrieve a single item by row+subrow ID. e.g. MapMarker 2:1 would be row ID 2, subrow ID 1.
	 *
	 * @param cls The type/sheet to retrieve
	 * @param rowId  The row ID to retrieve
	 * @param subrowId  The subrow ID to retrieve
	 * @param <X> The type/sheet to retrieve
	 * @return The mapped object
	 */
	public <X extends XivApiSubrowObject> X getBySubrowId(Class<X> cls, int rowId, int subrowId) {

		String sheetName = MappingUtils.validateAndGetSheetName(cls);

		RootMapper<X> mapping = getMapping(cls);

		URI uri = buildUri(builder -> builder
				.appendPath("sheet")
				.appendPath(sheetName)
				.appendPath(String.format("%d:%d", rowId, subrowId))
				.addParameters(mapping.getQueryFields()));

		JsonNode root = sendGET(uri);

		XivApiSchemaVersion sv = MappingUtils.makeSchemaVersion(root.get("schema").textValue());

		var cache = new DedupeCacheImpl();
		XivApiContext context = new XivApiContext(root, settings, sv, urlResolver, cache);

		return mapping.getWrappedMapper().getValue(root, context);
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

		RootMapper<X> mapping = getMapping(cls);

		int perPage = options.getPerPage();

		URI firstPageUri = buildUri(builder -> builder
				.appendPath("sheet")
				.appendPath(sheetName)
				.addParameters(mapping.getQueryFields())
				.setParameter("limit", String.valueOf(perPage)));

		JsonNode firstPage = sendGET(firstPageUri);

		return new XivApiListPaginator<>(this, firstPage, firstPageUri, options::shouldStop, mapping.getWrappedMapper(), 100, options.getListCacheMode());
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

		RootMapper<X> mapping = getMapping(cls);

		int perPage = options.getPerPage();

		URI firstPageUri = buildUri(builder -> builder.appendPath("search")
				// TODO: doesn't support multi-sheet searching yet
				.setParameter("sheets", sheetName)
				.setParameter("query", filter.toFilterString())
				.setParameter("limit", String.valueOf(perPage))
				.addParameters(mapping.getQueryFields()));

		JsonNode firstPage = sendGET(firstPageUri);

		return new XivApiSearchPaginator<>(this, firstPage, firstPageUri, options::shouldStop, mapping.getWrappedMapper(), 100, options.getListCacheMode());
	}

	/**
	 * @return The list of available game versions, but flattened.
	 *
	 * @see #getGameVersionsFull()
	 */
	public List<String> getGameVersions() {
		return getGameVersionsFull()
				.stream()
				.flatMap(gv -> gv.names().stream())
				.toList();
	}

	/**
	 * @return The list of available game versions, in the original form that the API's /api/1/version endpoint
	 * returns.
	 */
	public List<GameVersion> getGameVersionsFull() {
		URI uri = buildUri(builder -> builder.appendPath("version"));
		JsonNode result = sendGET(uri).get("versions");
		return mapper.convertValue(result, new TypeReference<>() {
		});
	}

	public URI getAssetUri(String assetPath, AssetFormat format) {
		return urlResolver.getAssetUri(assetPath, format);
	}

	public URI getAssetUri(String assetPath, String format) {
		return urlResolver.getAssetUri(assetPath, format);
	}

	public <X extends XivApiObject> void validateModel(Class<X> clazz) {
		getMapping(clazz);
	}

	public XivApiUrlResolver getUrlResolver() {
		return urlResolver;
	}

	/**
	 * Shut down this client.
	 */
	@Override
	public void close() {
		client.close();
		rootMappingCache.clear();
	}
}
