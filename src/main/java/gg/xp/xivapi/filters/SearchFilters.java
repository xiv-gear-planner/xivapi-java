package gg.xp.xivapi.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SearchFilters {

	private SearchFilters() {
	}

	private record SearchFilterOr(List<SearchFilter> filters) implements SearchFilter {

		@Override
		public String toFilterString() {
			return filters.stream()
					.map(SearchFilter::toFilterString)
					.collect(Collectors.joining(" ", "(", ")"));
		}

		@Override
		public String toString() {
			return filters.stream()
					.map(SearchFilter::toString)
					.collect(Collectors.joining(" ", "Or(", ")"));
		}
	}

	private record SearchFilterAnd(List<SearchFilter> filters) implements SearchFilter {

		@Override
		public String toFilterString() {
			return filters.stream()
					.map(SearchFilter::toFilterString)
					.map(s -> "+" + s)
					.collect(Collectors.joining(" "));
		}

		@Override
		public String toString() {
			return filters.stream()
					.map(SearchFilter::toString)
					.collect(Collectors.joining(" ", "And(", ")"));
		}
	}

	private record SearchFilterNot(SearchFilter filter) implements SearchFilter {

		@Override
		public String toFilterString() {
			return "-" + filter.toFilterString();
		}

		@Override
		public String toString() {
			return "Not(%s)".formatted(filter);
		}
	}

	private record SearchFilterImpl(String filterValue) implements SearchFilter {

		@Override
		public String toFilterString() {
			return filterValue;
		}

		@Override
		public String toString() {
			return filterValue;
		}
	}

	public static SearchFilter of(String filterValue) {
		return new SearchFilterImpl(filterValue);
	}

	public static SearchFilter or(String... filters) {
		return new SearchFilterOr(Stream.of(filters).map(SearchFilters::of).toList());
	}

	public static SearchFilter or(List<SearchFilter> filters) {
		return new SearchFilterOr(new ArrayList<>(filters));
	}

	public static SearchFilter or(SearchFilter... filters) {
		return new SearchFilterOr(Arrays.asList(filters));
	}

	public static SearchFilter and(String... filters) {
		return new SearchFilterAnd(Stream.of(filters).map(SearchFilters::of).toList());
	}

	public static SearchFilter and(List<SearchFilter> filters) {
		return new SearchFilterAnd(new ArrayList<>(filters));
	}

	public static SearchFilter and(SearchFilter... filters) {
		return new SearchFilterAnd(Arrays.asList(filters));
	}

	public static SearchFilter not(SearchFilter filter) {
		return new SearchFilterNot(filter);
	}

	public static SearchFilter binary(String field, String operator, Object value) {
		final String formattedValue = value instanceof String str ? "\"%s\"".formatted(str) : value.toString();
		return of("%s%s%s".formatted(field, operator, formattedValue));
	}

	public static SearchFilter eq(String field, Object value) {
		return binary(field, "=", value);
	}

	public static SearchFilter isTrue(String field) {
		return binary(field, "=", true);
	}

	public static SearchFilter isFalse(String field) {
		return binary(field, "=", false);
	}

	public static SearchFilter gte(String field, Number value) {
		return binary(field, ">=", value);
	}

	public static SearchFilter lte(String field, Number value) {
		return binary(field, "<=", value);
	}

	public static SearchFilter gt(String field, Number value) {
		return binary(field, ">", value);
	}

	public static SearchFilter lt(String field, Number value) {
		return binary(field, "<", value);
	}

	public static SearchFilter strPart(String field, String value) {
		return binary(field, "~", value);
	}

	public static String any(String field) {
		return field + "[]";
	}
}
