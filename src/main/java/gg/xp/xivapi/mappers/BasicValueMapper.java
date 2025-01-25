package gg.xp.xivapi.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.annotations.EmptyStringNull;
import gg.xp.xivapi.exceptions.XivApiException;
import gg.xp.xivapi.impl.XivApiContext;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * Mapper for an individual value (numeric, string, boolean)
 *
 * @param <X>
 */
public class BasicValueMapper<X> implements FieldMapper<X> {
	private final Class<X> fieldType;
	private final ObjectMapper mapper;
	private final boolean blankNull;

	public BasicValueMapper(Class<X> fieldType, Method method, ObjectMapper mapper) {
		this.fieldType = fieldType;
		this.mapper = mapper;
		blankNull = method.isAnnotationPresent(EmptyStringNull.class) || method.getAnnotatedReturnType().isAnnotationPresent(EmptyStringNull.class);
		if (blankNull && !fieldType.equals(String.class)) {
			throw new IllegalArgumentException("Method '" + method + "' must have a non-empty String");
		}
	}

	@Override
	public X getValue(JsonNode current, XivApiContext context) {
		try {
			X out = mapper.convertValue(current, fieldType);
			if (blankNull && "".equals(out)) {
				return null;
			}
			return out;
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
