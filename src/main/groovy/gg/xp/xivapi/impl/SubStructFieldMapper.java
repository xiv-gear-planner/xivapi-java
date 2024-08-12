package gg.xp.xivapi.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class SubStructFieldMapper<X> implements FieldMapper<X> {
	private final String fieldName;
	private final Class<X> fieldType;
	private final ObjectMapper mapper;
	private final StructFieldMapper<X> wrapped;

	public SubStructFieldMapper(String fieldName, Class<X> fieldType, ObjectMapper mapper) {
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.mapper = mapper;
		this.wrapped = new StructFieldMapper<>(fieldType, mapper);
	}

	@Override
	public X getValue(JsonNode current, JsonNode root) {
		return wrapped.getValue(current.get("fields").get(fieldName), root);
	}

	@Override
	public List<String> getQueryFieldNames() {
		return wrapped.getQueryFieldNames().stream().map(item -> "%s.%s".formatted(fieldName, item)).toList();
	}
}
