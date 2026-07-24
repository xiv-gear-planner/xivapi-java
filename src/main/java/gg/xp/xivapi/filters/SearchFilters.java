package gg.xp.xivapi.filters;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Collection of methods useful for constructing search filters.
 * <p>
 * All objects returned by this class are immutable. They can safely be re-used without making defensive copies.
 * <p>
 * Some search filters will intelligently "collapse" - for example not(not(foo)) will collapse down to foo, and
 * or(or(a, b), c) will collapse to or(a, b, c).
 * <p>
 * Some methods in here also have equivalents on the {@link SearchFilter} interface itself, mainly so that you can
 * use Groovy operator overloads seamlessly, e.g. a | b instead of or(a, b).
 */
public final class SearchFilters {

	private SearchFilters() {
	}

	private record SearchFilterOr(List<SearchFilter> filters) implements SearchFilter {

		@Override
		public String toFilterString() {
			// A one-entry OR can just use the inner part
			if (filters.size() == 1) {
				return filters.get(0).toFilterString();
			}
			return filters.stream()
					// First, auto-flatten nested ORs
					.flatMap(child -> {
						if (child instanceof SearchFilterOr orChild) {
							return orChild.filters().stream();
						}
						return Stream.of(child);
					})
					.map(searchFilter -> {
						String inner = searchFilter.toFilterStringWrapped();
						if (inner.startsWith("-") || searchFilter instanceof SearchFilterAnd) {
							return "(" + inner + ")";
						}
						return inner;
					})
					.collect(Collectors.joining(" "));
		}

		@Override
		public String toFilterStringWrapped() {
			return "(" + toFilterString() + ")";
		}

		@Override
		public @NotNull String toString() {
			return filters.stream()
					.map(SearchFilter::toString)
					.collect(Collectors.joining(" ", "Or(", ")"));
		}
	}

	private record SearchFilterAnd(List<SearchFilter> filters) implements SearchFilter {

		@Override
		public String toFilterString() {
			if (filters.size() == 1) {
				return filters.get(0).toFilterString();
			}
			return filters.stream()
					// First, auto-flatten nested ANDs
					.flatMap(child -> {
						if (child instanceof SearchFilterAnd andChild) {
							return andChild.filters().stream();
						}
						return Stream.of(child);
					})
					// Use the "wrapped" version, e.g. if I have a nested OR, it needs to have parenthesis around it
					.map(searchFilter -> {
						// Don't re-wrap NOTs. e.g. A && !B should become +A -B, not +A +(-B). The latter will work
						// but is inefficient.
						if (searchFilter instanceof SearchFilterNot notChild) {
							return notChild.toFilterString();
						}
						return searchFilter.toFilterStringWrapped();
					})
					.map(s -> {
						// For negative filters, we must not add a +.
						// i.e. "+Foo=GoodValue -Bar=BadValue" is correct, but "+Foo=GoodValue +-Bar=BadValue" is not.
						if (s.startsWith("-")) {
							return s;
						}
						return "+" + s;
					})
					.collect(Collectors.joining(" "));
		}

		@Override
		public @NotNull String toString() {
			return filters.stream()
					.map(SearchFilter::toString)
					.collect(Collectors.joining(" ", "And(", ")"));
		}
	}

	private record SearchFilterNot(SearchFilter filter) implements SearchFilter {

		@Override
		public String toFilterString() {
			// Unwrap double negation
			if (filter instanceof SearchFilterNot notChild) {
				return notChild.filter.toFilterString();
			}
			String inner = filter.toFilterString();
			if (inner.startsWith("-") || inner.startsWith("+")) {
				return "-(" + inner + ")";
			}
			return "-" + inner;
		}

		@Override
		public @NotNull String toString() {
			return "Not(%s)".formatted(filter);
		}
	}

	private record SearchFilterImpl(String filterValue) implements SearchFilter {

		@Override
		public String toFilterString() {
			return filterValue;
		}

		@Override
		public @NotNull String toString() {
			return filterValue;
		}
	}

	/**
	 * Search filter from a verbatim string. Must already be formatted correctly and have double-quotes and backslashes
	 * already escaped, but should not be URL-escaped.
	 *
	 * @param filterValue The search string.
	 * @return The search filter consisting of that literal string.
	 */
	public static SearchFilter of(String filterValue) {
		return new SearchFilterImpl(filterValue);
	}

	/**
	 * Combine multiple filters in an OR fashion.
	 *
	 * @param filters The raw string filters. Must already be formatted correctly minus URL escaping (see {@link #of(String)}.
	 * @return A filter representing the logical OR of the constituent filters.
	 */
	public static SearchFilter or(String... filters) {
		return new SearchFilterOr(Stream.of(filters).map(SearchFilters::of).toList());
	}

	/**
	 * Combine multiple filters in an OR fashion.
	 *
	 * @param filters The raw string filters. Must already be formatted correctly minus URL escaping (see {@link #of(String)}.
	 * @return A filter representing the logical OR of the constituent filters.
	 */
	public static SearchFilter or(List<SearchFilter> filters) {
		List<SearchFilter> flattened = filters.stream()
				.flatMap(f -> f instanceof SearchFilterOr orChild ? orChild.filters().stream() : Stream.of(f))
				.toList();
		return new SearchFilterOr(flattened);
	}

	/**
	 * Combine multiple filters in an OR fashion.
	 *
	 * @param filters The filters.
	 * @return A filter representing the logical OR of the constituent filters.
	 */
	public static SearchFilter or(SearchFilter... filters) {
		return or(Arrays.asList(filters));
	}

	/**
	 * Combine multiple filters in an AND fashion.
	 *
	 * @param filters The raw string filters. Must already be formatted correctly minus URL escaping (see {@link #of(String)}.
	 * @return A filter representing the logical AND of the constituent filters.
	 */
	public static SearchFilter and(String... filters) {
		return and(Stream.of(filters).map(SearchFilters::of).toList());
	}

	/**
	 * Combine multiple filters in an AND fashion.
	 *
	 * @param filters The raw string filters. Must already be formatted correctly minus URL escaping (see {@link #of(String)}.
	 * @return A filter representing the logical AND of the constituent filters.
	 */
	public static SearchFilter and(List<SearchFilter> filters) {
		List<SearchFilter> flattened = filters.stream()
				.flatMap(f -> f instanceof SearchFilterAnd andChild ? andChild.filters().stream() : Stream.of(f))
				.toList();
		return new SearchFilterAnd(flattened);
	}

	/**
	 * Combine multiple filters in an AND fashion.
	 *
	 * @param filters The raw string filters.
	 * @return A filter representing the logical AND of the constituent filters.
	 */
	public static SearchFilter and(SearchFilter... filters) {
		return and(Arrays.asList(filters));
	}

	/**
	 * Logically invert a filter.
	 *
	 * @param filter The filter to invert.
	 * @return The filter representing the logical inverse of the input.
	 */
	public static SearchFilter not(SearchFilter filter) {
		return new SearchFilterNot(filter);
	}

	/**
	 * Create a filter using a binary operator.
	 *
	 * @param field    The field on which to operate.
	 * @param operator The operator to apply.
	 * @param value    The value. String values will be quoted and escaped automatically.
	 * @return The binary filter.
	 */
	public static SearchFilter binary(String field, String operator, Object value) {
		final String formattedValue = value instanceof String str ? formatStringArg(str) : value.toString();
		return of("%s%s%s".formatted(field, operator, formattedValue));
	}

	/**
	 * Create a {@link #binary(String, String, Object)} filter using the '=' (equals) operator.
	 *
	 * @param field The field.
	 * @param value The expected value.
	 * @return The filter.
	 */
	public static SearchFilter eq(String field, Object value) {
		return binary(field, "=", value);
	}

	/**
	 * Create a filter for a boolean value being true.
	 *
	 * @param field The field on which to expect truth.
	 * @return The filter.
	 */
	public static SearchFilter isTrue(String field) {
		return binary(field, "=", true);
	}

	/**
	 * Create a filter for a boolean value being false.
	 *
	 * @param field The field on which to expect falsity.
	 * @return The filter.
	 */
	public static SearchFilter isFalse(String field) {
		return binary(field, "=", false);
	}

	/**
	 * Create a {@link #binary(String, String, Object)} filter using the '>=' (greater than or equal to) operator.
	 *
	 * @param field The field.
	 * @param value The comparison value.
	 * @return The filter.
	 */
	public static SearchFilter gte(String field, Number value) {
		return binary(field, ">=", value);
	}

	/**
	 * Create a {@link #binary(String, String, Object)} filter using the '<=' (less than or equal to) operator.
	 *
	 * @param field The field.
	 * @param value The comparison value.
	 * @return The filter.
	 */
	public static SearchFilter lte(String field, Number value) {
		return binary(field, "<=", value);
	}

	/**
	 * Create a {@link #binary(String, String, Object)} filter using the '>' (strictly greater than) operator.
	 *
	 * @param field The field.
	 * @param value The comparison value.
	 * @return The filter.
	 */
	public static SearchFilter gt(String field, Number value) {
		return binary(field, ">", value);
	}

	/**
	 * Create a {@link #binary(String, String, Object)} filter using the '<' (strictly less than) operator.
	 *
	 * @param field The field.
	 * @param value The comparison value.
	 * @return The filter.
	 */
	public static SearchFilter lt(String field, Number value) {
		return binary(field, "<", value);
	}

	/**
	 * Create a {@link #binary(String, String, Object)} filter using the '~' (string contains) operator.
	 *
	 * @param field The field.
	 * @param value The partial string to search for.
	 * @return The filter.
	 */
	public static SearchFilter strPart(String field, String value) {
		return binary(field, "~", value);
	}

	/**
	 * Modifies a field name with the '[]' modifier. This should be used with an array field, to expect that any index
	 * matches the filter to which this is applied.
	 *
	 * @param field The field to match.
	 * @return A String representing the field name with the "any index" modifier applied.
	 */
	public static String any(String field) {
		return field + "[]";
	}

	/**
	 * Modifies a field name with the '[index]' modifier. This should be used with an array field, to expect that a
	 * specific index matches the filter to which this is applied.
	 *
	 * @param field The field to match.
	 * @return A String representing the field name with the "specific index" modifier applied.
	 */
	public static String index(String field, int index) {
		return field + "[" + index + "]";
	}

	/**
	 * Escape values meant to be plugged in as the expected value of a filter.
	 * <p>
	 * The two characters that need to be escaped in string values are double quotes and backslashes. Both of them
	 * are escaped by prefixing them with a backslash. All other characters are passed through raw.
	 *
	 * @param raw The unescaped string.
	 * @return The escaped string.
	 */
	private static String formatStringArg(String raw) {
		// Replace
		String escaped = raw.replaceAll("[\\\\\"]", "\\\\$0");
		return '"' + escaped + '"';
	}
}
