package gg.xp.xivapi.test.basictest

import gg.xp.xivapi.XivApiClient
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@CompileStatic
class EmptyStringNullTest {
	@Test
	void test() {
		var client = new XivApiClient()
		// Limsa - not a duty
		var limsa = client.getById(TerritoryType, 128)
		Assertions.assertNotNull(limsa.contentFinderConditionNonNull)
		Assertions.assertNull(limsa.contentFinderConditionNonNull.name)
		Assertions.assertEquals("", limsa.contentFinderConditionNonNull.nameNotNull)
		Assertions.assertEquals(0, limsa.contentFinderConditionNonNull.rowId)
		Assertions.assertNull(limsa.contentFinderConditionNullable)
	}
}
