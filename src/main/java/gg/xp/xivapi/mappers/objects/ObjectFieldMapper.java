package gg.xp.xivapi.mappers.objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.annotations.NullIfZero;
import gg.xp.xivapi.annotations.XivApiMetaField;
import gg.xp.xivapi.annotations.XivApiRaw;
import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.exceptions.XivApiDeserializationException;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.FieldMapper;
import gg.xp.xivapi.mappers.getters.MetaFieldMapper;
import gg.xp.xivapi.mappers.getters.NormalFieldMapper;
import gg.xp.xivapi.mappers.getters.RawFieldMapper;
import gg.xp.xivapi.mappers.util.MappingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ObjectFieldMapper<X> implements FieldMapper<X> {

	private static final Logger log = LoggerFactory.getLogger(ObjectFieldMapper.class);
	private final Map<Method, FieldMapper<?>> methodFieldMap = new LinkedHashMap<>();
	private final Class<X> objectType;
	private final Method pkMethod;
	private final Method ridMethod;

	public ObjectFieldMapper(Class<X> cls, ObjectMapper mapper) {
		this.objectType = cls;
		try {
			pkMethod = cls.getMethod("getPrimaryKey");
//			methodFieldMap.put(pkMethod, new MetaFieldMapper<>("value", int.class, pkMethod, mapper));
			ridMethod = cls.getMethod("getRowId");
//			methodFieldMap.put(ridMethod, new MetaFieldMapper<>("row_id", int.class, ridMethod, mapper));
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		for (Method method : cls.getMethods()) {
			if (method.getDeclaringClass().equals(XivApiObject.class)) {
				// These are handled already
				continue;
			}

			// Ignore default methods
			if (method.isDefault()) {
				continue;
			}

			Class<?> returnType = method.getReturnType();

			XivApiMetaField metaFieldAnn = method.getAnnotation(XivApiMetaField.class);
			String fieldName = MappingUtils.getFieldName(method);

			FieldMapper<?> fieldMapper;
			if (method.isAnnotationPresent(XivApiRaw.class)) {
				fieldMapper = new RawFieldMapper<>(fieldName, returnType, method, mapper);
			}
			else if (metaFieldAnn != null) {
				fieldMapper = new MetaFieldMapper<>(fieldName, returnType, method, mapper);
			}
			else {
				fieldMapper = new NormalFieldMapper<>(fieldName, returnType, method, mapper);
			}
			methodFieldMap.put(method, fieldMapper);

		}
	}

	public Class<X> getObjectType() {
		return objectType;
	}

	@Override
	public X getValue(JsonNode current, XivApiContext context) {

		int primaryKey;
		int rowId;
		final Map<Method, Object> methodValueMap = new LinkedHashMap<>();
		try {
			// If this is nested (i.e. it is not the root node), and it has a value of 0 but lacks row_id and/or fields,
			// then it is actually a null.
			boolean isNested = current != context.getRootNode();
			if (isNested) {
				boolean zeroValue = current.get("value").asInt() == 0;
				if (zeroValue
				    && (current.get("row_id") == null
				        || current.get("fields") == null
				        || current.get("fields").isEmpty())) {
					return null;
				}
				if (zeroValue) {
					if (objectType.isAnnotationPresent(NullIfZero.class)) {
						return null;
					}
				}
				// This behavior is different because only nested objects have a 'value'.
				// Top level only has row_id.
				primaryKey = current.get("value").asInt();
				rowId = current.get("row_id").asInt();
			}
			else {
				rowId = primaryKey = current.get("row_id").asInt();
			}


			methodFieldMap.forEach((method, fieldMapper) -> {
				Object value = fieldMapper.getValue(current, context);
				methodValueMap.put(method, value);
			});
		}
		catch (Throwable t) {
			throw new XivApiDeserializationException("Error deserializing %s from '%s'".formatted(objectType, current), t);
		}


		// TODO: validate that every method is either default or has a value

		//noinspection unchecked
		return (X) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{objectType}, (proxy, method, args) -> {
			// Don't override any default methods
			if (method.isDefault()) {
				return InvocationHandler.invokeDefault(proxy, method, args);
			}

			// Custom toString()
			if (method.getName().equals("toString") && method.getParameterCount() == 0) {
				int id = ((XivApiObject) proxy).getPrimaryKey();
				return "%s(%s)".formatted(objectType.getSimpleName(), id);
			}

			if (method.equals(pkMethod)) {
				return primaryKey;
			}
			if (method.equals(ridMethod)) {
				return rowId;
			}

			// TODO: strict mode where this is an error

			Object value = methodValueMap.get(method);
			if (value == null) {
				if (method.getReturnType().isPrimitive()) {
					log.error("Null primitive field! {}", method.getName());
					return 0;
				}
				else {
					if (!method.isAnnotationPresent(NullIfZero.class)
					    || method.getReturnType().isAnnotationPresent(NullIfZero.class)) {
						log.error("Null object field! {}", method.getName());
					}
				}
			}

			if (value instanceof XivApiObject xao) {
				if (xao.getPrimaryKey() == 0 && method.isAnnotationPresent(NullIfZero.class)) {
					return null;
				}
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
