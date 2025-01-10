package gg.xp.xivapi.test.basictest

import gg.xp.xivapi.XivApiClient
import gg.xp.xivapi.assets.ImageFormat
import gg.xp.xivapi.clienttypes.XivApiSettings
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import static gg.xp.xivapi.test.testutils.TestUtils.serializeAndDeserialize

@Slf4j
@CompileStatic
class SerDeTest {

	private static final String schemaVersion = "exdschema@5f292f39f3deab2c43bee62b202b54ebf51e15b7-2024.08.02.0000.0000"
	static XivApiClient client
	static Item item

	@BeforeAll
	static void setup() {
		var client = new XivApiClient({ XivApiSettings.Builder it ->
			it.schemaVersion = schemaVersion
			it.gameVersion = "7.05"
		})
		SerDeTest.client = client
		def item = client.getById(Item, 44096)
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
		log.info "${item.methodValueMap}"
		SerDeTest.item = serializeAndDeserialize item
	}


	@Test
	void testId() {
		Assertions.assertEquals(44096, item.primaryKey)
	}

	@Test
	void testRowId() {
		Assertions.assertEquals(44096, item.rowId)
	}

	@Test
	void testToString() {
		Assertions.assertEquals("Item(44096)", item.toString())
	}

	@Test
	void testSchemaVersion() {
		Assertions.assertEquals(schemaVersion, item.schemaVersion.fullVersionString())
	}

	@Test
	void testRowIdAlt() {
		Assertions.assertEquals(44096, item.rowIdAlt)
	}

	@Test
	void testName() {
		Assertions.assertEquals("Vegetable Soup", item.name)
	}

	@Test
	void testPluralName() {
		Assertions.assertEquals("bowls of vegetable soup", item.pluralName())
	}

	@Test
	void testPluralName2() {
		Assertions.assertEquals("bowls of vegetable soup", item.pluralName)
	}

	@Test
	void testRarity() {
		Assertions.assertEquals(1, item.rarity)
	}

	@Test
	void testLevelItem() {
		Assertions.assertEquals(690, item.levelItem)
	}

	@Test
	void testDesynth() {
		Assertions.assertEquals(false, item.desynth)
	}

	@Test
	void testNameSize() {
		Assertions.assertEquals("Vegetable Soup".length(), item.nameSize)
	}

	@Test
	void testItemActionId() {
		Assertions.assertEquals(2526, item.itemAction.primaryKey)
	}

	@Test
	void testItemActionCondBattle() {
		Assertions.assertEquals(true, item.itemAction.condBattle)
	}

	@Test
	void testItemActionType() {
		Assertions.assertEquals(845, item.itemAction.type())
	}

	@Test
	void testIconId() {
		Assertions.assertEquals(24103, item.icon.id)
	}

	@Test
	void testIconUrl() {
		Assertions.assertEquals(new URI("https://beta.xivapi.com/api/1/asset/ui/icon/024000/024103.tex?format=png"), item.icon.pngIconUrl)
	}

	@Test
	void testIconAssetUrl() {
		Assertions.assertEquals(new URI("https://beta.xivapi.com/api/1/asset/ui/icon/024000/024103_hr1.tex?format=png"), item.icon.assetPathHD.getURI(ImageFormat.PNG))
	}

	@Test
	void testIconAssetUrlAsUri() {
		Assertions.assertEquals(new URI("https://beta.xivapi.com/api/1/asset/ui/icon/024000/024103_hr1.tex?format=jpg"), item.icon.assetPathHDasURI)
	}

	@Test
	void testIconSchemaVersion() {
		Assertions.assertEquals(schemaVersion, item.icon.schemaVersion.fullVersionString())
	}

	@Test
	void testClassJobCategory() {
		Assertions.assertEquals(null, item.classJobCategory)
	}

	@Test
	void testClassJobCategoryNotNull() {
		Assertions.assertEquals(0, item.classJobCategoryNotNull.rowId)
	}

	@Test
	void testClassJobCategorySchemaVersion() {
		Assertions.assertEquals(schemaVersion, item.classJobCategoryNotNull.schemaVersion.fullVersionString())
	}

	@Test
	void testDefaultJavaMethods() {
		log.info("Start")
		synchronized (item) {
			log.info("2")
			item.notifyAll()
			log.info("3")
		}
		log.info("4")
		item.hashCode()
		Assertions.assertEquals(item, item)
		Assertions.assertEquals(item.hashCode(), item.hashCode())
		Assertions.assertFalse(item == null)
	}

	@Test
	void testEquality() {

		Item sameItem = client.getById(Item, 44096)

		Assertions.assertEquals(sameItem, item)
		Assertions.assertEquals(sameItem.hashCode(), item.hashCode())

		Item differentItem = client.getById(Item, 43333)

		Assertions.assertNotEquals(differentItem, item)
		Assertions.assertNotEquals(differentItem.hashCode(), item.hashCode())
	}

}
