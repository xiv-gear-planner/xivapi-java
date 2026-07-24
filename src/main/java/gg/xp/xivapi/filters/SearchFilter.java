package gg.xp.xivapi.filters;

/**
 * Interface representing a search filter
 */
public interface SearchFilter {
	/**
	 * @return This search filter, formatted into a string.
	 */
	String toFilterString();

	/**
	 * Alternative version of {@link #toFilterString()} that should be called when this filter is going to be combined
	 * with other filters in such a way that it might need to be wrapped with parentheses. If that is the case, then
	 * this method should be overridden to do that.
	 *
	 * @return The same as {@link #toFilterString()} but in a manner safe to use in a nested context.
	 */
	default String toFilterStringWrapped() {
		return toFilterString();
	}

	// Groovy operator overloads

	/**
	 * Overload for Groovy "bitwise or" operator. Internally flattens nested levels, so that you can do something like
	 * {@code x | y | z} and it will be equivalent to {@code or(x, y, z)}.
	 *
	 * @param other The filter to "or" with.
	 * @return The result.
	 */
	default SearchFilter or(SearchFilter other) {
		return SearchFilters.or(this, other);
	}

	/**
	 * Overload for Groovy "bitwise and" operator. Internally flattens nested levels, so that you can do something like
	 * {@code x & y & z} and it will be equivalent to {@code and(x, y, z)}.
	 *
	 * @param other The filter to "and" with.
	 * @return The result.
	 */
	default SearchFilter and(SearchFilter other) {
		return SearchFilters.and(this, other);
	}

	/**
	 * Overload for Groovy "bitwise negate" operator.
	 *
	 * @return The negated filter.
	 */
	default SearchFilter bitwiseNegate() {
		return SearchFilters.not(this);
	}
}
