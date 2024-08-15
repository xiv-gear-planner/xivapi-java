package gg.xp.xivapi.test.pagertest

import gg.xp.xivapi.XivApiClient
import gg.xp.xivapi.filters.SearchFilters
import gg.xp.xivapi.pagination.ListOptions
import gg.xp.xivapi.test.basictest.Item
import groovy.transform.CompileStatic
import org.apache.commons.collections4.IteratorUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@CompileStatic
class BasicSearchTest {

	@Test
	void testSearch() {
		// Not going to inspect much in this list, because there's no guarantee of order
		var client = new XivApiClient()
		Iterator<Item> itemsIter = client.getSearchIterator(Item,
				SearchFilters.and("LevelItem>=700", "LevelItem<=710", "ClassJobCategory.WHM=1"),
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
				SearchFilters.and("LevelItem>=700", "LevelItem<=710", "ClassJobCategory.WHM=1"),
				ListOptions.newBuilder().with {
					perPage 10
					build()
				})

		List<Item> dumpedNoStop = IteratorUtils.toList(itemsIterNoStop)

		Iterator<Item> itemsIter = client.getSearchIterator(Item,
				SearchFilters.and("LevelItem>=700", "LevelItem<=710", "ClassJobCategory.WHM=1"),
				ListOptions.<Item>newBuilder().with {
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
