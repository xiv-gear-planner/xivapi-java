package gg.xp.xivapi.impl;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface FieldMapper<X> {
	X getValue(JsonNode current, XivApiContext context);

	List<String> getQueryFieldNames();
}
