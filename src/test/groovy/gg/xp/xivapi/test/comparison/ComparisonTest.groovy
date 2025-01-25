package gg.xp.xivapi.test.comparison

import gg.xp.xivapi.XivApiClient
import gg.xp.xivapi.clienttypes.XivApiSettings
import gg.xp.xivapi.test.comparison.bar.Item
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

// Test for a particular corner case.
// Because equality is based only on whether the method->value map is equal,
// two objects might be considered equal even if they are from different sheets.
class ComparisonTest {
	@Test
	void comparisonCornerCaseTest() {
		var client = new XivApiClient({ XivApiSettings.Builder it ->
			it.schemaVersion = "exdschema@5f292f39f3deab2c43bee62b202b54ebf51e15b7-2024.08.02.0000.0000"
			it.gameVersion = "7.05"
		})
		var action = client.getById(Item, 5)
		var item = client.getById(gg.xp.xivapi.test.comparison.foo.Item, 5)
		Assertions.assertNotEquals action, item

	}
}
