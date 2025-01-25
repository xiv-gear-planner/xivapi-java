package gg.xp.xivapi.pagination;

/**
 * Controls how inner object deduplication works in the context of lists and searching.
 * e.g. if two Items have the same ClassJobCategory, then instead of keeping two objects with identical contents
 * in memory, both Items can simply point to the same ClassJobCategory instance. For single-object requests, the "scope"
 * of deduplication is that single request. For lists/searches, the behavior can be configured, as if it is not
 * expected that there will be very many duplicates, the caching may not be helpful.
 */
public enum ListCacheMode {
	/**
	 * No caching at all.
	 */
	None,
	/**
	 * Cache context per item - same as if the items were retrieved individually.
	 */
	PerItem,
	/**
	 * Cache context per page of items.
	 */
	PerPage,
	/**
	 * One cache context for the whole list/search.
	 */
	WholeQuery
}
