package gg.xp.xivapi.impl;

import java.util.function.Function;

public interface DedupeCache {
	<K, T> T computeIfAbsent(Class<T> type, K cacheKey, Function<K, T> mappingFunction);
}
