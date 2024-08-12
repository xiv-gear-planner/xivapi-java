package gg.xp.xivapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import gg.xp.xivapi.annotations.XivApiSheet
import gg.xp.xivapi.clienttypes.XivApiObject
import gg.xp.xivapi.impl.ApiObjectFieldMapper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@CompileStatic
@Slf4j
class XivApiClient {

	private final ObjectMapper mapper = new ObjectMapper();


	<X extends XivApiObject> X getById(Class<X> cls, int id) {
		if (!cls.isInterface()) {
			throw new IllegalArgumentException("Argument must be an interface, got ${cls}")
		}
		var annotation = cls.getAnnotation(XivApiSheet)
		if (annotation == null) {
			throw new IllegalArgumentException("Class ${cls} does not have a @XivApiSheet annotation")
		}
		String sheetName = annotation.value()

		var mapping = new ApiObjectFieldMapper<X>(cls, mapper)

		List<String> fields = mapping.queryFieldNames

		URI uri = new URI("https://beta.xivapi.com/api/1/sheet/${sheetName}/${id}?fields=${fields.join(",")}")

		log.info("Constructed URI: ${uri}")

		// TODO: replace with apache http client
		var client = HttpClient.newBuilder().build()
		var request = HttpRequest.newBuilder(uri).GET().build()

		String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body()

		JsonNode root = this.mapper.readTree(response)

		return mapping.getValue(root, root)


	}

	<X extends XivApiObject> List<X> getAll(Class<X> cls) {

	}


}
