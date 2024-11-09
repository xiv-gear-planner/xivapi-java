package gg.xp.xivapi.test.langtest

import com.fasterxml.jackson.databind.ObjectMapper
import gg.xp.xivapi.XivApiClient
import gg.xp.xivapi.clienttypes.XivApiSettings
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

import static gg.xp.xivapi.test.testutils.TestUtils.serializeAndDeserialize

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
		// TODO: this one specifically doesn't work yet
		// https://discord.com/channels/474518001173921794/474519195963490305/1303576638482681867
		Assertions.assertEquals("Verschlungene Schatten 1", action.locationDe.name)

		Assertions.assertEquals("Verschlungene Schatten 1", action.locationsAll.de.name)

		Assertions.assertEquals("the Binding Coil of Bahamut - Turn 1", action.locationFull.nameStrings['en'])
		Assertions.assertEquals("Verschlungene Schatten 1", action.locationFull.nameStrings['de'])

		// Make sure this serializes in a sane way
		Map<String, String> serialized = new ObjectMapper().convertValue(action.locationFull.nameStrings, Map.class)

		Assertions.assertEquals("the Binding Coil of Bahamut - Turn 1", serialized['en'])
		Assertions.assertEquals("Verschlungene Schatten 1", serialized['de'])

		String asJson = new ObjectMapper().writeValueAsString(action.locationFull.nameStrings)
		Assertions.assertEquals("{\"de\":\"Verschlungene Schatten 1\",\"en\":\"the Binding Coil of Bahamut - Turn 1\",\"fr\":\"le Labyrinthe de Bahamut I\",\"ja\":\"大迷宮バハムート：邂逅編1\"}", asJson)

		// Test java serialization
		AozAction rehydrated = serializeAndDeserialize(action)

		Assertions.assertEquals(5, rehydrated.rowId)
		Assertions.assertEquals(93, rehydrated.location)
		Assertions.assertEquals("the Binding Coil of Bahamut - Turn 1", rehydrated.locationFull.name)
		Assertions.assertEquals("Verschlungene Schatten 1", rehydrated.locationFull.nameDe)

	}
}
