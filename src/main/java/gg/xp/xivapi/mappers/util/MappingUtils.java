package gg.xp.xivapi.mappers.util;

import gg.xp.xivapi.annotations.XivApiField;
import gg.xp.xivapi.annotations.XivApiMetaField;
import gg.xp.xivapi.exceptions.XivApiMappingException;

import java.lang.reflect.Method;

public final class MappingUtils {
	private MappingUtils() {}

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
		XivApiMetaField metaFieldAnn = method.getAnnotation(XivApiMetaField.class);
		if (fieldAnnotation != null) {
			fieldName = fieldAnnotation.value();
		}
		else if (metaFieldAnn != null) {
			fieldName = metaFieldAnn.value();
		}
		else if (method.getName().startsWith("get")) {
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
}
