package gg.xp.xivapi.pagination;

import com.fasterxml.jackson.databind.JsonNode;
import gg.xp.xivapi.XivApiClient;
import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.clienttypes.XivApiSchemaVersion;
import gg.xp.xivapi.exceptions.XivApiDeserializationException;
import gg.xp.xivapi.impl.DedupeCache;
import gg.xp.xivapi.impl.DedupeCacheImpl;
import gg.xp.xivapi.impl.NoopDedupeCache;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.FieldMapper;
import gg.xp.xivapi.mappers.util.MappingUtils;
import org.apache.commons.collections4.IteratorUtils;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Base list/search paginator implementation. Not thread safe. Does not do any prefetching.
 *
 * @param <X>
 */
public abstract sealed class XivApiPaginator<X extends XivApiObject> implements Iterator<X> permits XivApiListPaginator, XivApiSearchPaginator {
	private final XivApiClient client;
	protected final URI originalUri;
	private final BiPredicate<Integer, X> stopCondition;
	private final FieldMapper<X> mapper;
	protected final int perPageItemCount;
	private final ListCacheMode cacheMode;
	private final DedupeCache cache;
	protected XivApiPage currentPage;
	private int globalBaseIndex;
	private boolean hasHitStopCondition;

	XivApiPaginator(XivApiClient client, URI originalUri, BiPredicate<Integer, X> stopCondition, FieldMapper<X> mapper, int perPageItemCount, JsonNode firstResponse, ListCacheMode cacheMode) {
		this.client = client;
		this.originalUri = originalUri;
		this.stopCondition = stopCondition;
		this.mapper = mapper;
		this.perPageItemCount = perPageItemCount;
		this.cacheMode = cacheMode;
		switch (cacheMode) {
			case None -> cache = NoopDedupeCache.INSTANCE;
			case PerItem, PerPage -> cache = null;
			case WholeQuery -> cache = new DedupeCacheImpl();
			default -> throw new IllegalArgumentException("Unknown cache mode " + cacheMode);
		}
		// Must be called last as it reads instance fields!
		currentPage = new XivApiPage(firstResponse);
	}

	protected abstract URI getNextPageUri();

	private void nextPage() {
		globalBaseIndex += currentPage.size();
		URI newURI = getNextPageUri();

		JsonNode newRoot = client.sendGET(newURI);
		currentPage = new XivApiPage(newRoot);
	}

	@Override
	public boolean hasNext() {
		if (!currentPage.hasAnyValues()) {
			return false;
		}
		if (hasHitStopCondition) {
			return false;
		}
		if (!currentPage.hasNext()) {
			// If the page has fewer entries than expected, we are done
			if (!hasMorePages()) {
				return false;
			}
			nextPage();
		}
		return currentPage.hasNext();
	}

	protected boolean hasMorePages() {
		return currentPage.size() >= perPageItemCount;
	}

	@Override
	public X next() {
		if (!currentPage.hasNext()) {
			throw new NoSuchElementException("Current page has no more values");
		}
		return currentPage.next();
	}

	protected abstract JsonNode getResultsNode(JsonNode rootNode);

	protected class XivApiPage implements Iterator<X> {
		protected final List<X> values;
		protected int index;
		protected JsonNode rootNode;

		XivApiPage(JsonNode root) {
			this.rootNode = root;
			JsonNode rows = getResultsNode(rootNode);
			if (rows == null) {
				throw new XivApiDeserializationException("Missing main results field in response");
			}
			XivApiSchemaVersion sv = MappingUtils.makeSchemaVersion(root.get("schema").textValue());
			Iterable<JsonNode> iter = rows::elements;
			DedupeCache pageCache = cache == null && cacheMode == ListCacheMode.PerPage ? new DedupeCacheImpl() : cache;
			values = StreamSupport.stream(iter.spliterator(), false)
					.map(node -> {
						DedupeCache itemCache = pageCache == null && cacheMode == ListCacheMode.PerItem ? new DedupeCacheImpl() : cache;
						var context = new XivApiContext(node, client.getSettings(), sv, client.getUrlResolver(), itemCache);
						return mapper.getValue(node, context);
					})
					.toList();
		}

		public boolean hasNext() {
			if (values.isEmpty()) {
				return false;
			}
			// If we have 10 items, we want to stop when index is 10
			if (index >= values.size()) {
				return false;
			}
			X peek = values.get(index);
			boolean fullStop = stopCondition.test(index + globalBaseIndex, peek);
			if (fullStop) {
				hasHitStopCondition = true;
				return false;
			}
			return true;
		}

		boolean hasAnyValues() {
			return !values.isEmpty();
		}

		public X next() {
			X out;
			try {
				out = values.get(index);
			}
			catch (IndexOutOfBoundsException e) {
				throw new NoSuchElementException(e);
			}
			index++;
			return out;
		}

		public int size() {
			return values.size();
		}
	}

	public List<X> toList() {
		return IteratorUtils.toList(this);
	}

	public Stream<X> stream() {
		return StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED),
				false);
	}

	public Iterator<X> toBufferedIterator(int bufferSize) {
		return new BufferedIterator<>(this, bufferSize);
	}

	public Stream<X> toBufferedStream(int bufferSize) {
		return StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(toBufferedIterator(bufferSize), Spliterator.ORDERED),
				false);
	}


}
