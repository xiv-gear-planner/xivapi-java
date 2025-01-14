package gg.xp.xivapi.mappers.getters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.exceptions.XivApiDeserializationException;
import gg.xp.xivapi.exceptions.XivApiMappingException;
import gg.xp.xivapi.exceptions.XivApiMissingNodeException;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.FieldMapper;
import gg.xp.xivapi.mappers.QueryFieldsBuilder;

import java.lang.reflect.Method;
import java.util.List;

public class MetaFieldMapper<X> implements FieldMapper<X> {
	private final String metaFieldName;
	private final Class<X> fieldType;
	private final Method method;
	private final ObjectMapper mapper;

	public MetaFieldMapper(String metaFieldName, Class<X> fieldType, Method method, ObjectMapper mapper) {
		this.metaFieldName = metaFieldName;
		this.fieldType = fieldType;
		this.method = method;
		this.mapper = mapper;
	}

	@Override
	public X getValue(JsonNode current, XivApiContext context) {
		if (current == null) {
			throw new XivApiMissingNodeException("'current' is null", null, fieldType, method);
		}
		try {
			var fieldNode = current.get(metaFieldName);
			return mapper.convertValue(fieldNode, fieldType);
		}
		catch (Throwable t) {
			throw new XivApiDeserializationException("Error deserializing %s".formatted(metaFieldName), t);
		}
	}

	@Override
	public void buildQueryFields(QueryFieldsBuilder parent) {
		// Nothing to do
	}
}
