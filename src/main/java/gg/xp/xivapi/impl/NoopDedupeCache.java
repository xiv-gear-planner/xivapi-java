package gg.xp.xivapi.impl;

import java.util.function.Function;

public class NoopDedupeCache implements DedupeCache{
	public static final DedupeCache INSTANCE = new NoopDedupeCache();
	@Override
	public <K, T> T computeIfAbsent(Class<T> type, K cacheKey, Function<K, T> mappingFunction) {
		return mappingFunction.apply(cacheKey);
	}
}
