package gg.xp.xivapi.test.basictest

import gg.xp.xivapi.XivApiClient
import gg.xp.xivapi.exceptions.XivApiErrorResponseException
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@CompileStatic
class SheetNotFoundTest {
	@Test
	void testSingleGet() {
		var client = new XivApiClient()
		var exc = Assertions.assertThrows(XivApiErrorResponseException) {
			client.getById NonExistent, 0
		}
		Assertions.assertEquals 404, exc.code
		Assertions.assertEquals 'not found: the Excel sheet "NonExistent" could not be found', exc.message
	}

	@Test
	void testSingleGetOpt() {
		var client = new XivApiClient()
		var exc = Assertions.assertThrows(XivApiErrorResponseException) {
			// optional get should still throw - it only returns null if it's specifically the row that is not found, not the whole sheet
			client.getByIdOpt NonExistent, 0
		}
		Assertions.assertEquals 404, exc.code
		Assertions.assertEquals 'not found: the Excel sheet "NonExistent" could not be found', exc.message
	}

	@Test
	void testIterator() {
		var client = new XivApiClient()
		var exc = Assertions.assertThrows(XivApiErrorResponseException) {
			client.getListIterator NonExistent
		}
		Assertions.assertEquals 404, exc.code
		Assertions.assertEquals 'not found: the Excel sheet "NonExistent" could not be found', exc.message
	}
}
