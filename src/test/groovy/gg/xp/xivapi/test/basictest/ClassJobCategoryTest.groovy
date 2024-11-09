package gg.xp.xivapi.test.basictest

import gg.xp.xivapi.XivApiClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ClassJobCategoryTest {

	/**
	 * Standalone ClassJobCategory test to check @XivApiThis functionality at the top level
	 */
	@Test
	void testClassJobCatgory() {
		var client = new XivApiClient()
		def cjc = client.getById(ClassJobCategory, 181)

		Assertions.assertEquals(181, cjc.rowId)
		Assertions.assertTrue(cjc.jobs['SGE'])
		Assertions.assertFalse(cjc.jobs['NIN'])

		Assertions.assertEquals(true, cjc.jobsFull['SGE'])
		Assertions.assertEquals(false, cjc.jobsFull['NIN'])
		Assertions.assertEquals('SGE', cjc.jobsFull['Name'])

	}

}
