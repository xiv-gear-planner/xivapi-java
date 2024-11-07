package gg.xp.xivapi.mappers.objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.annotations.NullIfZero;
import gg.xp.xivapi.annotations.XivApiLang;
import gg.xp.xivapi.annotations.XivApiMetaField;
import gg.xp.xivapi.annotations.XivApiRaw;
import gg.xp.xivapi.annotations.XivApiThis;
import gg.xp.xivapi.annotations.XivApiTransientField;
import gg.xp.xivapi.clienttypes.XivApiLangString;
import gg.xp.xivapi.clienttypes.XivApiBase;
import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.exceptions.XivApiDeserializationException;
import gg.xp.xivapi.exceptions.XivApiException;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.FieldMapper;
import gg.xp.xivapi.mappers.QueryField;
import gg.xp.xivapi.mappers.QueryFieldType;
import gg.xp.xivapi.mappers.getters.MetaFieldMapper;
import gg.xp.xivapi.mappers.getters.NormalFieldMapper;
import gg.xp.xivapi.mappers.getters.ThisFieldMapper;
import gg.xp.xivapi.mappers.getters.TransientFieldMapper;
import gg.xp.xivapi.mappers.util.MappingUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

			// TODO: there is no transient equivalent of this
			if (thisAnn != null) {
				fieldMapper = new ThisFieldMapper<>(transientFieldAnn != null, returnType, method, mapper);
			}
			else if (metaFieldAnn != null) {
				// If it is a meta field (value, row_id, score, etc), use MetaFieldMapper
				fieldMapper = new MetaFieldMapper<>(fieldName, returnType, method, mapper);
			}
			else if (returnType.equals(XivApiLangString.class)) {
				fieldMapper = new LangStringFieldMapper(fieldName, transientFieldAnn != null, method);
			}
			else {
				XivApiLang langAnn = method.getAnnotation(XivApiLang.class);
				if (method.isAnnotationPresent(XivApiRaw.class)) {
					fieldName += "@as(raw)";
				}
				else if (langAnn != null) {
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
	}

	@Override
	public X getValue(JsonNode current, XivApiContext context) {

		int primaryKey;
		int rowId;
		final Map<Method, Object> methodValueMap = new LinkedHashMap<>();
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

		//noinspection unchecked
		return (X) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{objectType}, new ObjectInvocationHandler(methodValueMap, strict));

	}

	@Override
	public List<QueryField> getQueryFields() {
		List<QueryField> normalFields = new ArrayList<>();
		List<QueryField> transientFields = new ArrayList<>();
		@Nullable QueryField wildcardNormal = null;
		@Nullable QueryField wildcardTransient = null;
		// Iterate through each sub-mapping
		outer:
		for (FieldMapper<?> mapper : methodFieldMap.values()) {
			for (QueryField qf : mapper.getQueryFields()) {
				boolean isWildcard = qf.isAll();
				boolean isTrans = qf.type() == QueryFieldType.TransientField;
				if (isTrans) {
					if (isWildcard) {
						wildcardTransient = qf;
					}
					else {
						transientFields.add(qf);
					}
				}
				else {
					if (isWildcard) {
						wildcardNormal = qf;
					}
					else {
						normalFields.add(qf);
					}
				}
				if (wildcardNormal != null && wildcardTransient != null) {
					// No point in continuing if everything is a wildcard anyway
					break outer;
				}
			}
		}
		List<QueryField> out = new ArrayList<>();
		if (wildcardNormal != null) {
			out.add(wildcardNormal);
		}
		else {
			out.addAll(normalFields);
		}
		if (wildcardTransient != null) {
			out.add(wildcardTransient);
		}
		else {
			out.addAll(transientFields);
		}
		return out;
	}

}
