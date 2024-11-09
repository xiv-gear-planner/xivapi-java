package gg.xp.xivapi.test.langtest

import gg.xp.xivapi.XivApiClient
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

@Slf4j
@CompileStatic
class TranslatedWeaponItemTest {

	static Item item

	@BeforeAll
	static void setup() {
		var client = new XivApiClient()
		def item = client.getById(Item, 42888)
		TranslatedWeaponItemTest.item = item
		log.info item.toString()
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
	void testBaseParamList() {
		List<BaseParam> baseParams = item.baseParamList.de
		Assertions.assertEquals(6, baseParams.size())
		BaseParam firstParam = baseParams[0]
		Assertions.assertEquals(5, firstParam.rowId)

		Assertions.assertEquals(0, baseParams[4].rowId)
		Assertions.assertEquals(0, baseParams[5].rowId)
	}

	@Test
	void testBaseParamArray() {
		BaseParam[] baseParams = item.baseParamArray.de
		Assertions.assertEquals(6, baseParams.size())
		BaseParam firstParam = baseParams[0]
		Assertions.assertEquals(5, firstParam.rowId)

		Assertions.assertEquals(0, baseParams[4].rowId)
		Assertions.assertEquals(0, baseParams[5].rowId)
	}

	@Test
	void testBaseParamListTrunc() {
		List<BaseParam> baseParams = item.baseParamListTrunc.de
		Assertions.assertEquals(4, baseParams.size())
		BaseParam firstParam = baseParams[0]
		Assertions.assertEquals(5, firstParam.rowId)

		Assertions.assertTrue(baseParams.every {it.rowId != 0})
	}

	@Test
	void testBaseParamListTrunc2() {
		List<BaseParam> baseParams = item.baseParamListTrunc2.de
		Assertions.assertEquals(4, baseParams.size())
		BaseParam firstParam = baseParams[0]
		Assertions.assertEquals(5, firstParam.rowId)

		Assertions.assertTrue(baseParams.every {it.rowId != 0})
	}

	@Test
	void testBaseParamArrayTrunc() {
		BaseParam[] baseParams = item.baseParamArrayTrunc.de
		Assertions.assertEquals(4, baseParams.size())
		BaseParam firstParam = baseParams[0]
		Assertions.assertEquals(5, firstParam.rowId)

		Assertions.assertTrue(baseParams.every {it.rowId != 0})
	}

}
