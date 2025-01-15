package gg.xp.xivapi.pagination;

import java.util.function.BiPredicate;

public class ListOptions<X> {
	private final int perPage;
	private final BiPredicate<Integer, X> stopCondition;
	private final ListCacheMode listCacheMode;

	@Deprecated // Use builder
	public ListOptions(int perPage, BiPredicate<Integer, X> stopCondition) {
		this(perPage, stopCondition, ListCacheMode.WholeQuery);
	}

	private ListOptions(int perPage, BiPredicate<Integer, X> stopCondition, ListCacheMode listCacheMode) {
		this.perPage = perPage;
		this.stopCondition = stopCondition;
		this.listCacheMode = listCacheMode;
	}

	public int getPerPage() {
		return perPage;
	}

	public BiPredicate<Integer, X> getStopCondition() {
		return stopCondition;
	}

	public boolean shouldStop(int page, X item) {
		return stopCondition.test(page, item);
	}

	public ListCacheMode getListCacheMode() {
		return listCacheMode;
	}

	public static <X> ListOptionsBuilder<X> newBuilder() {
		return new ListOptionsBuilder<>();
	}

	public static class ListOptionsBuilder<X> {
		private int perPage = 100;
		private BiPredicate<Integer, X> stopCondition = (index, value) -> false;
		private ListCacheMode listCacheMode = ListCacheMode.WholeQuery;

		public ListOptionsBuilder<X> perPage(int perPage) {
			this.perPage = perPage;
			return this;
		}

		public ListOptionsBuilder<X> stopCondition(BiPredicate<Integer, X> stopCondition) {
			this.stopCondition = stopCondition;
			return this;
		}

		public ListOptionsBuilder<X> listCacheMode(ListCacheMode listCacheMode) {
			this.listCacheMode = listCacheMode;
			return this;
		}

		public ListOptions<X> build() {
			return new ListOptions<>(perPage, stopCondition, listCacheMode);
		}
	}
}
