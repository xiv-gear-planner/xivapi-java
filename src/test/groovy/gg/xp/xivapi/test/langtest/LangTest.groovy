package gg.xp.xivapi.test.langtest

import gg.xp.xivapi.XivApiClient
import gg.xp.xivapi.clienttypes.XivApiSettings
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@CompileStatic
class LangTest {
	@Test
	void testLangs() {
		var client = new XivApiClient({ XivApiSettings.Builder it ->
			it.schemaVersion = "exdschema@5f292f39f3deab2c43bee62b202b54ebf51e15b7-2024.08.02.0000.0000"
			it.gameVersion = "7.05"
		})

		AozAction action = client.getById(AozAction, 5)

		Assertions.assertEquals(5, action.rowId)
		Assertions.assertEquals(93, action.location)
		Assertions.assertEquals("the Binding Coil of Bahamut - Turn 1", action.locationFull.name)
		Assertions.assertEquals("Verschlungene Schatten 1", action.locationFull.nameDe)
		// TODO: this one specifically doesn't work
//		Assertions.assertEquals("Verschlungene Schatten 1", action.locationDe.name)

		
	}
}
