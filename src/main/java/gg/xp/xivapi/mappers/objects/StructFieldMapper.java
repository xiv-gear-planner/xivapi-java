package gg.xp.xivapi.mappers.objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.annotations.XivApiField;
import gg.xp.xivapi.exceptions.XivApiException;
import gg.xp.xivapi.clienttypes.XivApiStruct;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.FieldMapper;
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

// TODO
public class StructFieldMapper<X> implements FieldMapper<X> {

	private static final Logger log = LoggerFactory.getLogger(StructFieldMapper.class);
	private final Map<Method, FieldMapper<?>> methodFieldMap = new LinkedHashMap<>();
	private final Class<X> objectType;

	public StructFieldMapper(Class<X> cls, ObjectMapper mapper) {
		this.objectType = cls;
		for (Method method : cls.getMethods()) {
			if (method.getDeclaringClass().equals(XivApiStruct.class)) {
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

	public Class<X> getObjectType() {
		return objectType;
	}

	@Override
	public X getValue(JsonNode current, XivApiContext context) {

		// TODO: deduplicate this
		final Map<Method, Object> methodValueMap = new LinkedHashMap<>();
		methodFieldMap.forEach((method, fieldMapper) -> {
			Object value = fieldMapper.getValue(current, context);
			methodValueMap.put(method, value);

		});

		//noinspection unchecked
		return (X) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{objectType}, (proxy, method, args) -> {
			// Don't override any default methods
			if (method.isDefault()) {
				return InvocationHandler.invokeDefault(proxy, method, args);
			}

			// Custom toString()
			if (method.getName().equals("toString") && method.getParameterCount() == 0) {
				return "%s(Proxy)".formatted(objectType.getSimpleName());
			}

			Object value = methodValueMap.get(method);
			if (value == null) {
				if (context.getSettings().isStrict()) {
					throw new XivApiException("Null value for field %s of %s. Node: %s".formatted(method.getName(), objectType.getSimpleName(), current));
				}
				if (method.getReturnType().isPrimitive()) {
					log.error("Null primitive field! {}", method.getName());
					return 0;
				}
				else {
					log.error("Null object field! {}", method.getName());
				}
			}

			return value;
		});

	}

	@Override
	public List<String> getQueryFieldNames() {
		// Xivapi does not support filtering sub-fields in simple structs - only in real sheet objects.
		return List.of();
//		return methodFieldMap.values()
//				.stream()
//				.flatMap(fm -> fm.getQueryFieldNames().stream())
//				.toList();
	}

}
