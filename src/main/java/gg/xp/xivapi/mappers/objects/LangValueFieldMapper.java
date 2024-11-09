package gg.xp.xivapi.mappers.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import gg.xp.xivapi.clienttypes.XivApiLangValue;
import gg.xp.xivapi.exceptions.XivApiMappingException;
import gg.xp.xivapi.exceptions.XivApiMissingNodeException;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.AutoValueMapper;
import gg.xp.xivapi.mappers.FieldMapper;
import gg.xp.xivapi.mappers.QueryFieldsBuilder;
import gg.xp.xivapi.mappers.util.MappingUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LangValueFieldMapper<X> implements FieldMapper<XivApiLangValue<X>> {
	private final Method method;
	private final String fieldName;
	private final boolean isTransient;
	private final Pattern fieldMatcher;
	private final List<String> langs;
	private final AutoValueMapper<X> innerMapper;
	private final Class<X> innerTypeClass;

	public LangValueFieldMapper(String fieldName, boolean isTransient, Method method, ObjectMapper mapper) {
		this.fieldName = fieldName;
		this.isTransient = isTransient;
		this.method = method;
		this.fieldMatcher = Pattern.compile(fieldName + "@lang\\(([a-z]+)\\)");
		this.langs = MappingUtils.ALL_LANGS;
		ParameterizedType fullType = ((ParameterizedType) method.getGenericReturnType());
		Type innerType = fullType.getActualTypeArguments()[0];
		//noinspection unchecked
		this.innerTypeClass = (Class<X>) MappingUtils.parameterizedTypeToRawClass(innerType);
		this.innerMapper = new AutoValueMapper<>(innerTypeClass, method, innerType, mapper);
	}

	@Override
	public XivApiLangValue<X> getValue(JsonNode current, XivApiContext context) {
		if (current == null) {
			throw new XivApiMissingNodeException("'current' is null", null, XivApiLangValue.class, method);
		}
		String fieldName = isTransient ? "transient" : "fields";
		var fieldsNode = current.get(fieldName);
		if (fieldsNode == null) {
			throw new XivApiMissingNodeException("'%s' node is missing".formatted(fieldName), current, XivApiLangValue.class, method);
		}
		Map<String, X> out = new HashMap<>(4);
		fieldsNode.fields().forEachRemaining(entry -> {
			Matcher matcher = fieldMatcher.matcher(entry.getKey());
			if (matcher.matches()) {
				String lang = matcher.group(1);
				X value = innerMapper.getValue(entry.getValue(), context);
				out.put(lang, value);
			}
		});
		if (out.isEmpty()) {
			throw new XivApiMappingException("There were no matching language nodes for field %s".formatted(this.fieldName));
		}
		return new LangValueImpl<>(Collections.unmodifiableMap(out));
	}

	@Override
	public void buildQueryFields(QueryFieldsBuilder parent) {
		boolean isArray = MappingUtils.isArrayQueryType(innerTypeClass);
		for (String lang : langs) {
			var child = QueryFieldsBuilder.normalField(fieldName);
			if (isArray) {
				child.markAsArray();
			}
			child.addDecorator("lang(%s)".formatted(lang));
			innerMapper.buildQueryFields(child);
			parent.addChild(child);
		}
	}

	public static class LangValueSerializer extends JsonSerializer<XivApiLangValue<?>> {
		@Override
		public void serialize(XivApiLangValue<?> value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException {
			gen.writeObject(value.getAll());
		}
	}

	@JsonSerialize(using = LangValueSerializer.class)
	private record LangValueImpl<X>(@JsonIgnore Map<String, X> values) implements XivApiLangValue<X> {

		@Override
		@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType") // explicitly constructed with unmodifiableMap
		public Map<String, X> getAll() {
			return values;
		}

	}
}
