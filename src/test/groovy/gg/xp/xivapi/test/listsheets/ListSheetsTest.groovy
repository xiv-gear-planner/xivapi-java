package gg.xp.xivapi.test.listsheets

import gg.xp.xivapi.XivApiClient
import gg.xp.xivapi.clienttypes.SheetMetadata
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@CompileStatic
class ListSheetsTest {
	@Test
	void testListSheets() {
		var client = new XivApiClient()
		List<SheetMetadata> sheets = client.listSheets()
		for (final SheetMetadata sheet in sheets) {
			if (sheet.name() == 'ItemLevel') {
				return
			}
		}
		Assertions.fail "Expected to have a sheet called 'LevelItem'"
	}
}
