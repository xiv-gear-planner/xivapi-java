package gg.xp.xivapi.test.nestedstruct

import gg.xp.xivapi.XivApiClient
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

/**
 * Test SpecialShop. This tests the ability to have a List<? extends XivApiStruct>
 */
@CompileStatic
class SpecialShopTest {
	@Test
	void testSpecialShop() {
		var client = new XivApiClient()
		SpecialShop shop = client.getById SpecialShop, 1769596
		assertEquals 1769596, shop.rowId

		SpecialShopItem anItem = shop.item[9]
		assertEquals 11869, anItem.item[0].rowId
		assertEquals "Fabled Ring of Healing", anItem.item[0].name
		assertEquals 1, anItem.receiveCount[0]
		assertEquals "", anItem.achievementUnlock.name
	}
}
