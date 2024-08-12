package gg.xp.xivapi.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.annotations.XivApiField;
import gg.xp.xivapi.annotations.XivApiRaw;
import gg.xp.xivapi.clienttypes.XivApiObject;
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

	public StructFieldMapper(Class<X> cls, ObjectMapper mapper) {
		this.objectType = cls;
		for (Method method : cls.getMethods()) {
			// TODO: deduplicate this
			if (method.getDeclaringClass().equals(XivApiObject.class)) {
				// These are handled already
				return;
			}

			// Ignore default methods
			if (method.isDefault()) {
				return;
			}

			Class<?> returnType = method.getReturnType();

			String fieldName;
			XivApiField fieldAnnotation = method.getAnnotation(XivApiField.class);
			if (fieldAnnotation != null) {
				fieldName = fieldAnnotation.value();
			}
			else if (method.getName().startsWith("get")) {
				fieldName = method.getName().substring(3);
			}
			else {
				throw new RuntimeException("I don't know how to map " + method.getName());
			}


			FieldMapper<?> fieldMapper;
			if (method.isAnnotationPresent(XivApiRaw.class)) {
				fieldMapper = new RawFieldMapper<>(fieldName, returnType, mapper);
			}
			else if (XivApiObject.class.isAssignableFrom(returnType)) {
				fieldMapper = new SubObjectFieldMapper<>(fieldName, returnType, mapper);
			}
			else {
				fieldMapper = new FlatFieldMapper<>(fieldName, returnType, mapper);
			}
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
				int id = ((XivApiObject) proxy).getId();
				return "%s(%s)".formatted(objectType.getSimpleName(), id);
			}

			// TODO: strict mode where this is an error
			Object value = methodValueMap.get(method);
			if (value == null)
				if (method.getReturnType().isPrimitive()) {
					log.error("Null primitive field! {}", method.getName());
					return 0;
				}
				else {
					log.error("Null object field! {}", method.getName());
				}

			return value;
		});

	}

	@Override
	public List<String> getQueryFieldNames() {
		return methodFieldMap.values()
				.stream()
				.flatMap(fm -> fm.getQueryFieldNames().stream())
				.toList();
	}

}
