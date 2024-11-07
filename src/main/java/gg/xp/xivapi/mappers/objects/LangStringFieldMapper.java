package gg.xp.xivapi.mappers.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import gg.xp.xivapi.clienttypes.XivApiLangString;
import gg.xp.xivapi.exceptions.XivApiMappingException;
import gg.xp.xivapi.exceptions.XivApiMissingNodeException;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.FieldMapper;
import gg.xp.xivapi.mappers.QueryField;
import gg.xp.xivapi.mappers.util.MappingUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LangStringFieldMapper implements FieldMapper<XivApiLangString> {
	private final Method method;
	private final String fieldName;
	private final boolean isTransient;
	private final Pattern fieldMatcher;
	private final List<String> langs;

	public LangStringFieldMapper(String fieldName, boolean isTransient, Method method) {
		this.fieldName = fieldName;
		this.isTransient = isTransient;
		this.method = method;
		this.fieldMatcher = Pattern.compile(fieldName + "@lang\\(([a-z]+)\\)");
		this.langs = MappingUtils.ALL_LANGS;
	}

	@Override
	public XivApiLangString getValue(JsonNode current, XivApiContext context) {
		if (current == null) {
			throw new XivApiMissingNodeException("'current' is null", null, XivApiLangString.class, method);
		}
		String fieldName = isTransient ? "transient" : "fields";
		var fieldsNode = current.get(fieldName);
		if (fieldsNode == null) {
			throw new XivApiMissingNodeException("'%s' node is missing".formatted(fieldName), current, XivApiLangString.class, method);
		}
		Map<String, String> out = new HashMap<>(4);
		fieldsNode.fields().forEachRemaining(entry -> {
			Matcher matcher = fieldMatcher.matcher(entry.getKey());
			if (matcher.matches()) {
				String lang = matcher.group(1);
				String value = entry.getValue().textValue();
				out.put(lang, value);
			}
		});
		if (out.isEmpty()) {
			throw new XivApiMappingException("There were no matching language nodes for field %s".formatted(this.fieldName));
		}
		return new LangStringImpl(out);
	}

	@Override
	public List<QueryField> getQueryFields() {
		return langs.stream()
				.map(lang -> {
					String field = "%s@lang(%s)".formatted(fieldName, lang);
					return isTransient ? QueryField.transientField(field) : QueryField.normalField(field);
				})
				.toList();
	}

	public static class LangStringSerializer extends JsonSerializer<XivApiLangString> {
		@Override
		public void serialize(XivApiLangString value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException {
			// Start writing at the top level
			for (Map.Entry<String, String> entry : value.getAll().entrySet()) {
				gen.writeObjectField(entry.getKey(), entry.getValue());
			}
		}
	}

	@JsonSerialize(using = LangStringSerializer.class)
	private record LangStringImpl(@JsonIgnore Map<String, String> values) implements XivApiLangString {

		@Override
		public Map<String, String> getAll() {
			return Collections.unmodifiableMap(values);
		}

	}
}
