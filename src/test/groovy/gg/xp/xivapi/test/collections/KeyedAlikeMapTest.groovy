package gg.xp.xivapi.test.collections


import gg.xp.xivapi.collections.KeyedAlikeMapFactory
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@CompileStatic
class KeyedAlikeMapTest {

	private static final String key1 = "Foo"
	private static final String key2 = "Bar"
	private static final String key3 = "Baz"
	private static final Set<String> keySet = new LinkedHashSet([key1, key2, key3])
	private static KeyedAlikeMapFactory<String> factory = new KeyedAlikeMapFactory<>(keySet)

	private static void assertSize(int expected, Map<?, ?> map) {
		Assertions.assertEquals expected, map.size()
		Assertions.assertEquals expected, map.keySet().size()
		Assertions.assertEquals expected, map.values().size()
		Assertions.assertEquals expected, map.entrySet().size()
		Assertions.assertEquals expected, map.iterator().toList().size()
		if (expected == 0) {
			Assertions.assertTrue map.isEmpty()
		}
		else {
			Assertions.assertFalse map.isEmpty()
		}
	}

	private static void equalsIgnoreOrder(Collection<?> expected, Collection<?> actual) {
		Assertions.assertEquals expected.toSet(), actual.toSet()
	}

	@Test
	void basicTest() {
		var map = factory.create()
		assertSize 0, map

		map[key1] = "Stuff"
		assertSize 1, map
		Assertions.assertEquals([(key1): "Stuff"], map)

		var prevKey2 = map.put key2, "Stuff2"
		assertSize 2, map
		Assertions.assertNull(prevKey2)
		Assertions.assertEquals([(key1): "Stuff", (key2): "Stuff2"], map)

		// Overwrite
		var prevKey1 = map.put key1, "Stuff3"
		map[key1] = "Stuff3"
		assertSize 2, map
		Assertions.assertEquals("Stuff", prevKey1)
		Assertions.assertEquals([(key1): "Stuff3", (key2): "Stuff2"], map)

		map.remove key1
		assertSize 1, map
		Assertions.assertEquals([(key2): "Stuff2"], map)
		Assertions.assertFalse(map.containsValue("asdf"))
		Assertions.assertTrue(map.containsValue("Stuff2"))
		Assertions.assertFalse(map.containsValue(null))

		map[key3] = null
		assertSize 2, map
		Assertions.assertEquals([(key2): "Stuff2", (key3): null], map)
		Assertions.assertFalse(map.containsValue("asdf"))
		Assertions.assertTrue(map.containsValue("Stuff2"))
		Assertions.assertTrue(map.containsValue(null))

		var prevKey3 = map.put key3, "stuff"
		Assertions.assertEquals(null, prevKey3)

		map.clear()
		assertSize 0, map
		Assertions.assertEquals([:], map)

		var before = map.remove(key1)
		Assertions.assertNull(before)
		var beforeInvalid = map.remove("invalid")
		Assertions.assertNull(beforeInvalid)
	}

	@Test
	void testIteration() {
		var map = factory.create()
		assertSize 0, map
		Assertions.assertEquals "KeyedAlikeMap{}", map.toString()

		map[key1] = "Stuff"
		assertSize 1, map
		Assertions.assertEquals([(key1): "Stuff"], map)

		map[key2] = "Stuff2"
		assertSize 2, map
		Assertions.assertEquals([(key1): "Stuff", (key2): "Stuff2"], map)
		Assertions.assertEquals($/KeyedAlikeMap{${key2}=Stuff2, ${key1}=Stuff}/$.toString(), map.toString())

		// Key set does not have a defined order
		equalsIgnoreOrder([key1, key2], map.keySet())
		equalsIgnoreOrder([key1, key2], map.entrySet().collect { it.key })
		// Values should happen in the original array order
		Assertions.assertEquals(["Stuff", "Stuff2"], map.values().toList())
		// Entry set does not have a defined order
		equalsIgnoreOrder(["Stuff", "Stuff2"], map.entrySet().collect { it.value })

		map[key3] = null
		map.remove(key2)
		assertSize 2, map
		Assertions.assertEquals([(key1): "Stuff", (key3): null], map)
		// Key set does not have a defined order
		equalsIgnoreOrder([key1, key3], map.keySet())
		equalsIgnoreOrder([key1, key3], map.entrySet().collect { it.key })
		// Values should happen in the original array order
		Assertions.assertEquals(["Stuff", null], map.values().toList())
		// Entry set does not have a defined order
		equalsIgnoreOrder(["Stuff", null], map.entrySet().collect { it.value })

		var map2 = factory.create(map)
		Assertions.assertEquals map, map2
		assertSize 2, map2

		Assertions.assertEquals map.hashCode(), map2.hashCode()

		Assertions.assertEquals map, map
		Assertions.assertNotEquals map, 1

		// Size mismatch
		map2[key2] = "Yeah"
		Assertions.assertNotEquals map, map2

		// Same keys, different values
		var map3 = factory.create(map)
		Assertions.assertEquals map, map3
		assertSize 2, map3
		map3[key1] = "No"
		assertSize 2, map3
		Assertions.assertNotEquals map, map3

		// Different keys
		map2.remove(key3)
		Assertions.assertNotEquals map, map2

	}

	@Test
	void testValidation() {
		var map = factory.create()
		Assertions.assertThrows(IllegalArgumentException) {
			map.put("invalid", "foo")
		}
	}

}
