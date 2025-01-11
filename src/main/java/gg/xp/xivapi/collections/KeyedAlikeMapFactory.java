package gg.xp.xivapi.collections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * KeyedAlikeMapFactory is used to take a set of allowed keys, and produce many {@link KeyedAlikeMap} instances from it.
 * <p>
 * To use, specify your key set in the constructor. Then, use {@link #create} in either form to create a map instance.
 * Every KeyedAlikeMap instance created by a KeyedAlikeMapFactory will share the same underlying map, and store the
 * actual values in an array. This saves memory in a similar manner to Python's "slots".
 *
 * @param <K> The key type.
 * @see KeyedAlikeMap
 */
public class KeyedAlikeMapFactory<K> {
	private final Map<K, Integer> keyMapping;

	public KeyedAlikeMapFactory(Set<K> keys) {
		Map<K, Integer> map = new HashMap<>();
		int count = 0;
		for (K key : keys) {
			map.put(key, count++);
		}
		this.keyMapping = map;
	}

	public <V> KeyedAlikeMap<K, V> create() {
		return new KeyedAlikeMap<>(keyMapping);
	}

	public <V> KeyedAlikeMap<K, V> create(Map<K, V> values) {
		KeyedAlikeMap<K, V> out = new KeyedAlikeMap<>(keyMapping);
		out.putAll(values);
		return out;
	}

}
