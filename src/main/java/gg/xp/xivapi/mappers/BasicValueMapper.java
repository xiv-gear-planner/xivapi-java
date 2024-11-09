package gg.xp.xivapi.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.exceptions.XivApiException;
import gg.xp.xivapi.impl.XivApiContext;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Mapper for an individual value (numeric, string, boolean)
 *
 * @param <X>
 */
public class BasicValueMapper<X> implements FieldMapper<X> {
	private final Class<X> fieldType;
	private final ObjectMapper mapper;

	public BasicValueMapper(Class<X> fieldType, Method method, ObjectMapper mapper) {
		this.fieldType = fieldType;
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
	public void buildQueryFields(QueryFieldsBuilder parent) {
		// Nothing to do
	}
}
