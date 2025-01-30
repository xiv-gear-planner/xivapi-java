package gg.xp.xivapi.test.basictest

import gg.xp.xivapi.XivApiClient
import gg.xp.xivapi.clienttypes.XivApiSettings
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@CompileStatic
class ClosingTest {
	private static final String schemaVersion = "exdschema@5f292f39f3deab2c43bee62b202b54ebf51e15b7-2024.08.02.0000.0000"

	// Allows access to private rootMappingCache field
	@CompileDynamic
	@Test
	void closeTest() {
		var client = new XivApiClient({ XivApiSettings.Builder it ->
			it.schemaVersion = schemaVersion
			it.gameVersion = "7.05"
		})
		def item = client.getById(Item, 44096)
		Assertions.assertEquals 1, client.rootMappingCache.size()
		client.close()
		Assertions.assertEquals 0, client.rootMappingCache.size()
	}

	@Test
	void testAutoClose() {
		try (var client = new XivApiClient({ XivApiSettings.Builder it ->
			it.schemaVersion = schemaVersion
			it.gameVersion = "7.05"
		})) {
			def item = client.getById(Item, 44096)
		}
	}
}
