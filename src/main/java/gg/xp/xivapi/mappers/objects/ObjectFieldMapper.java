package gg.xp.xivapi.mappers.objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.annotations.NullIfZero;
import gg.xp.xivapi.annotations.XivApiAs;
import gg.xp.xivapi.annotations.XivApiLang;
import gg.xp.xivapi.annotations.XivApiMetaField;
import gg.xp.xivapi.annotations.XivApiRaw;
import gg.xp.xivapi.annotations.XivApiThis;
import gg.xp.xivapi.annotations.XivApiTransientField;
import gg.xp.xivapi.clienttypes.XivApiBase;
import gg.xp.xivapi.clienttypes.XivApiLangValue;
import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.collections.KeyedAlikeMapFactory;
import gg.xp.xivapi.exceptions.XivApiDeserializationException;
import gg.xp.xivapi.exceptions.XivApiException;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.FieldMapper;
import gg.xp.xivapi.mappers.QueryFieldsBuilder;
import gg.xp.xivapi.mappers.getters.MetaFieldMapper;
import gg.xp.xivapi.mappers.getters.NormalFieldMapper;
import gg.xp.xivapi.mappers.getters.ThisFieldMapper;
import gg.xp.xivapi.mappers.getters.TransientFieldMapper;
import gg.xp.xivapi.mappers.util.MappingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Mapper for full sheet objects, both top-level and nested.
 * <p>
 * The general flow is:
 * <ul>
 *     <li>Look at the class and find the common methods (primary key, row id, schema version)</li>
 *     <li>Look at every non-default method and find an appropriate field mapper for it, and store this into a map.</li>
 *     <li>When we want to actually deserialize an object, consult this map and construct a proxy object.</li>
 * </ul>
 *
 * @param <X> The object type
 */
public class ObjectFieldMapper<X> implements FieldMapper<X> {

	private static final Logger log = LoggerFactory.getLogger(ObjectFieldMapper.class);
	private final Map<Method, FieldMapper<?>> methodFieldMap = new LinkedHashMap<>();
	private final Class<X> objectType;
	private final Method pkMethod;
	private final Method ridMethod;
	private final Method svMethod;
	private final Method tsMethod;
	private final KeyedAlikeMapFactory<Method> kaMapFactory;

	public ObjectFieldMapper(Class<X> cls, ObjectMapper mapper) {
		this.objectType = cls;
		try {
			// Common methods
			pkMethod = cls.getMethod("getPrimaryKey");
			ridMethod = cls.getMethod("getRowId");
			svMethod = XivApiBase.class.getMethod("getSchemaVersion");
			tsMethod = Object.class.getMethod("toString");
		}
		catch (NoSuchMethodException e) {
			throw new XivApiException(e);
		}
		for (Method method : cls.getMethods()) {
			if (method.getDeclaringClass().isAssignableFrom(XivApiObject.class)) {
				// The methods declared at the XivApiObject level or higher are the "Common methods" referenced above
				continue;
			}

			// Default methods do not need to be mapped. They will behave as expected.
			if (method.isDefault()) {
				continue;
			}

			Class<?> returnType = method.getReturnType();

			// By default, we want to use NormalFieldMapper, unless there is an annotation to specify otherwise.
			XivApiThis thisAnn = method.getAnnotation(XivApiThis.class);
			XivApiMetaField metaFieldAnn = method.getAnnotation(XivApiMetaField.class);
			XivApiTransientField transientFieldAnn = method.getAnnotation(XivApiTransientField.class);

			@SuppressWarnings("NonConstantStringShouldBeStringBuffer")
			String fieldName = MappingUtils.getFieldName(method);

			FieldMapper<?> fieldMapper;

			if (thisAnn != null) {
				fieldMapper = new ThisFieldMapper<>(transientFieldAnn != null, returnType, method, mapper);
			}
			else if (metaFieldAnn != null) {
				// If it is a meta field (value, row_id, score, etc), use MetaFieldMapper
				fieldMapper = new MetaFieldMapper<>(fieldName, returnType, method, mapper);
			}
			// This reads from multiple fields at this level, so it needs to be here rather than AutoValueMapper.
			else if (returnType.equals(XivApiLangValue.class)) {
				fieldMapper = new LangValueFieldMapper<>(fieldName, transientFieldAnn != null, method, mapper);
			}
			else {
				XivApiLang langAnn = method.getAnnotation(XivApiLang.class);
				// TODO: these should use the decoration query fields
				if (method.isAnnotationPresent(XivApiRaw.class)) {
					fieldName += "@as(raw)";
				}
				XivApiAs genericAs = method.getAnnotation(XivApiAs.class);
				if (genericAs != null) {
					fieldName += "@as(%s)".formatted(genericAs.value());
				}
				if (langAnn != null) {
					fieldName += "@lang(%s)".formatted(langAnn.value());
				}
				if (transientFieldAnn != null) {
					// If transient, map accordingly
					fieldMapper = new TransientFieldMapper<>(fieldName, returnType, method, mapper);
				}
				else {
					// Normal field
					fieldMapper = new NormalFieldMapper<>(fieldName, returnType, method, mapper);
				}
			}
			methodFieldMap.put(method, fieldMapper);
		}
		Set<Method> allMethods = new HashSet<>(methodFieldMap.keySet());
		allMethods.add(pkMethod);
		allMethods.add(ridMethod);
		allMethods.add(svMethod);
		allMethods.add(tsMethod);
		this.kaMapFactory = new KeyedAlikeMapFactory<>(allMethods);
	}

	@Override
	public X getValue(JsonNode current, XivApiContext context) {

		int primaryKey;
		int rowId;
		final Map<Method, Object> methodValueMap = kaMapFactory.create();
		try {
			// If this is nested (i.e. it is not the root node), and it has a value of 0 but lacks row_id and/or fields,
			// then it is actually a null.
			boolean isNested = current != context.rootNode();
			if (isNested) {
				boolean zeroValue = current.get("value").asInt() == 0;
				// TODO: double check this logic
				if (zeroValue
				    && (current.get("row_id") == null
				        || current.get("fields") == null
				        || current.get("fields").isEmpty())) {
					return null;
				}
				// If the interface we are trying to deserialize is annotated with @NullIfZero, then it is okay
				// to return a null if the 'value' (cross-sheet reference) is zero
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

			methodValueMap.put(pkMethod, primaryKey);
			methodValueMap.put(ridMethod, rowId);
			methodValueMap.put(svMethod, context.schemaVersion());
			// TODO: this would work better as some kind of lazy value, as it is not ideal to have to intern every
			// single instance of these to save memory.
			methodValueMap.put(tsMethod, "%s(%s)".formatted(objectType.getSimpleName(), rowId));
			// Go through the method -> field map, deserialize each field into its respective type, and then
			// assemble a method -> value map.
			methodFieldMap.forEach((method, fieldMapper) -> {
				Object value = fieldMapper.getValue(current, context);
				methodValueMap.put(method, value);
			});
		}
		catch (Throwable t) {
			throw new XivApiDeserializationException("Error deserializing %s from '%s'".formatted(objectType, current), t);
		}

		boolean strict = context.settings().isStrict();

		// It is not necessary to use the `strict` flag as part of the cache key, as both the strict flag and the cache
		// itself are both scoped to the context object.
		return context.cache().computeIfAbsent(objectType, methodValueMap, map -> {
			//noinspection unchecked
			return (X) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{objectType}, new ObjectInvocationHandler(map, strict));
		});

	}

	@Override
	public void buildQueryFields(QueryFieldsBuilder parent) {
		for (FieldMapper<?> mapper : methodFieldMap.values()) {
			mapper.buildQueryFields(parent);
		}
	}

}
