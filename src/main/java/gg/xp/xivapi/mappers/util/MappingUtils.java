package gg.xp.xivapi.mappers.util;

import gg.xp.xivapi.annotations.XivApiField;
import gg.xp.xivapi.annotations.XivApiMetaField;
import gg.xp.xivapi.annotations.XivApiSheet;
import gg.xp.xivapi.annotations.XivApiTransientField;
import gg.xp.xivapi.clienttypes.XivApiSchemaVersion;
import gg.xp.xivapi.exceptions.XivApiMappingException;
import gg.xp.xivapi.mappers.QueryField;
import gg.xp.xivapi.mappers.QueryFieldType;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class MappingUtils {
	private MappingUtils() {
	}

	/**
	 * Given a method, determine the name of the XivApi field to use.
	 *
	 * @param method The method
	 * @return The name
	 * @throws XivApiMappingException if the method neither has an appropriate name nor an appropriate annotation.
	 */
	public static String getFieldName(Method method) {
		String fieldName;
		XivApiField fieldAnnotation = method.getAnnotation(XivApiField.class);
		if (fieldAnnotation != null) {
			return fieldAnnotation.value();
		}
		XivApiMetaField metaFieldAnn = method.getAnnotation(XivApiMetaField.class);
		if (metaFieldAnn != null) {
			return metaFieldAnn.value();
		}
		XivApiTransientField transAnn = method.getAnnotation(XivApiTransientField.class);
		if (transAnn != null && !transAnn.value().isEmpty()) {
			return transAnn.value();
		}
		if (method.getName().startsWith("get")) {
			fieldName = method.getName().substring(3);
		}
		else if (method.getName().startsWith("is")) {
			fieldName = method.getName().substring(2);
		}
		else {
			throw new RuntimeException("I don't know how to map " + method.getName());
		}
		return fieldName;
	}

	/**
	 * Check whether a field should have [] suffixed to it in the fields param. i.e. returns true iff the type is
	 * a list or array.
	 *
	 * @param type The type to check
	 * @return True if list/array.
	 */
	public static boolean isArrayQueryType(Class<?> type) {
		return type.equals(List.class) || type.isArray();
	}

	public static String validateAndGetSheetName(Class<?> cls) {

		if (!cls.isInterface()) {
			throw new IllegalArgumentException("Argument must be an interface, got %s".formatted(cls));
		}

		XivApiSheet sheetAnn = cls.getAnnotation(XivApiSheet.class);

		if (sheetAnn == null) {
			throw new IllegalArgumentException("Class %s does not have a @XivApiSheet sheetAnn".formatted(cls));
		}

		String value = sheetAnn.value();

		if (value.isEmpty()) {
			return cls.getSimpleName();
		}

		return value;
	}

	public static List<NameValuePair> formatQueryFields(Collection<QueryField> fields) {
		Map<QueryFieldType, List<QueryField>> grouped = fields.stream().collect(Collectors.groupingBy(QueryField::type));
		return List.of(
				new BasicNameValuePair("fields", grouped.getOrDefault(QueryFieldType.Field, List.of())
						.stream()
						.map(QueryField::name)
						.collect(Collectors.joining(","))),
				new BasicNameValuePair("transient", grouped.getOrDefault(QueryFieldType.TransientField, List.of())
						.stream()
						.map(QueryField::name)
						.collect(Collectors.joining(",")))
		);
	}

	public static Class<?> parameterizedTypeToRawClass(Type typeOriginal) {
		Type type = typeOriginal;
		while (type instanceof ParameterizedType pt) {
			type = pt.getRawType();
		}
		if (type instanceof Class cls) {
			return cls;
		}
		else {
			throw new IllegalArgumentException("Cannot determine base type for " + typeOriginal);
		}
	}

	private record XivApiSchemaImpl(String version) implements XivApiSchemaVersion {
		@Override
		public String fullVersionString() {
			return version;
		}
	}

	public static XivApiSchemaVersion makeSchemaVersion(String version) {
		return new XivApiSchemaImpl(version);
	}

	public static boolean methodMapEquals(Map<Method, Object> left, Map<Method, Object> right) {
		if (!Objects.equals(left.keySet(), right.keySet())) {
			return false;
		}
		for (var entry : left.entrySet()) {
			Object leftValue = entry.getValue();
			Object rightValue = right.get(entry.getKey());
			if (!unknownValueEquals(leftValue, rightValue)) {
				return false;
			}
		}
		return true;
	}

	// TODO: unit tests
	public static boolean unknownValueEquals(Object left, Object right) {
		if (left == null && right == null) {
			return true;
		}
		else if (left == null || right == null) {
			return false;
		}
		else if (left.getClass().isArray() && right.getClass().isArray()) {
			// TODO: can this handle a primitive array?
			return Arrays.equals((Object[]) left, (Object[]) right);
		}
		else {
			return Objects.equals(left, right);
		}
	}
}
