package gg.xp.xivapi.filters;

/**
 * Interface representing a search filter
 */
public interface SearchFilter {
	/**
	 * @return This search filter, formatted into a string.
	 */
	String toFilterString();

	default String toFilterStringWrapped() {
		return toFilterString();
	}
}
