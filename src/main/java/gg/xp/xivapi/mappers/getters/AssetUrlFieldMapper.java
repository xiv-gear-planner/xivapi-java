package gg.xp.xivapi.mappers.getters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.annotations.XivApiAssetPath;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.FieldMapper;
import gg.xp.xivapi.mappers.QueryFieldsBuilder;

import java.lang.reflect.Method;

public class AssetUrlFieldMapper<X> implements FieldMapper<X> {

	private final Class<X> returnType;
	private final ObjectMapper mapper;
	private final String format;

	public AssetUrlFieldMapper(Class<X> returnType, Method method, ObjectMapper mapper) {
		this.returnType = returnType;
		this.mapper = mapper;
		XivApiAssetPath ann = method.getAnnotation(XivApiAssetPath.class);
		if (ann == null) {
			throw new IllegalArgumentException("@XivApiAssetPath annotation is required");
		}
		format = ann.format();
	}

	@Override
	public X getValue(JsonNode current, XivApiContext context) {
		try {
			String raw = current.textValue();
			context.urlResolver().getAssetUri(raw, format);
			return mapper.convertValue(raw, returnType);
		}
		catch (Throwable t) {
			throw new RuntimeException("Error deserializing", t);
		}
	}

	@Override
	public void buildQueryFields(QueryFieldsBuilder parent) {
		// Nothing to do
	}
}
