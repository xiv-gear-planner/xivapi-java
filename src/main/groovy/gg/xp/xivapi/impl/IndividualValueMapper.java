package gg.xp.xivapi.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.clienttypes.XivApiException;

import java.lang.reflect.Field;
import java.util.List;

public class IndividualValueMapper<X> implements FieldMapper<X> {
	private final Class<X> fieldType;
	private final Field field;
	private final ObjectMapper mapper;

	public IndividualValueMapper(Class<X> fieldType, Field field, ObjectMapper mapper) {
		this.fieldType = fieldType;
		this.field = field;
		this.mapper = mapper;
	}

	@Override
	public X getValue(JsonNode current, XivApiContext context) {
		try {
			return mapper.convertValue(current, fieldType);
		}
		catch (Throwable t) {
			throw new XivApiException("Error deserializing value %s into %s".formatted(current, fieldType), t);
		}
	}

	@Override
	public List<String> getQueryFieldNames() {
		return List.of();
	}
}
