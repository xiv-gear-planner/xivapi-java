package gg.xp.xivapi.test.collections

import gg.xp.xivapi.collections.KeyedAlikeMap
import gg.xp.xivapi.collections.KeyedAlikeMapFactory
import gg.xp.xivapi.mappers.objects.serialization.MethodKeySerDe
import gg.xp.xivapi.mappers.objects.ObjectInvocationHandler
import gg.xp.xivapi.test.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class SharedKeyMapSerializationTest {

	interface TestInterface {
		String getName()
		int getId()
	}

	@Test
	void testSharedMapAfterSerialization() {
		Method getNameMethod = TestInterface.class.getMethod("getName")
		Method getIdMethod = TestInterface.class.getMethod("getId")
		Set<Method> methods = [getNameMethod, getIdMethod] as Set
		
		var serDe = new MethodKeySerDe()
		var factory = new KeyedAlikeMapFactory<Method>(methods, serDe)
		
		var map1 = factory.create()
		map1.put(getNameMethod, "Item 1")
		map1.put(getIdMethod, 1)
		
		var map2 = factory.create()
		map2.put(getNameMethod, "Item 2")
		map2.put(getIdMethod, 2)
		
		// Both maps should share the same keyMapping
		// We can't access it directly but we can verify it by looking at internal structure or just trusting the factory
		// For verification, let's put them in a list and serialize the list
		
		def handler1 = new ObjectInvocationHandler(map1, true)
		def proxy1 = Proxy.newProxyInstance(this.class.classLoader, [TestInterface] as Class[], handler1) as TestInterface
		
		def handler2 = new ObjectInvocationHandler(map2, true)
		def proxy2 = Proxy.newProxyInstance(this.class.classLoader, [TestInterface] as Class[], handler2) as TestInterface
		
		def list = [proxy1, proxy2]
		
		def deserializedList = TestUtils.serializeAndDeserialize(list) as List<TestInterface>
		
		def dProxy1 = deserializedList[0]
		def dProxy2 = deserializedList[1]
		
		Assertions.assertEquals("Item 1", dProxy1.getName())
		Assertions.assertEquals(1, dProxy1.getId())
		Assertions.assertEquals("Item 2", dProxy2.getName())
		Assertions.assertEquals(2, dProxy2.getId())
		
		// Now verify that they share the same backing keyMapping
		def dHandler1 = Proxy.getInvocationHandler(dProxy1) as ObjectInvocationHandler
		def dHandler2 = Proxy.getInvocationHandler(dProxy2) as ObjectInvocationHandler
		
		def dMap1 = dHandler1.methodValueMap as KeyedAlikeMap
		def dMap2 = dHandler2.methodValueMap as KeyedAlikeMap
		
		// Accessing private field keyMapping via reflection
		def field = KeyedAlikeMap.class.getDeclaredField("keyMapping")
		field.setAccessible(true)
		
		def keyMapping1 = field.get(dMap1)
		def keyMapping2 = field.get(dMap2)
		
		Assertions.assertSame(keyMapping1, keyMapping2, "Key mappings should be the same reference after deserialization")
	}
}
