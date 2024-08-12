package gg.xp.xivapi.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class SubObjectFieldMapper<X> implements FieldMapper<X> {
	private final String fieldName;
	private final Class<X> fieldType;
	private final ObjectMapper mapper;
	private final ApiObjectFieldMapper<X> wrapped;

	public SubObjectFieldMapper(String fieldName, Class<X> fieldType, ObjectMapper mapper) {
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.mapper = mapper;
		this.wrapped = new ApiObjectFieldMapper<>(fieldType, mapper);
	}

	@Override
	public X getValue(JsonNode current, XivApiContext context) {
		return wrapped.getValue(current.get("fields").get(fieldName), context);
	}

	@Override
	public List<String> getQueryFieldNames() {
		return wrapped.getQueryFieldNames().stream().map(item -> "%s.%s".formatted(fieldName, item)).toList();
	}
}
