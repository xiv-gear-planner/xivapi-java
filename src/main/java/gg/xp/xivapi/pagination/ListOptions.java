package gg.xp.xivapi.pagination;

import java.util.function.BiPredicate;

@SuppressWarnings("ClassCanBeRecord") // Future expansion
public class ListOptions<X> {
	private final int perPage;
	private final BiPredicate<Integer, X> stopCondition;

	public ListOptions(int perPage, BiPredicate<Integer, X> stopCondition) {
		this.perPage = perPage;
		this.stopCondition = stopCondition;
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

	public static <X> ListOptionsBuilder<X> newBuilder() {
		return new ListOptionsBuilder<>();
	}

	public static class ListOptionsBuilder<X> {
		private int perPage = 100;
		private BiPredicate<Integer, X> stopCondition = (index, value) -> false;

		public ListOptionsBuilder<X> perPage(int perPage) {
			this.perPage = perPage;
			return this;
		}

		public ListOptionsBuilder<X> stopCondition(BiPredicate<Integer, X> stopCondition) {
			this.stopCondition = stopCondition;
			return this;
		}

		public ListOptions<X> build() {
			return new ListOptions<>(perPage, stopCondition);
		}
	}
}
