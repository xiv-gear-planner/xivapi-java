package gg.xp.xivapi.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.clienttypes.XivApiStruct;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.objects.ObjectFieldMapper;
import gg.xp.xivapi.mappers.objects.StructFieldMapper;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Converter that takes a JSON node and a method, and determines the next converter in the chain.
 *
 * @param <X>
 */
public class AutoValueMapper<X> implements FieldMapper<X> {

	private final FieldMapper<X> innerMapper;

	public AutoValueMapper(Class<X> returnType, Type returnTypeFull, ObjectMapper mapper) {
		if (XivApiObject.class.isAssignableFrom(returnType)) {
			innerMapper = new ObjectFieldMapper<>(returnType, mapper);
		}
		else if (XivApiStruct.class.isAssignableFrom(returnType)) {
			innerMapper = new StructFieldMapper<>(returnType, mapper);
		}
		else {
			innerMapper = new BasicValueMapper<>(returnType, mapper);
		}
	}

	@Override
	public X getValue(JsonNode current, XivApiContext context) {
		return innerMapper.getValue(current, context);
	}

	@Override
	public List<String> getQueryFieldNames() {
		return innerMapper.getQueryFieldNames();
	}

}
