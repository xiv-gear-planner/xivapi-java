package gg.xp.xivapi.mappers.getters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.exceptions.XivApiDeserializationException;
import gg.xp.xivapi.exceptions.XivApiMissingNodeException;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.AutoValueMapper;
import gg.xp.xivapi.mappers.FieldMapper;
import gg.xp.xivapi.mappers.QueryFieldsBuilder;
import gg.xp.xivapi.mappers.util.MappingUtils;

import java.lang.reflect.Method;

public class NormalFieldMapper<X> implements FieldMapper<X> {
	private final String fieldName;
	private final Class<X> fieldType;
	private final FieldMapper<X> innerMapper;
	private final Method method;

	public NormalFieldMapper(String fieldName, Class<X> fieldType, Method method, ObjectMapper mapper) {
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.method = method;
		this.innerMapper = new AutoValueMapper<>(fieldType, method, method.getGenericReturnType(), mapper);
	}

	@Override
	public X getValue(JsonNode current, XivApiContext context) {
		// TODO: add this level of error checking to the rest of them
		if (current == null) {
			throw new XivApiMissingNodeException("'current' is null", null, fieldType, method);
		}
		var fieldsNode = current.get("fields");
		if (fieldsNode == null) {
			throw new XivApiMissingNodeException("'fields' node is missing", current, fieldType, method);
		}
		var fieldNode = fieldsNode.get(fieldName);
		if (fieldNode == null) {
			throw new XivApiMissingNodeException("Missing field node for field " + fieldName, fieldsNode, fieldType, method);
		}
		try {
			return innerMapper.getValue(fieldNode, context);
		}
		catch (Throwable t) {
			throw new XivApiDeserializationException("Error deserializing %s from %s".formatted(fieldName, current), t);
		}
	}

	@Override
	public void buildQueryFields(QueryFieldsBuilder parent) {
		var child = QueryFieldsBuilder.normalField(fieldName);
		boolean isArray = MappingUtils.isArrayQueryType(fieldType);
		if (isArray) {
			child.markAsArray();
		}
		innerMapper.buildQueryFields(child);
		parent.addChild(child);
	}

}
