package gg.xp.xivapi.collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * KeyedAlikeMap is a map with a defined set of allowed keys. The intent is for multiple maps which are going to have
 * an identical set of possible keys to share the same underlying key mapping. The map's values are stored in a flat
 * array, where the indices map to keys, and the values are values. The map passed into the constructor maps keys to
 * indices. Assuming that enough of the possible keys are in use, and the key-to-index map is re-used across many maps,
 * this saves significant memory over a traditional hash map.
 * <p>
 * Thread safety: concurrent reads are okay, and raw concurrent writes are probably fine. However, modifying the map
 * while iterating and other combinations of simultaneous reading/writing are undefined. Methods such as size/isEmpty
 * have undefined behavior as well if they overlap with a write.
 * <p>
 * This class is not meant to be instantiated by the user. Instead, use your key set to create a
 * {@link KeyedAlikeMapFactory}, which may then be used to produce KeyedAlikeMap instances.
 *
 * @param <K> The key type.
 * @param <V> The value type.
 * @see KeyedAlikeMapFactory
 */
@SuppressWarnings("Convert2streamapi") // performance
public class KeyedAlikeMap<K, V> implements Map<K, V> {

	/**
	 * Contains the universe of keys. The integers represent the index that the value corresponding to the key will be
	 * inserted into the valueMapping array.
	 */
	private final Map<K, Integer> keyMapping;
	/**
	 * Contains the values. Indices correspond to keyMapping values.
	 * <p>
	 * This is specifically an array of Object rather than V so that we can use {@link #NULL_MARKER} to represent
	 * entries which have been explicitly set to null. Since Object defaults to null, this means that the default
	 * state of the map is to have no entries.
	 */
	private final Object[] values;
	/**
	 * Sentry value for elements which have explicitly been set to null.
	 * Unset values instead use a literal null.
	 */
	private static final Object NULL_MARKER = new Object();

	KeyedAlikeMap(Map<K, Integer> keyMapping) {
		//noinspection AssignmentOrReturnOfFieldWithMutableType
		this.keyMapping = keyMapping;
		values = new Object[keyMapping.size()];
	}

	@Override
	public int size() {
		int sz = 0;
		for (Object o : values) {
			if (o != null) {
				sz++;
			}
		}
		return sz;
	}

	@Override
	public boolean isEmpty() {
		for (Object o : values) {
			if (o != null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean containsKey(@Nullable Object key) {
		// a null value in valueMapping means that the key is not present.
		int index = indexForLax(key);
		// We also need to account for the possibility that the key does not exist in our allowed keys.
		return index >= 0 && values[index] != null;
	}

	@Override
	public boolean containsValue(Object value) {
		// No better way without increasing memory usage. Just brute force iterate over all values.
		for (Object o : values) {
			if (value == null) {
				if (o == NULL_MARKER) {
					return true;
				}
			}
			else {
				if (Objects.equals(o, value)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get the index for a key. Throws an exception if the key is not in the allowed key set.
	 * @param key The key
	 * @return The corresponding value index
	 * @throws IllegalArgumentException If the key is not in the allowed key set
	 */
	private int indexForStrict(Object key) {
		int index = indexForLax(key);
		if (index < 0) {
			throw new IllegalArgumentException("Key not present in allowed key set: " + key);
		}
		return index;
	}

	/**
	 * Get the index for a key. Returns -1 if the key is not in the allowed key set.
	 *
	 * @param key The key to look up.
	 * @return The index, or -1 if the key is not in the allowed key set.
	 */
	private int indexForLax(Object key) {
		Integer index = keyMapping.get(key);
		if (index == null) {
			return -1;
		}
		if (index < 0 || index >= values.length) {
			throw new IllegalArgumentException("Invalid index: " + index);
		}
		return index;
	}

	@Override
	public @Nullable V get(Object key) {
		int index = indexForLax(key);
		// Not a valid key
		if (index < 0) {
			return null;
		}
		var value = values[index];
		// Value explicitly set to null
		if (value == NULL_MARKER) {
			return null;
		}
		return (V) value;
	}

	@Nullable
	@Override
	public V put(K key, V value) {
		// Unfortunately, we have to throw here if an invalid key is supplied. No better option.
		// Unlike Collection.add, there's no way to indicate that a map put failed.
		int index = indexForStrict(key);
		var oldValue = values[index];
		// We can set a null value explicitly, but we have to use NULL_MARKER to differentiate between an explicit null
		// and a value which has not been set.
		values[index] = value == null ? NULL_MARKER : value;
		return oldValue == NULL_MARKER ? null : (V) oldValue;
	}

	@Override
	public V remove(Object key) {
		int index = indexForLax(key);
		if (index < 0) {
			// Unlike put, we don't need to throw here.
			return null;
		}
		var oldValue = values[index];
		values[index] = null;
		return oldValue == NULL_MARKER ? null : (V) oldValue;
	}

	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> m) {
		m.forEach(this::put);
	}

	@Override
	public void clear() {
		Arrays.fill(values, null);
	}

	@NotNull
	@Override
	public Set<K> keySet() {
		// keySet is fairly straightforward - iterate over the keyMapping and filter out null (unset) values.
		return new AbstractSet<>() {
			@Override
			public Iterator<K> iterator() {
				return keyMapping.keySet().stream().filter(key -> {
					int index = indexForStrict(key);
					Object valueRaw = values[index];
					return valueRaw != null;
				}).iterator();
			}

			@Override
			public int size() {
				return KeyedAlikeMap.this.size();
			}

			@Override
			public boolean contains(Object o) {
				return KeyedAlikeMap.this.containsKey(o);
			}
		};
	}

	@NotNull
	@Override
	public Collection<V> values() {
		return new AbstractCollection<>() {
			// Values is fairly straightforward as well - iterate over the values array, filtering out null values.
			@Override
			public @NotNull Iterator<V> iterator() {
				return Arrays.stream(values)
						.flatMap(valueRaw -> {
							if (valueRaw == null) {
								return Stream.empty();
							}
							else if (valueRaw == NULL_MARKER) {
								return Stream.of((V) null);
							}
							return Stream.of((V) valueRaw);
						}).iterator();
			}

			@Override
			public int size() {
				return KeyedAlikeMap.this.size();
			}

			@Override
			public boolean contains(Object o) {
				return KeyedAlikeMap.this.containsValue(o);
			}
		};
	}

	@NotNull
	@Override
	public Set<Entry<K, V>> entrySet() {
		return new AbstractSet<>() {
			@Override
			public @NotNull Iterator<Entry<K, V>> iterator() {
				return keyMapping.keySet().stream().map(key -> {
					int index = indexForStrict(key);
					Object valueRaw = values[index];
					if (valueRaw == null) {
						return null;
					}
					else if (valueRaw == NULL_MARKER) {
						valueRaw = null;
					}
					Object finalValueRaw = valueRaw;
					return (Entry<K, V>) new EntryImpl<>(key, (V) finalValueRaw);
				}).filter(Objects::nonNull).iterator();
			}

			@Override
			public int size() {
				return KeyedAlikeMap.this.size();
			}
		};
	}

	private record EntryImpl<K, V>(K key, V value) implements Map.Entry<K, V> {

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(Object value) {
			// This would require keeping a reference to the map.
			throw new UnsupportedOperationException("Cannot set values via entrySet()");
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;

		// Type and size check first
		if (!(o instanceof Map<?, ?> m))
			return false;
		if (m.size() != size())
			return false;

		try {
			for (Entry<K, V> e : entrySet()) {
				K key = e.getKey();
				V value = e.getValue();
				if (value == null) {
					// Special case for explicit null values.
					if (!(m.get(key) == null && m.containsKey(key)))
						return false;
				}
				else {
					if (!value.equals(m.get(key)))
						return false;
				}
			}
		}
		catch (ClassCastException | NullPointerException unused) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int h = 0;
		for (Entry<K, V> entry : entrySet())
			h += entry.hashCode();
		return h;
	}

	@Override
	public String toString() {
		Iterator<Entry<K, V>> i = entrySet().iterator();
		if (!i.hasNext()) {
			return "KeyedAlikeMap{}";
		}
		StringBuilder sb = new StringBuilder("KeyedAlikeMap{");
		for (; ; ) {
			Entry<K, V> e = i.next();
			K key = e.getKey();
			V value = e.getValue();
			sb.append(key == this ? "(this Map)" : key);
			sb.append('=');
			sb.append(value == this ? "(this Map)" : value);
			if (!i.hasNext())
				return sb.append('}').toString();
			sb.append(',').append(' ');
		}
	}
}
