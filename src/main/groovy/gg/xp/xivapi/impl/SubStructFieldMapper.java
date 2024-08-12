package gg.xp.xivapi.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.IteratorUtils;

import java.util.List;

public class SubStructFieldMapper<X> implements FieldMapper<X> {
	private final String fieldName;
	private final Class<X> fieldType;
	private final ObjectMapper mapper;
	private final StructFieldMapper<X> wrapped;

	public SubStructFieldMapper(String fieldName, Class<X> fieldType, ObjectMapper mapper) {
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.mapper = mapper;
		this.wrapped = new StructFieldMapper<>(fieldType, mapper);
	}

	@Override
	public X getValue(JsonNode current, XivApiContext context) {
		JsonNode child = current.get("fields").get(fieldName);
		if (child == null) {
			throw new IllegalArgumentException("Expected to have child field %s but it does not exist. Actual children: %s"
					.formatted(fieldName, IteratorUtils.toList(current.fieldNames())));
		}
		return wrapped.getValue(child, context);
	}

	@Override
	public List<String> getQueryFieldNames() {
		// Cannot filter to sub-fields in these
		return List.of(fieldName);
	}
}
