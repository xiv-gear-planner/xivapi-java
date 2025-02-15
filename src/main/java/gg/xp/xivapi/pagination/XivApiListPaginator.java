package gg.xp.xivapi.pagination;

import com.fasterxml.jackson.databind.JsonNode;
import gg.xp.xivapi.XivApiClient;
import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.mappers.FieldMapper;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.BiPredicate;

/**
 * Raw list iterator. This is a dumb implementation that does not parallelize or readahead. It will request a new
 * page only when the existing page has been exhausted. The {@link #hasNext()} method may block until the next
 * page has been returned.
 * <p>
 * This implementation is not thread-safe. It should be accessed only by a single thread.
 * <p>
 * It is implemented as an iterator-of-iterators, where each page is an iterator, and the combined pagination
 *
 * @param <X>
 */
public final class XivApiListPaginator<X extends XivApiObject> extends XivApiPaginator<X> {

	public XivApiListPaginator(XivApiClient client, JsonNode firstResponse, URI originalUri, BiPredicate<Integer, X> stopCondition, FieldMapper<X> mapper, int perPageItemCount, ListCacheMode cacheMode) {
		super(client, originalUri, stopCondition, mapper, perPageItemCount, firstResponse, cacheMode);
	}

	@Override
	protected URI getNextPageUri() {
		X last = currentPage.values.get(currentPage.values.size() - 1);
		try {
			return new URIBuilder(originalUri)
					.setParameter("after", String.valueOf(last.getRowId()))
					.build();
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected JsonNode getResultsNode(JsonNode rootNode) {
		return rootNode.get("rows");
	}
}
