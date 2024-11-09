package gg.xp.xivapi.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.clienttypes.XivApiStruct;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.objects.ArrayFieldMapper;
import gg.xp.xivapi.mappers.objects.ListFieldMapper;
import gg.xp.xivapi.mappers.objects.MapFieldMapper;
import gg.xp.xivapi.mappers.objects.ObjectFieldMapper;
import gg.xp.xivapi.mappers.objects.StructFieldMapper;
import gg.xp.xivapi.mappers.util.MappingUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Converter that takes a JSON node and a method, and determines the next converter in the chain.
 *
 * @param <X>
 */
public class AutoValueMapper<X> implements FieldMapper<X> {

	private final FieldMapper<X> innerMapper;

	@SuppressWarnings({"unchecked", "rawtypes"})
	public AutoValueMapper(Class<X> returnType, Method method, Type returnTypeFull, ObjectMapper mapper) {
		if (XivApiObject.class.isAssignableFrom(returnType)) {
			// TODO: split ObjectFieldMapper into top-level and nested?
			innerMapper = new ObjectFieldMapper<>(returnType, mapper);
		}
		else if (XivApiStruct.class.isAssignableFrom(returnType)) {
			innerMapper = new StructFieldMapper<>(returnType, mapper);
		}
		else if (MappingUtils.isArrayQueryType(returnType)) {
			if (returnType.equals(List.class)) {
				innerMapper = new ListFieldMapper(returnType, method, returnTypeFull, mapper);
			}
			else {
				innerMapper = new ArrayFieldMapper(returnType, method, returnTypeFull, mapper);
			}
		}
		else if (returnType.equals(Map.class)) {
			innerMapper = new MapFieldMapper(returnType, method, returnTypeFull, mapper);
		}
		else {
			innerMapper = new BasicValueMapper<>(returnType, method, mapper);
		}
	}

	@Override
	public X getValue(JsonNode current, XivApiContext context) {
		return innerMapper.getValue(current, context);
	}

	@Override
	public void buildQueryFields(QueryFieldsBuilder parent) {
		innerMapper.buildQueryFields(parent);
	}

}
