package gg.xp.xivapi.mappers.getters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.AutoValueMapper;
import gg.xp.xivapi.mappers.FieldMapper;
import gg.xp.xivapi.mappers.QueryFieldsBuilder;

import java.lang.reflect.Method;
import java.util.List;

public class FlatFieldMapper<X> implements FieldMapper<X> {
	private final String fieldName;
	private final AutoValueMapper<X> innerMapper;

	public FlatFieldMapper(String fieldName, Class<X> fieldType, Method method, ObjectMapper mapper) {
		this.fieldName = fieldName;
		this.innerMapper = new AutoValueMapper<>(fieldType, method, method.getGenericReturnType(), mapper);
	}

	@Override
	public X getValue(JsonNode current, XivApiContext context) {
		try {
			var fieldNode = current.get(fieldName);
			return innerMapper.getValue(fieldNode, context);
		}
		catch (Throwable t) {
			throw new RuntimeException("Error deserializing %s".formatted(fieldName), t);
		}
	}

	@Override
	public void buildQueryFields(QueryFieldsBuilder parent) {
		// Nothing to do
	}
}
