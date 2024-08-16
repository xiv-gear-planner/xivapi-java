package gg.xp.xivapi.mappers.objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.annotations.XivApiField;
import gg.xp.xivapi.clienttypes.XivApiBase;
import gg.xp.xivapi.exceptions.XivApiException;
import gg.xp.xivapi.clienttypes.XivApiStruct;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.FieldMapper;
import gg.xp.xivapi.mappers.QueryField;
import gg.xp.xivapi.mappers.getters.FlatFieldMapper;
import gg.xp.xivapi.mappers.util.MappingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StructFieldMapper<X> implements FieldMapper<X> {

	private static final Logger log = LoggerFactory.getLogger(StructFieldMapper.class);
	private final Map<Method, FieldMapper<?>> methodFieldMap = new LinkedHashMap<>();
	private final Class<X> objectType;
	private final Method svMethod;
	private final Method tsMethod;

	public StructFieldMapper(Class<X> cls, ObjectMapper mapper) {
		this.objectType = cls;
		try {
			svMethod = XivApiBase.class.getMethod("getSchemaVersion");
			tsMethod = Object.class.getMethod("toString");
		}
		catch (NoSuchMethodException e) {
			throw new XivApiException(e);
		}
		for (Method method : cls.getMethods()) {
			if (method.getDeclaringClass().isAssignableFrom(XivApiStruct.class)) {
				// These are handled already
				continue;
			}

			// Ignore default methods
			if (method.isDefault()) {
				continue;
			}

			Class<?> returnType = method.getReturnType();

			String fieldName = MappingUtils.getFieldName(method);

			FieldMapper<?> fieldMapper = new FlatFieldMapper<>(fieldName, returnType, method, mapper);
			methodFieldMap.put(method, fieldMapper);

		}
	}

	@Override
	public X getValue(JsonNode current, XivApiContext context) {

		final Map<Method, Object> methodValueMap = new LinkedHashMap<>();
		methodValueMap.put(svMethod, context.getSchemaVersion());
		methodValueMap.put(tsMethod, "%s(StructProxy)".formatted(objectType.getSimpleName()));
		methodFieldMap.forEach((method, fieldMapper) -> {
			Object value = fieldMapper.getValue(current, context);
			methodValueMap.put(method, value);
		});

		//noinspection unchecked
		return (X) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{objectType}, new StructInvocationHandler(methodValueMap, context.getSettings().isStrict()));
	}

	@Override
	public List<QueryField> getQueryFields() {
		// Xivapi does not support filtering sub-fields in simple structs - only in real sheet objects.
		return List.of();
	}

}
