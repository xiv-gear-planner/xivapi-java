package gg.xp.xivapi.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

public class FlatFieldMapper<X> implements FieldMapper<X> {
	private final String fieldName;
	private final Class<X> fieldType;
	private final ObjectMapper mapper;

	public FlatFieldMapper(String fieldName, Class<X> fieldType, ObjectMapper mapper) {
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.mapper = mapper;
	}

	@Override
	public X getValue(JsonNode current, XivApiContext context) {
		try {
			var fieldNode = current.get(fieldName);
			return mapper.convertValue(fieldNode, fieldType);
		}
		catch (Throwable t) {
			throw new RuntimeException("Error deserializing %s".formatted(fieldName), t);
		}
	}

	@Override
	public List<String> getQueryFieldNames() {
		return Collections.emptyList();
	}
}
