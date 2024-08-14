package gg.xp.xivapi.mappers.objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.AutoValueMapper;
import gg.xp.xivapi.mappers.FieldMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

// TODO
public class ListFieldMapper<X> implements FieldMapper<List<X>> {

	private static final Logger log = LoggerFactory.getLogger(ListFieldMapper.class);
	private final Class<List<X>> objectType;
	private final AutoValueMapper<X> innerMapper;

	public ListFieldMapper(Class<List<X>> cls, Method method, ObjectMapper mapper) {
		this.objectType = cls;
		if (!cls.equals(List.class)) {
			throw new IllegalArgumentException("Type must be a List<X>, not %s".formatted(cls));
		}
		Type grt = method.getGenericReturnType();
		if (grt instanceof ParameterizedType actualType) {
			//noinspection unchecked
			this.innerMapper = new AutoValueMapper<>((Class<X>) actualType.getRawType(), actualType, mapper);
		}
		else {
			throw new RuntimeException("Unexpected type %s for method %s - not a generic list type".formatted(grt, method));
		}
	}

	public Class<List<X>> getObjectType() {
		return objectType;
	}

	@Override
	public List<X> getValue(JsonNode current, XivApiContext context) {

		if (!current.isArray()) {
			throw new IllegalArgumentException("Expected an array, got %s".formatted(current));
		}
		// TODO: deduplicate this
		List<X> out = new ArrayList<>(current.size());
		for (JsonNode jsonNode : current) {
			out.add(innerMapper.getValue(jsonNode, context));
		}
		return out;
	}

	@Override
	public List<String> getQueryFieldNames() {
		return innerMapper.getQueryFieldNames();
	}

}
