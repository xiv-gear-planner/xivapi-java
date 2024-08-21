package gg.xp.xivapi.test.basictest

import gg.xp.xivapi.XivApiClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GameVersionTest {
	@Test
	void testGameVersions() {
		var client = new XivApiClient()
		List<String> versions = client.gameVersions
		Assertions.assertTrue("7.05" in versions)
		Assertions.assertTrue("7.05x1" in versions)
		Assertions.assertTrue("latest" in versions)
	}
}
