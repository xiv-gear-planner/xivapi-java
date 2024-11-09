package gg.xp.xivapi.mappers.getters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.exceptions.XivApiDeserializationException;
import gg.xp.xivapi.exceptions.XivApiMissingNodeException;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.AutoValueMapper;
import gg.xp.xivapi.mappers.FieldMapper;
import gg.xp.xivapi.mappers.QueryFieldsBuilder;

import java.lang.reflect.Method;
import java.util.List;

public class ThisFieldMapper<X> implements FieldMapper<X> {
	private final Class<X> fieldType;
	private final FieldMapper<X> innerMapper;
	private final Method method;
	private final boolean isTransient;

	public ThisFieldMapper(boolean isTransient, Class<X> fieldType, Method method, ObjectMapper mapper) {
		this.isTransient = isTransient;
		this.fieldType = fieldType;
		this.method = method;
		this.innerMapper = new AutoValueMapper<>(fieldType, method, method.getGenericReturnType(), mapper);
	}

	@Override
	public X getValue(JsonNode current, XivApiContext context) {
		if (current == null) {
			throw new XivApiMissingNodeException("'current' is null", null, fieldType, method);
		}
		String fieldName = isTransient ? "transient" : "fields";
		var fieldsNode = current.get(fieldName);
		if (fieldsNode == null) {
			throw new XivApiMissingNodeException("'%s' node is missing".formatted(fieldName), current, fieldType, method);
		}
		try {
			return innerMapper.getValue(fieldsNode, context);
		}
		catch (Throwable t) {
			throw new XivApiDeserializationException("Error deserializing %s from %s".formatted(method.getName(), current), t);
		}
	}

	@Override
	public void buildQueryFields(QueryFieldsBuilder parent) {
		parent.addChild(QueryFieldsBuilder.all());
	}

}
