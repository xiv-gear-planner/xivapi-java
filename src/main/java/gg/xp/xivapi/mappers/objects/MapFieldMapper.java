package gg.xp.xivapi.mappers.objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.annotations.OmitZeroes;
import gg.xp.xivapi.annotations.XivApiMapKeys;
import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.AutoValueMapper;
import gg.xp.xivapi.mappers.FieldMapper;
import gg.xp.xivapi.mappers.QueryFieldsBuilder;
import gg.xp.xivapi.mappers.util.MappingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Mapper for Map types. Keys are always String. Value is generic.
 * <p>
 * TODO: support this at the top-level
 *
 * @param <X> The list type (e.g. List of String, not String)
 */
public class MapFieldMapper<X> implements FieldMapper<Map<String, X>> {

	private static final Logger log = LoggerFactory.getLogger(MapFieldMapper.class);
	private final AutoValueMapper<X> innerMapper;
	private final Class<X> valueType;
	private final boolean omitZero;
	private final Predicate<String> includeKey;

	@SuppressWarnings("unchecked")
	public MapFieldMapper(Class<Map<String, X>> cls, Method method, Type returnTypeFull, ObjectMapper mapper) {
		if (!cls.equals(Map.class)) {
			throw new IllegalArgumentException("Type must be an array, not %s".formatted(cls));
		}
		Type valueTypeFull = ((ParameterizedType) returnTypeFull).getActualTypeArguments()[1];
		this.valueType = (Class<X>) MappingUtils.parameterizedTypeToRawClass(valueTypeFull);
		omitZero = method.isAnnotationPresent(OmitZeroes.class);
		if (omitZero && !XivApiObject.class.isAssignableFrom(valueType)) {
			throw new IllegalArgumentException("@OmitZeroes only makes sense when dealing with a sheet object type");
		}
		this.innerMapper = new AutoValueMapper<>(valueType, method, valueTypeFull, mapper);
		XivApiMapKeys mapKeysAnn = method.getAnnotation(XivApiMapKeys.class);
		if (mapKeysAnn != null) {
			includeKey = Pattern.compile(mapKeysAnn.value()).asMatchPredicate();
		}
		else {
			includeKey = (s) -> true;
		}
	}

	@Override
	public Map<String, X> getValue(JsonNode current, XivApiContext context) {

		if (!current.isObject()) {
			throw new IllegalArgumentException("Expected a json object, got %s".formatted(current));
		}

		Map<String, X> out = new HashMap<>();

		current.fields().forEachRemaining(entry -> {
			String key = entry.getKey();
			if (includeKey.test(key)) {
				X value = innerMapper.getValue(entry.getValue(), context);
				if (omitZero && value instanceof XivApiObject obj && obj.isZero()) {
					return;
				}
				out.put(key, value);
			}
		});
		return out;
	}

	@Override
	public void buildQueryFields(QueryFieldsBuilder parent) {
		innerMapper.buildQueryFields(parent);
	}

}
