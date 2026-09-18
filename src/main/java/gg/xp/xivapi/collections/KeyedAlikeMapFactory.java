package gg.xp.xivapi.collections;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

	/**
	 * Constructs a KeyedAlikeMapFactory for the given set of keys.
	 * <p>
	 * Note that resulting maps will only be serializable if the keys are serializable.
	 *
	 * @param keys The set of all possible keys. If this is a LinkedHashMap, then key ordering will be preserved.
	 */
	public KeyedAlikeMapFactory(Set<K> keys) {
		this.keyMapping = createKeyMapping(keys);
	}

	/**
	 * Constructor that also accepts a serde helper, so that maps with non-serializable keys can still be serialized.
	 *
	 * @param keys  The set of all possible keys. If this is a LinkedHashMap, then key ordering will be preserved.
	 * @param serDe A helper to convert the keys to/from a serializable for.
	 */
	public KeyedAlikeMapFactory(Set<K> keys, KeySerDe<K, ?> serDe) {
		Map<K, Integer> map = createKeyMapping(keys);
		this.keyMapping = new MapSerializationProxy<>(map, serDe);
	}

	private Map<K, Integer> createKeyMapping(Set<K> keys) {
		Map<K, Integer> map;
		// If caller specifically wants an ordered mapping, they can indicate such by passing a LinkedHashMap.
		if (keys instanceof LinkedHashSet) {
			map = new LinkedHashMap<>();
		}
		else {
			map = new HashMap<>();
		}
		int count = 0;
		for (K key : keys) {
			map.put(key, count++);
		}
		return map;
	}

	/**
	 * Create an empty map
	 *
	 * @param <V> The type of values
	 * @return An empty map
	 */
	public <V> KeyedAlikeMap<K, V> create() {
		return new KeyedAlikeMap<>(keyMapping);
	}

	/**
	 * Create a map from the given values
	 *
	 * @param values The initial values
	 * @param <V>    The type of values
	 * @return A map with the initial values
	 * @throws IllegalArgumentException when a key in the initial is not in the set of keys handled by this factory
	 */
	public <V> KeyedAlikeMap<K, V> create(Map<K, V> values) {
		KeyedAlikeMap<K, V> out = new KeyedAlikeMap<>(keyMapping);
		out.putAll(values);
		return out;
	}

}
