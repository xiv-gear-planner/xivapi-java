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

	@Test
	void testBaseParamRawList() {
		Assertions.assertEquals([5, 3, 27, 44, 0, 0], item.baseParamRawList)
	}

	@Test
	void testBaseParamRawArray() {
		Assertions.assertEquals([5, 3, 27, 44, 0, 0], item.baseParamRawArray.toList())
	}

	@Test
	void testBaseParamList() {
		List<BaseParam> baseParams = item.baseParamList
		Assertions.assertEquals(6, item.baseParamList.size())
		BaseParam firstParam = baseParams[0]
		Assertions.assertEquals(5, firstParam.rowId)

		Assertions.assertEquals([90, 70, 70, 70, 70, 70, 100, 70, 100, 100, 70, 70, 70], firstParam.meldParams)

		Assertions.assertEquals(0, baseParams[4].rowId)
		Assertions.assertEquals(0, baseParams[5].rowId)
	}

	@Test
	void testBaseParamArray() {
		BaseParam[] baseParams = item.baseParamArray
		Assertions.assertEquals(6, item.baseParamArray.size())
		BaseParam firstParam = baseParams[0]
		Assertions.assertEquals(5, firstParam.rowId)

		Assertions.assertEquals([90, 70, 70, 70, 70, 70, 100, 70, 100, 100, 70, 70, 70], firstParam.meldParams)

		Assertions.assertEquals(0, baseParams[4].rowId)
		Assertions.assertEquals(0, baseParams[5].rowId)
	}

	@Test
	void testBaseParamListTrunc() {
		List<BaseParam> baseParams = item.baseParamListTrunc
		Assertions.assertEquals(4, baseParams.size())
		BaseParam firstParam = baseParams[0]
		Assertions.assertEquals(5, firstParam.rowId)

		Assertions.assertEquals([90, 70, 70, 70, 70, 70, 100, 70, 100, 100, 70, 70, 70], firstParam.meldParams)

		Assertions.assertTrue(baseParams.every {it.rowId != 0})
	}

	@Test
	void testBaseParamListTrunc2() {
		List<BaseParam> baseParams = item.baseParamListTrunc2
		Assertions.assertEquals(4, baseParams.size())
		BaseParam firstParam = baseParams[0]
		Assertions.assertEquals(5, firstParam.rowId)

		Assertions.assertEquals([90, 70, 70, 70, 70, 70, 100, 70, 100, 100, 70, 70, 70], firstParam.meldParams)

		Assertions.assertTrue(baseParams.every {it.rowId != 0})
	}

	@Test
	void testBaseParamArrayTrunc() {
		BaseParam[] baseParams = item.baseParamArrayTrunc
		Assertions.assertEquals(4, baseParams.size())
		BaseParam firstParam = baseParams[0]
		Assertions.assertEquals(5, firstParam.rowId)

		Assertions.assertEquals([90, 70, 70, 70, 70, 70, 100, 70, 100, 100, 70, 70, 70], firstParam.meldParams)

		Assertions.assertTrue(baseParams.every {it.rowId != 0})
	}

	@Test
	void testClassJobCategory() {
		Assertions.assertTrue(item.classJobCategory.jobs["SGE"])
		Assertions.assertFalse(item.classJobCategory.jobs["WHM"])
		Assertions.assertNull(item.classJobCategory.jobs["Name"])

		Assertions.assertEquals(true, item.classJobCategory.jobsFull["SGE"])
		Assertions.assertEquals(false, item.classJobCategory.jobsFull["WHM"])
		Assertions.assertEquals("SGE", item.classJobCategory.jobsFull["Name"])
	}

}
