package gg.xp.xivapi.test.pagertest

import gg.xp.xivapi.XivApiClient
import gg.xp.xivapi.pagination.ListOptions
import groovy.transform.CompileStatic
import org.apache.commons.collections4.IteratorUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@CompileStatic
class BasicListTest {

	// This also tests transients
	@Test
	void testList() {
		var client = new XivApiClient()
		Iterator<AozAction> actions = client.getListIterator(AozAction)

		List<AozAction> dumped = IteratorUtils.toList(actions)

		int minExpectedSize = 125

		if (dumped.size() < minExpectedSize) {
			Assertions.fail("AozActoin list was too small (${dumped.size()}, expected ${minExpectedSize})")
		}

		Assertions.assertEquals(0, dumped[0].rowId)
		Assertions.assertEquals("", dumped[0].action.name)

		Assertions.assertEquals(1, dumped[1].rowId)
		Assertions.assertEquals("Snort", dumped[1].action.name)

		Assertions.assertEquals(5, dumped[5].rowId)
		Assertions.assertEquals("High Voltage", dumped[5].action.name)
		Assertions.assertEquals(72205, dumped[5].icon.id)
		Assertions.assertEquals(93, dumped[5].location)
		Assertions.assertTrue(dumped[5].description().startsWith("An industrial form of machina-based"))


		Assertions.assertEquals(98, dumped[98].rowId)

		Assertions.assertEquals(99, dumped[99].rowId)
		Assertions.assertEquals("Choco Meteor", dumped[99].action.name)

		Assertions.assertEquals(100, dumped[100].rowId)
		Assertions.assertEquals("Matra Magic", dumped[100].action.name)

		Assertions.assertEquals(101, dumped[101].rowId)
		Assertions.assertEquals("Peripheral Synthesis", dumped[101].action.name)

		Assertions.assertEquals(119, dumped[119].rowId)
		Assertions.assertEquals("Laser Eye", dumped[119].action.name)

		Assertions.assertEquals(124, dumped[124].rowId)
		Assertions.assertEquals("Being Mortal", dumped[124].action.name)
	}


	@Test
	void testStopCondition() {
		var client = new XivApiClient()
		Iterator<AozAction> actions = client.getListIterator(AozAction,
				ListOptions.<AozAction> newBuilder().with {
					stopCondition { index, item ->
						item.action.name == "Laser Eye"
					}
					perPage 100
					build()
				}
		)

		List<AozAction> dumped = IteratorUtils.toList(actions)

		Assertions.assertEquals(0, dumped[0].rowId)
		Assertions.assertEquals("", dumped[0].action.name)

		Assertions.assertEquals(1, dumped[1].rowId)
		Assertions.assertEquals("Snort", dumped[1].action.name)

		Assertions.assertEquals(98, dumped[98].rowId)

		Assertions.assertEquals(99, dumped[99].rowId)
		Assertions.assertEquals("Choco Meteor", dumped[99].action.name)

		Assertions.assertEquals(100, dumped[100].rowId)
		Assertions.assertEquals("Matra Magic", dumped[100].action.name)

		Assertions.assertEquals(101, dumped[101].rowId)
		Assertions.assertEquals("Peripheral Synthesis", dumped[101].action.name)

//		Assertions.assertEquals(119, dumped[119].rowId)
//		Assertions.assertEquals("Laser Eye", dumped[119].action.name)

		Assertions.assertEquals(119, dumped.size())
	}
}
