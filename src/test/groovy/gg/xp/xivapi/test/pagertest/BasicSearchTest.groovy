package gg.xp.xivapi.test.pagertest

import gg.xp.xivapi.XivApiClient
import gg.xp.xivapi.clienttypes.XivApiSettings
import gg.xp.xivapi.pagination.ListOptions
import gg.xp.xivapi.test.basictest.Item
import groovy.transform.CompileStatic
import org.apache.commons.collections4.IteratorUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

import static gg.xp.xivapi.filters.SearchFilters.*

@CompileStatic
class BasicSearchTest {

	private static final String schemaVersion = "exdschema@5f292f39f3deab2c43bee62b202b54ebf51e15b7-2024.08.02.0000.0000"

	@Test
	void testSearch() {
		// Not going to inspect much in this list, because there's no guarantee of order
		var client = new XivApiClient({ XivApiSettings.Builder it ->
			it.schemaVersion = schemaVersion
			it.gameVersion = "7.05"
		})
		Iterator<Item> itemsIter = client.getSearchIterator(Item,
				and(gte("LevelItem", 700), lte("LevelItem", 710), eq("ClassJobCategory.WHM", 1)),
				ListOptions.newBuilder().with {
					perPage 10
					build()
				})

		List<Item> dumped = IteratorUtils.toList(itemsIter)

		int minExpectedSize = 35

		if (dumped.size() < minExpectedSize) {
			Assertions.fail("Items list was too small (${dumped.size()}, expected ${minExpectedSize})")
		}

		Item neoKingdomCane = dumped.find {
			it.rowId == 42701
		}

		if (neoKingdomCane == null) {
			Assertions.fail("Specific item was not found")
		}

		Assertions.assertEquals(neoKingdomCane.name, "Neo Kingdom Cane")
	}


	@Test
	void testSearchStopCondition() {
		// Not going to inspect much in this list, because there's no guarantee of order
		var client = new XivApiClient()
		Iterator<Item> itemsIterNoStop = client.getSearchIterator(Item,
				and(gte("LevelItem", 700), lte("LevelItem", 710), eq("ClassJobCategory.WHM", 1)),
				ListOptions.newBuilder().with {
					perPage 10
					build()
				})

		List<Item> dumpedNoStop = IteratorUtils.toList(itemsIterNoStop)

		Iterator<Item> itemsIter = client.getSearchIterator(Item,
				and(gte("LevelItem", 700), lte("LevelItem", 710), eq("ClassJobCategory.WHM", 1)),
				ListOptions.<Item> newBuilder().with {
					perPage 10
					stopCondition { index, value ->
						value.rowId == 42842
					}
					build()
				})

		List<Item> dumped = IteratorUtils.toList(itemsIter)

		if (dumped.size() >= dumpedNoStop.size()) {
			Assertions.fail("Items list was too large (${dumped.size()}, expected less than ${dumpedNoStop.size()})")
		}

		Item shouldNotFind = dumped.find {
			it.rowId == 42842
		}

		if (shouldNotFind != null) {
			Assertions.fail("Expected to not find the item")
		}
	}
}
