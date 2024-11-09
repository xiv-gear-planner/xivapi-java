package gg.xp.xivapi.mappers.getters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.exceptions.XivApiDeserializationException;
import gg.xp.xivapi.exceptions.XivApiMissingNodeException;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.AutoValueMapper;
import gg.xp.xivapi.mappers.FieldMapper;
import gg.xp.xivapi.mappers.QueryFieldsBuilder;
import gg.xp.xivapi.mappers.QueryFieldType;
import gg.xp.xivapi.mappers.util.MappingUtils;

import java.lang.reflect.Method;
import java.util.List;

public class TransientFieldMapper<X> implements FieldMapper<X> {
	private final String fieldName;
	private final Class<X> fieldType;
	private final ObjectMapper mapper;
	private final FieldMapper<X> innerMapper;
	private final Method method;

	public TransientFieldMapper(String fieldName, Class<X> fieldType, Method method, ObjectMapper mapper) {
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.mapper = mapper;
		this.method = method;
		this.innerMapper = new AutoValueMapper<>(fieldType, method, method.getGenericReturnType(), mapper);
	}

	@Override
	public X getValue(JsonNode current, XivApiContext context) {
		if (current == null) {
			throw new XivApiMissingNodeException("'current' is null", null, fieldType, method);
		}
		var transientFieldsNode = current.get("transient");
		if (transientFieldsNode == null) {
			throw new XivApiMissingNodeException("'transient' node is missing", current, fieldType, method);
		}
		var fieldNode = transientFieldsNode.get(fieldName);
		if (fieldNode == null) {
			throw new XivApiMissingNodeException("Missing transient field node for field " + fieldName, transientFieldsNode, fieldType, method);
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
		child.markAsTransient();
		innerMapper.buildQueryFields(child);
		parent.addChild(child);
	}

}
