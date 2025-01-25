package gg.xp.xivapi.test.dedup

import gg.xp.xivapi.XivApiClient
import gg.xp.xivapi.clienttypes.XivApiSettings
import gg.xp.xivapi.filters.SearchFilters
import gg.xp.xivapi.pagination.ListCacheMode
import gg.xp.xivapi.pagination.ListOptions
import gg.xp.xivapi.test.basictest.Item
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@CompileStatic
class DedupTest {
	@Test
	void testDedup() {
		var client = new XivApiClient({ XivApiSettings.Builder it ->
			it.schemaVersion = "exdschema@5f292f39f3deab2c43bee62b202b54ebf51e15b7-2024.08.02.0000.0000"
			it.gameVersion = "7.05"
		})
		List<Item> items = client.getSearchIterator(Item, SearchFilters.eq("ClassJobCategory", 181)).toList()
		// Two sage weapons
		var item1 = items.find { it.rowId == 42587 }
		var item2 = items.find { it.rowId == 42791 }
		// Since they are both for Sage, their classJobCategory should be equal
		Assertions.assertEquals item1.classJobCategory, item2.classJobCategory
		// The purpose of dedup is to replace them with the same underlying object
		Assertions.assertSame item1.classJobCategory, item2.classJobCategory
	}

	@Test
	void testDedupDisabled() {
		var client = new XivApiClient({ XivApiSettings.Builder it ->
			it.schemaVersion = "exdschema@5f292f39f3deab2c43bee62b202b54ebf51e15b7-2024.08.02.0000.0000"
			it.gameVersion = "7.05"
		})
		List<Item> items = client.getSearchIterator(Item, SearchFilters.eq("ClassJobCategory", 181), ListOptions.newBuilder().with {
			listCacheMode(ListCacheMode.None)
			build()
		}).toList()
		// Two sage weapons
		var item1 = items.find { it.rowId == 42587 }
		var item2 = items.find { it.rowId == 42791 }
		// Since they are both for Sage, their classJobCategory should be equal
		Assertions.assertEquals item1.classJobCategory, item2.classJobCategory
		// Dedup disabled, so should not be equal
		Assertions.assertNotSame item1.classJobCategory, item2.classJobCategory
	}
}
