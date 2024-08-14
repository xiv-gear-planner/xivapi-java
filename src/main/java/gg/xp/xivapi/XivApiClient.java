package gg.xp.xivapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.annotations.XivApiSheet;
import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.clienttypes.XivApiSchemaVersion;
import gg.xp.xivapi.clienttypes.XivApiSettings;
import gg.xp.xivapi.mappers.objects.ObjectFieldMapper;
import gg.xp.xivapi.impl.XivApiContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class XivApiClient implements AutoCloseable {
	private static final Logger log = LoggerFactory.getLogger(XivApiClient.class);

	private final ObjectMapper mapper = new ObjectMapper();
	private final XivApiSettings settings;
	private final HttpClient client;

	public XivApiClient(XivApiSettings settings) {
		this.settings = settings;
		// TODO: replace with apache http client
		this.client = HttpClient.newBuilder().build();
	}

	public XivApiClient() {
		this(XivApiSettings.newBuilder().build());
	}

	public <X extends XivApiObject> X getById(final Class<X> cls, final int id) {
		if (!cls.isInterface()) {
			throw new IllegalArgumentException("Argument must be an interface, got %s".formatted(cls));
		}

		XivApiSheet sheetAnn = cls.getAnnotation(XivApiSheet.class);

		if (sheetAnn == null) {
			throw new IllegalArgumentException("Class %s does not have a @XivApiSheet sheetAnn".formatted(cls));
		}

		final String sheetName = sheetAnn.value();

		ObjectFieldMapper<X> mapping = new ObjectFieldMapper<>(cls, mapper);

		final List<String> fields = mapping.getQueryFieldNames();

		final URI uri;
		try {
			uri = new URI("%s/sheet/%s/%d?fields=%s".formatted(settings.getBaseUri(), sheetName, id, String.join(",", fields)));
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		log.info("Constructed URI: {}", uri);

		HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

		String response;
		JsonNode root;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
			root = this.mapper.readTree(response);
		}
		catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}

		XivApiSchemaVersion sv = new XivApiSchemaVersion() {
			@Override
			public String fullVersionString() {
				return root.get("schema").textValue();
			}
		};

		XivApiContext context = new XivApiContext(root, settings, sv);

		return mapping.getValue(root, context);
	}

	public <X extends XivApiObject> List<X> getAll(Class<X> cls) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void close() {
		client.close();
	}
}
