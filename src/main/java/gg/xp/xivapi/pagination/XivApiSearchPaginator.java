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
public final class XivApiSearchPaginator<X extends XivApiObject> extends XivApiPaginator<X> {

	public XivApiSearchPaginator(XivApiClient client, JsonNode firstResponse, URI originalUri, BiPredicate<Integer, X> stopCondition, FieldMapper<X> mapper, int perPageItemCount) {
		super(client, originalUri, stopCondition, mapper, perPageItemCount, firstResponse);
	}

	@Override
	protected URI getNextPageUri() {
		String cursor = currentPage.rootNode.get("next").textValue();
		try {
			return new URIBuilder(originalUri)
					.setParameter("cursor", cursor)
					.build();
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected JsonNode getResultsNode(JsonNode rootNode) {
		return rootNode.get("results");
	}

	@Override
	protected boolean hasMorePages() {
		return currentPage.rootNode.get("next") != null;
	}

}
