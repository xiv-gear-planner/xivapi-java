package gg.xp.xivapi.test.escape

import gg.xp.xivapi.XivApiClient
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@CompileStatic
class SheetWithSlashesTest {
	@Test
	void testSheetNameWithSlashes() {
		var client = new XivApiClient()
		var singleEntry = client.getById QuestThing, 0
		var entries = client.getListIterator(QuestThing).toList()

		Assertions.assertEquals('Alphinaud has something he wishes to tell you.', singleEntry.unknown4)
		Assertions.assertEquals('Alphinaud has something he wishes to tell you.', entries[0].unknown4)
	}
}
