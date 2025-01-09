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

		map[key2] = "Stuff2"
		assertSize 2, map
		Assertions.assertEquals([(key1): "Stuff", (key2): "Stuff2"], map)

		// Overwrite
		map[key1] = "Stuff3"
		assertSize 2, map
		Assertions.assertEquals([(key1): "Stuff3", (key2): "Stuff2"], map)

		map.remove key1
		assertSize 1, map
		Assertions.assertEquals([(key2): "Stuff2"], map)

		map[key3] = null
		assertSize 2, map
		Assertions.assertEquals([(key2): "Stuff2", (key3): null], map)

		map.clear()
		assertSize 0, map
		Assertions.assertEquals([:], map)
	}

	@Test
	void testIteration() {
		var map = factory.create()
		assertSize 0, map

		map[key1] = "Stuff"
		assertSize 1, map
		Assertions.assertEquals([(key1): "Stuff"], map)

		map[key2] = "Stuff2"
		assertSize 2, map
		Assertions.assertEquals([(key1): "Stuff", (key2): "Stuff2"], map)

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

	}

}
