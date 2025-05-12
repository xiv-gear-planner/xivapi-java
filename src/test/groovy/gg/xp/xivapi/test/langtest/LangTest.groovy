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
		Assertions.assertEquals("the Binding Coil of Bahamut - Turn 1", action.locationFull.nameStrings.en)
		Assertions.assertEquals("Verschlungene Schatten 1", action.locationFull.nameStrings.de)
		Assertions.assertEquals("le Labyrinthe de Bahamut I", action.locationFull.nameStrings.fr)
		Assertions.assertEquals("大迷宮バハムート：邂逅編1", action.locationFull.nameStrings.jp)
		Assertions.assertEquals("Verschlungene Schatten 1", action.locationDe.name)

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

	@Test
	void testLangs2() {
		var client = new XivApiClient({ XivApiSettings.Builder it ->
			it.schemaVersion = "exdschema@5f292f39f3deab2c43bee62b202b54ebf51e15b7-2024.08.02.0000.0000"
			it.gameVersion = "7.05"
		})

		AozAction2 action = client.getById(AozAction2, 5)

		Assertions.assertEquals(5, action.rowId)

		Assertions.assertEquals("the Binding Coil of Bahamut - Turn 1", action.locationsAll.en.name)
		Assertions.assertEquals("Verschlungene Schatten 1", action.locationsAll.de.name)
		Assertions.assertEquals("le Labyrinthe de Bahamut I", action.locationsAll.fr.name)
		Assertions.assertEquals("大迷宮バハムート：邂逅編1", action.locationsAll.jp.name)

		Assertions.assertEquals("An industrial form of machina-based aetherial manipulation developed and adapted by the Allagan Empire. Through the compression of ambient lightning-aspected aether, the caster (or casting machina) floods the vicinity with highly charged arcs of electricity.", action.description.en)

		// Test java serialization
		AozAction2 rehydrated = serializeAndDeserialize(action)

		Assertions.assertEquals("the Binding Coil of Bahamut - Turn 1", rehydrated.locationsAll.en.name)
		Assertions.assertEquals("Verschlungene Schatten 1", rehydrated.locationsAll.de.name)
		Assertions.assertEquals("le Labyrinthe de Bahamut I", rehydrated.locationsAll.fr.name)
		Assertions.assertEquals("大迷宮バハムート：邂逅編1", rehydrated.locationsAll.jp.name)


	}

	@Test
	void testLangs3() {
		var client = new XivApiClient({ XivApiSettings.Builder it ->
			it.schemaVersion = "exdschema@5f292f39f3deab2c43bee62b202b54ebf51e15b7-2024.08.02.0000.0000"
			it.gameVersion = "7.05"
		})

		Action action = client.getById(Action, 7395)

		String dflt = 'Increases damage dealt by 15%.\nDuration: 20s\nAdditional Effect: Grants Fire\'s Rumination\nDuration: 20s'
		String html = 'Increases damage dealt by 15%.<br><span style="color:rgba(0,204,34,1);">Duration:</span> 20s<br><span style="color:rgba(0,204,34,1);">Additional Effect: </span>Grants <span style="color:rgba(255,255,102,1);">Fire\'s Rumination</span><br><span style="color:rgba(0,204,34,1);">Duration: </span>20s'
		String de = 'Dein ausgeteilter Schaden ist um 15 % erhöht.\nDauer: 20 Sekunden\nZusatzeffekt: Gewährt dir Himmel und Erde.\nDauer: 20 Sekunden'
		String deHtml = 'Dein ausgeteilter Schaden ist um 15 % erhöht.<br>Dauer: 20 Sekunden<br>Zusatzeffekt: Gewährt dir <span style="color:rgba(255,255,102,1);">Himmel und Erde</span>.<br>Dauer: 20 Sekunden'

		Assertions.assertEquals(dflt, action.descriptionDefault)
		Assertions.assertEquals(dflt, action.descriptionRaw)
		Assertions.assertEquals(html, action.descriptionHtml)

		Assertions.assertEquals(de, action.descriptionDefaultDe)
		Assertions.assertEquals(de, action.descriptionRawDe)
		Assertions.assertEquals(deHtml, action.descriptionHtmlDe)

		Assertions.assertEquals(dflt, action.descriptionAll.en)
		Assertions.assertEquals(dflt, action.descriptionRawAll.en)
		Assertions.assertEquals(html, action.descriptionHtmlAll.en)

		Assertions.assertEquals(de, action.descriptionAll.de)
		Assertions.assertEquals(de, action.descriptionRawAll.de)
		Assertions.assertEquals(deHtml, action.descriptionHtmlAll.de)

	}
}
