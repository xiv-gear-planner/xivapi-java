package gg.xp.xivapi.mappers.getters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.FieldMapper;
import gg.xp.xivapi.mappers.QueryField;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class MetaFieldMapper<X> implements FieldMapper<X> {
	private final String metaFieldName;
	private final Class<X> fieldType;
	private final ObjectMapper mapper;

	public MetaFieldMapper(String metaFieldName, Class<X> fieldType, Method method, ObjectMapper mapper) {
		this.metaFieldName = metaFieldName;
		this.fieldType = fieldType;
		this.mapper = mapper;
	}

	@Override
	public X getValue(JsonNode current, XivApiContext context) {
		try {
			// TODO: clean up error handling when 'current' is null
			var fieldNode = current.get(metaFieldName);
			return mapper.convertValue(fieldNode, fieldType);
		}
		catch (Throwable t) {
			throw new RuntimeException("Error deserializing %s".formatted(metaFieldName), t);
		}
	}

	@Override
	public List<QueryField> getQueryFields() {
		// These are not filterable, so return nothing
		return List.of();
	}
}
