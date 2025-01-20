package gg.xp.xivapi.test.validation

import gg.xp.xivapi.XivApiClient
import gg.xp.xivapi.exceptions.XivApiMappingException
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

import java.util.function.Consumer

@CompileStatic
class ModelValidationTest {

	static <X> void assertThrows(Class<X> exceptionType, Executable runnable, Consumer<X> checker) {
		try {
			runnable.execute()
		}
		catch (Throwable t) {
			if (exceptionType.isInstance(t)) {
				checker.accept(t as X)
				return
			}
			else {
				Assertions.fail "Expected exception to be ${exceptionType}, received ${t}"
			}
		}
		Assertions.fail "Did not receive exception, expected ${exceptionType}"
	}

	@Test
	void testOmitZeroesValidation1() {
		var client = new XivApiClient()
		assertThrows(XivApiMappingException, {
			client.validateModel(TestModels.OmitZeroNonSheetMap)
		}, { outer ->
			Throwable inner = outer.cause
			Assertions.assertInstanceOf IllegalArgumentException, inner
			Assertions.assertEquals "@OmitZeroes only makes sense when dealing with a sheet object type", inner.message
		})
	}

	@Test
	void testOmitZeroesValidation2() {
		var client = new XivApiClient()
		assertThrows(XivApiMappingException, {
			client.validateModel(TestModels.OmitZeroNonSheetArray)
		}, { outer ->
			Throwable inner = outer.cause
			Assertions.assertInstanceOf IllegalArgumentException, inner
			Assertions.assertEquals "@OmitZeroes only makes sense when dealing with a sheet object type", inner.message
		})
	}

	@Test
	void testOmitZeroesValidation3() {
		var client = new XivApiClient()
		assertThrows(XivApiMappingException, {
			client.validateModel(TestModels.OmitZeroNonSheetList)
		}, { outer ->
			Throwable inner = outer.cause
			Assertions.assertInstanceOf IllegalArgumentException, inner
			Assertions.assertEquals "@OmitZeroes only makes sense when dealing with a sheet object type", inner.message
		})
	}

	@Test
	void testBaseName() {
		var client = new XivApiClient()
		assertThrows(XivApiMappingException, {
			client.validateModel(TestModels.BadName)
		}, { outer ->
			Throwable inner = outer.cause
			Assertions.assertInstanceOf IllegalArgumentException, inner
			Assertions.assertEquals "I don't know how to map field name 'foo' to a field", inner.message
		})

	}
}
