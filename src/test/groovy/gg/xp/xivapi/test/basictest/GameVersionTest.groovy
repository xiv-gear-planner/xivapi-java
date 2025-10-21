package gg.xp.xivapi.test.basictest

import gg.xp.xivapi.XivApiClient
import gg.xp.xivapi.clienttypes.GameVersion
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@CompileStatic
class GameVersionTest {
	@Test
	void testGameVersions() {
		var client = new XivApiClient()

		List<String> versions = client.gameVersions
		Assertions.assertTrue "7.05" in versions
		Assertions.assertTrue "7.05x1" in versions
		Assertions.assertTrue "latest" in versions

		List<GameVersion> full = client.gameVersionsFull
		Assertions.assertNotNull full.find { it.names().contains("7.05") && it.key() == '9f49c502be94f1cb' }
		Assertions.assertNotNull full.find { it.names().contains("7.05x1") && it.key() == 'cb4adbbad896fd36' }
		Assertions.assertNotNull full.find { it.names().contains("latest") && it.key() != null }

		List<String> keys = client.gameVersionKeys
		Assertions.assertTrue '9f49c502be94f1cb' in keys
		Assertions.assertTrue 'cb4adbbad896fd36' in keys
		Assertions.assertTrue 'd151ba652709a159' in keys
	}
}
