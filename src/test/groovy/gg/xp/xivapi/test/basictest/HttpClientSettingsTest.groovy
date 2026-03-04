package gg.xp.xivapi.test.basictest

import gg.xp.xivapi.XivApiClient
import gg.xp.xivapi.clienttypes.XivApiSettings
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.net.http.HttpClient

@CompileStatic
class HttpClientSettingsTest {

	@Test
	@CompileDynamic
	void testCustomHttpClient() {
		HttpClient customClient = HttpClient.newBuilder().build()
		var settings = XivApiSettings.newBuilder()
				.setHttpClient(customClient)
				.build()
		
		Assertions.assertSame(customClient, settings.getHttpClientOverride())
		
		try (var client = new XivApiClient(settings)) {
			// Use reflection to check the private 'client' field
			var field = XivApiClient.getDeclaredField("client")
			field.setAccessible(true)
			var internalClient = field.get(client)
			Assertions.assertSame(customClient, internalClient)
		}
	}

	@Test
	@CompileDynamic
	void testDefaultHttpClient() {
		var settings = XivApiSettings.newBuilder().build()
		Assertions.assertNull(settings.getHttpClientOverride())
		
		try (var client = new XivApiClient(settings)) {
			// Use reflection to check the private 'client' field
			var field = XivApiClient.getDeclaredField("client")
			field.setAccessible(true)
			var internalClient = field.get(client)
			Assertions.assertNotNull(internalClient)
			// It should be a default client, not null
		}
	}
}
