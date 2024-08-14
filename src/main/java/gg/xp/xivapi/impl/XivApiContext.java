package gg.xp.xivapi.impl;

import com.fasterxml.jackson.databind.JsonNode;
import gg.xp.xivapi.clienttypes.XivApiSchemaVersion;
import gg.xp.xivapi.clienttypes.XivApiSettings;

public class XivApiContext {

	private final JsonNode rootNode;
	private final XivApiSettings settings;
	private final XivApiSchemaVersion schemaVersion;

	public XivApiContext(JsonNode rootNode, XivApiSettings settings, XivApiSchemaVersion schemaVersion) {
		this.rootNode = rootNode;
		this.settings = settings;
		this.schemaVersion = schemaVersion;
	}

	public JsonNode getRootNode() {
		return rootNode;
	}

	public XivApiSettings getSettings() {
		return settings;
	}

	public XivApiSchemaVersion getSchemaVersion() {
		return schemaVersion;
	}
}
