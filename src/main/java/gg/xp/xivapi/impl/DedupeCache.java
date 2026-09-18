package gg.xp.xivapi.impl;

import java.util.function.Function;

public interface DedupeCache {
	<K, T> T computeIfAbsent(Class<T> type, int id, K cacheKey, Function<K, T> mappingFunction);
}
