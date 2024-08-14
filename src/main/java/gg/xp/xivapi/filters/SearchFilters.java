package gg.xp.xivapi.filters;

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

	public static SearchFilter or(SearchFilter... filters) {
		return new SearchFilterOr(Arrays.asList(filters));
	}

	public static SearchFilter and(String... filters) {
		return new SearchFilterAnd(Stream.of(filters).map(SearchFilters::of).toList());
	}

	public static SearchFilter and(SearchFilter... filters) {
		return new SearchFilterAnd(Arrays.asList(filters));
	}

	public static SearchFilter not(SearchFilter filter) {
		return new SearchFilterNot(filter);
	}
}
