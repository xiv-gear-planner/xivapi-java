package gg.xp.xivapi.test.basictest

import gg.xp.xivapi.XivApiClient
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

@Slf4j
@CompileStatic
class WeaponItemTest {

	static Item item

	@BeforeAll
	static void setup() {
		var client = new XivApiClient()
		def item = client.getById(Item, 42888)
		WeaponItemTest.item = item
		log.info item.toString()
		item.nameSize
		item.metaPropertyValues.each {
			String key = it.name
			String value
			try {
				value = it.value.toString()
			}
			catch (Throwable t) {
				value = "Error: ${t}"
			}
			log.info "$key: $value"
		}

	}

	@Test
	void testQuestExpFactor() {
		Assertions.assertEquals(200, item.classJobUse.unlockQuest.expFactor)
	}

	@Test
	void testQuestPlaceName() {
		Assertions.assertEquals("Limsa Lominsa", item.classJobUse.unlockQuest.placeName.name)
	}

	@Test
	void testItemAction() {
		Assertions.assertEquals(null, item.itemAction)
	}

//	@Test
//	void testId() {
//		Assertions.assertEquals(44096, item.id)
//	}
//
//	@Test
//	void testRowId() {
//		Assertions.assertEquals(44096, item.rowId)
//	}
//
//	@Test
//	void testRowIdAlt() {
//		Assertions.assertEquals(44096, item.rowIdAlt)
//	}
//
//	@Test
//	void testName() {
//		Assertions.assertEquals("Vegetable Soup", item.name)
//	}
//
//	@Test
//	void testPluralName() {
//		Assertions.assertEquals("bowls of vegetable soup", item.pluralName())
//	}
//
//	@Test
//	void testPluralName2() {
//		Assertions.assertEquals("bowls of vegetable soup", item.pluralName)
//	}
//
//	@Test
//	void testRarity() {
//		Assertions.assertEquals(1, item.rarity)
//	}
//
//	@Test
//	void testLevelItem() {
//		Assertions.assertEquals(690, item.levelItem)
//	}
//
//	@Test
//	void testDesynth() {
//		Assertions.assertEquals(false, item.desynth)
//	}
//
//	@Test
//	void testNameSize() {
//		Assertions.assertEquals("Vegetable Soup".length(), item.nameSize)
//	}
//
//	@Test
//	void testItemActionId() {
//		Assertions.assertEquals(2526, item.itemAction.id)
//	}
//
//	@Test
//	void testItemActionCondBattle() {
//		Assertions.assertEquals(true, item.itemAction.condBattle)
//	}
//
//	@Test
//	void testItemActionType() {
//		Assertions.assertEquals(845, item.itemAction.type())
//	}
//
//	@Test
//	void testIconId() {
//		Assertions.assertEquals(24103, item.icon.id)
//	}

}
