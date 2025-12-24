package gg.xp.xivapi.test.autounwrapvalue

import gg.xp.xivapi.XivApiClient
import gg.xp.xivapi.clienttypes.XivApiObject
import gg.xp.xivapi.clienttypes.XivApiSettings
import gg.xp.xivapi.exceptions.XivApiDeserializationException
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@CompileStatic
class AutoUnwrapValueTest {

	private static final String schemaVersion = "exdschema@5f292f39f3deab2c43bee62b202b54ebf51e15b7-2024.08.02.0000.0000"

	interface Item extends XivApiObject {
		List<Integer> getBaseParam();
	}

	@Test
	void autoUnwrapEnabledTest() {
		XivApiClient client = new XivApiClient({ XivApiSettings.Builder it ->
			it.autoUnwrapValue = true
			it.schemaVersion = schemaVersion
			it.gameVersion = "7.05"
		})
		Item item = client.getById Item, 24856
		// Even though there are other fields in BaseParam, that's fine - we only want the "value" field.
		Assertions.assertEquals 5, item.baseParam[0]
	}

	@Test
	void autoUnwrapDisabledTest() {
		XivApiClient client = new XivApiClient({ XivApiSettings.Builder it ->
			it.autoUnwrapValue = false
			it.schemaVersion = schemaVersion
			it.gameVersion = "7.05"
		})
		Assertions.assertThrows XivApiDeserializationException, {
			client.getById Item, 24856
		}
	}
}
