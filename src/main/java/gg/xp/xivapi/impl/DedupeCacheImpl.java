package gg.xp.xivapi.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class DedupeCacheImpl implements DedupeCache {
	private record CacheKey(Class<?> type, Object values) {}

	private final Map<CacheKey, Object> cache = new ConcurrentHashMap<>();

	@Override
	public <K, T> T computeIfAbsent(Class<T> type, K cacheKey, Function<K, T> mappingFunction) {
		var key = new CacheKey(type, cacheKey);
		//noinspection unchecked
		return (T) cache.computeIfAbsent(key, (ck) -> mappingFunction.apply((K) ck.values));
	}
}
