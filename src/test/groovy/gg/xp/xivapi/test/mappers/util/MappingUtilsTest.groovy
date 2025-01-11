package gg.xp.xivapi.test.mappers.util

import com.fasterxml.jackson.core.type.TypeReference
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Test

import java.lang.reflect.Type

import static gg.xp.xivapi.mappers.util.MappingUtils.parameterizedTypeToRawClass
import static gg.xp.xivapi.mappers.util.MappingUtils.unknownValueEquals
import static org.junit.jupiter.api.Assertions.*

@CompileStatic
class MappingUtilsTest {

	@Test
	void testUnknownValueEquals() {
		assertTrue unknownValueEquals(null, null)
		assertFalse unknownValueEquals(5, null)
		assertFalse unknownValueEquals(null, "Foo")
		String[] array1 = new String[]{"Foo", "Bar"}
		String[] array2 = new String[]{"Foo", "Bar"}
		String[] array3 = new String[]{"Bar", "Baz"}
		assertTrue unknownValueEquals(array1, array2)
		assertFalse unknownValueEquals(array1, array3)
		assertFalse unknownValueEquals("Foo", array3)
		assertFalse unknownValueEquals(array3, "Foo")
		Object obj1 = new Object()
		Object obj2 = new Object()
		assertFalse unknownValueEquals(obj1, obj2)
	}

	@Test
	void testParameterizedTypeToRawClass() {
		Type stringType = new TypeReference<String>() {}.type
		assertEquals String, parameterizedTypeToRawClass(stringType)

		Type listStringType = new TypeReference<List<String>>() {}.type
		assertEquals List, parameterizedTypeToRawClass(listStringType)

		Type listListListStringType = new TypeReference<List<List<List<String>>>>() {}.type
		assertEquals List, parameterizedTypeToRawClass(listListListStringType)
	}

}