package gg.xp.xivapi.collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Proxy for a map that allows us to plug in a serde for the keys.
 * The values must already be serializable.
 *
 * @param <K>
 * @param <V>
 */
public class MapSerializationProxy<K, V> implements Map<K, V>, Serializable {

	@Serial
	private static final long serialVersionUID = 2L;

	private final Map<K, V> underlying;
	private final KeySerDe<K, ? extends Serializable> serDe;

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType") // We specifically do not want an independent copy
	public MapSerializationProxy(Map<K, V> underlying, KeySerDe<K, ? extends Serializable> serDe) {
		this.underlying = underlying;
		this.serDe = serDe;
	}

	private record SerializableForm<K, V, S extends Serializable>(
			Map<S, V> serializableMap,
			KeySerDe<K, S> serDe
	) implements Serializable {
		@Serial
		private Object readResolve() {
			Map<K, V> underlying = new HashMap<>(serializableMap.size());
			for (var entry : serializableMap.entrySet()) {
				underlying.put(serDe.fromSerializeableForm(entry.getKey()), entry.getValue());
			}
			return new MapSerializationProxy<>(underlying, serDe);
		}
	}

	@Serial
	@SuppressWarnings("unchecked")
	private Object writeReplace() throws ObjectStreamException {
		Map<Serializable, V> serializableMap = new HashMap<>(underlying.size());
		for (var entry : underlying.entrySet()) {
			serializableMap.put(serDe.toSerializableForm(entry.getKey()), entry.getValue());
		}
		return new SerializableForm<>(serializableMap, ((KeySerDe<K, Serializable>) serDe));
	}

	@Override
	public int size() {
		return underlying.size();
	}

	@Override
	public boolean isEmpty() {
		return underlying.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return underlying.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return underlying.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return underlying.get(key);
	}

	@Nullable
	@Override
	public V put(K key, V value) {
		return underlying.put(key, value);
	}

	@Override
	public V remove(Object key) {
		return underlying.remove(key);
	}

	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> m) {
		underlying.putAll(m);
	}

	@Override
	public void clear() {
		underlying.clear();
	}

	@NotNull
	@Override
	public Set<K> keySet() {
		return underlying.keySet();
	}

	@NotNull
	@Override
	public Collection<V> values() {
		return underlying.values();
	}

	@NotNull
	@Override
	public Set<Entry<K, V>> entrySet() {
		return underlying.entrySet();
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o instanceof MapSerializationProxy<?, ?> other) {
			return Objects.equals(underlying, other.underlying);
		}
		return underlying.equals(o);
	}

	@Override
	public int hashCode() {
		return underlying.hashCode();
	}

	@Override
	public String toString() {
		return underlying.toString();
	}
}
