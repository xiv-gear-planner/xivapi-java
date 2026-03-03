package gg.xp.xivapi.test.collections

import gg.xp.xivapi.collections.KeySerDe
import gg.xp.xivapi.collections.KeyedAlikeMapFactory
import gg.xp.xivapi.collections.MapSerializationProxy
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@CompileStatic
class MapSerializationProxyTest {

	static class NonSerializableKey {
		String id

		NonSerializableKey(String id) { this.id = id }

		@Override
		boolean equals(Object o) { o instanceof NonSerializableKey && ((NonSerializableKey) o).id == id }

		@Override
		int hashCode() { id.hashCode() }
	}

	static class KeySerDeImpl implements KeySerDe<NonSerializableKey, String>, Serializable {
		@Serial
		static final long serialVersionUID = 1L

		@Override
		String toSerializableForm(NonSerializableKey key) { key.id }

		@Override
		NonSerializableKey fromSerializeableForm(String serializable) { new NonSerializableKey(serializable) }
	}

	@Test
	void testSerialization() {
		Map<NonSerializableKey, String> underlying = new HashMap<>()
		NonSerializableKey k1 = new NonSerializableKey("k1")
		NonSerializableKey k2 = new NonSerializableKey("k2")
		underlying.put(k1, "v1")
		underlying.put(k2, "v2")

		KeySerDe<NonSerializableKey, String> serDe = new KeySerDeImpl()
		MapSerializationProxy<NonSerializableKey, String> proxy = new MapSerializationProxy<>(underlying, serDe)

		// Verify it works as a map
		Assertions.assertEquals(2, proxy.size())
		Assertions.assertEquals("v1", proxy.get(k1))
		Assertions.assertEquals("v2", proxy.get(k2))

		// Serialize
		ByteArrayOutputStream baos = new ByteArrayOutputStream()
		ObjectOutputStream oos = new ObjectOutputStream(baos)
		oos.writeObject(proxy)
		oos.close()

		// Deserialize
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())
		ObjectInputStream ois = new ObjectInputStream(bais)
		MapSerializationProxy<NonSerializableKey, String> deserialized = (MapSerializationProxy<NonSerializableKey, String>) ois.readObject()

		// Verify deserialized proxy
		Assertions.assertEquals(2, deserialized.size())
		// Note: they are new objects because of deserialization
		Assertions.assertEquals("v1", deserialized.get(new NonSerializableKey("k1")))
		Assertions.assertEquals("v2", deserialized.get(new NonSerializableKey("k2")))

		Assertions.assertEquals(proxy, deserialized)
	}

	@Test
	void testKeyedAlikeMapSerialization() {
		NonSerializableKey k1 = new NonSerializableKey("k1")
		NonSerializableKey k2 = new NonSerializableKey("k2")
		Set<NonSerializableKey> keys = new LinkedHashSet<>([k1, k2])

		KeySerDe<NonSerializableKey, String> serDe = new KeySerDeImpl()
		KeyedAlikeMapFactory<NonSerializableKey> factory = new KeyedAlikeMapFactory<>(keys, serDe)

		var map = factory.create()
		map.put(k1, "value1")
		map.put(k2, "value2")

		// Serialize
		ByteArrayOutputStream baos = new ByteArrayOutputStream()
		ObjectOutputStream oos = new ObjectOutputStream(baos)
		oos.writeObject(map)
		oos.close()

		// Deserialize
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())
		ObjectInputStream ois = new ObjectInputStream(bais)
		var deserializedMap = ois.readObject() as Map<NonSerializableKey, String>

		Assertions.assertEquals("value1", deserializedMap.get(k1))
		Assertions.assertEquals("value2", deserializedMap.get(k2))
		Assertions.assertEquals(map, deserializedMap)
	}
}
