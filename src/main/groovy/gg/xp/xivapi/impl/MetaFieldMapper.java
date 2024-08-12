package gg.xp.xivapi.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

public class MetaFieldMapper<X> implements FieldMapper<X> {
	private final String metaFieldName;
	private final Class<X> fieldType;
	private final ObjectMapper mapper;

	public MetaFieldMapper(String metaFieldName, Class<X> fieldType, ObjectMapper mapper) {
		this.metaFieldName = metaFieldName;
		this.fieldType = fieldType;
		this.mapper = mapper;
	}

	@Override
	public X getValue(JsonNode current, JsonNode root) {
		try {
			var fieldNode = current.get(metaFieldName);
			return mapper.convertValue(fieldNode, fieldType);
		}
		catch (Throwable t) {
			throw new RuntimeException("Error deserializing %s".formatted(metaFieldName), t);
		}
	}

	@Override
	public List<String> getQueryFieldNames() {
		return Collections.emptyList();
	}
}
