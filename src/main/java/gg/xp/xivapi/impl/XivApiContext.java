package gg.xp.xivapi.impl;

import com.fasterxml.jackson.databind.JsonNode;
import gg.xp.xivapi.clienttypes.XivApiSchemaVersion;
import gg.xp.xivapi.clienttypes.XivApiSettings;
import gg.xp.xivapi.url.XivApiUrlResolver;

/**
 * Top-level deserialization context
 */
public record XivApiContext(JsonNode rootNode, XivApiSettings settings, XivApiSchemaVersion schemaVersion, XivApiUrlResolver urlResolver) {
}
