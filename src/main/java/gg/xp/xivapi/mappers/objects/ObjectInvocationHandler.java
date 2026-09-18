package gg.xp.xivapi.mappers.objects;

import gg.xp.xivapi.annotations.EmptyStringNull;
import gg.xp.xivapi.annotations.NullIfZero;
import gg.xp.xivapi.clienttypes.XivApiBase;
import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.exceptions.XivApiDeserializationException;
import gg.xp.xivapi.mappers.util.MappingUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class ObjectInvocationHandler implements InvocationHandler, Serializable {

	private static final Logger log = LoggerFactory.getLogger(ObjectInvocationHandler.class);

	private static final Method equalsMethod;
	private static final Method hashCodeMethod;
	private static final Method mapMethod;
	private static final Method tsMethod;

	static {
		try {
			equalsMethod = Object.class.getMethod("equals", Object.class);
			hashCodeMethod = Object.class.getMethod("hashCode");
			mapMethod = XivApiBase.class.getMethod("getMethodValueMap");
			tsMethod = Object.class.getMethod("toString");
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	@Serial
	private static final long serialVersionUID = 3L;
	private final Map<Method, Object> methodValueMap;
	private final boolean strict;
	private final String simpleName;
	private final int rowId;

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType") // We don't want a copy for memory reasons
	public ObjectInvocationHandler(Map<Method, Object> methodValueMap, boolean strict, String simpleName, int rowId) {
		this.methodValueMap = methodValueMap;
		this.strict = strict;
		this.simpleName = simpleName;
		this.rowId = rowId;
	}


	@Override
	@Nullable("When using NullIfZero")
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		Object value = methodValueMap.get(method);
		if (value == null) {
			// Don't override any default methods
			if (method.isDefault()) {
				return InvocationHandler.invokeDefault(proxy, method, args);
			}
			if (method.equals(mapMethod)) {
				return Collections.unmodifiableMap(methodValueMap);
			}
			if (method.getName().equals("getRowId")) {
				return rowId;
			}
			if (method.equals(tsMethod)) {
				return "%s(%s)".formatted(simpleName, rowId);
			}
			// Handle default java object methods
			if (method.getDeclaringClass().equals(Object.class)) {
				if (method.equals(hashCodeMethod)) {
					return MappingUtils.methodMapHashCode(methodValueMap);
				}
				else if (method.equals(equalsMethod)) {
					Object that = args[0];
					if (that == proxy) {
						return true;
					}
					else if (that instanceof XivApiObject other) {
						if (Arrays.equals(proxy.getClass().getGenericInterfaces(), other.getClass().getGenericInterfaces())) {
							var otherValueMap = other.getMethodValueMap();
							return MappingUtils.methodMapEquals(methodValueMap, otherValueMap);
						}
					}
					return false;
				}
			}

			Class<?> returnType = method.getReturnType();
			if (returnType.isPrimitive()) {
				if (strict) {
					throw new XivApiDeserializationException("Null primitive field! %s".formatted(method));
				}
				else {
					log.error("Null primitive field! {}", method.getName());
					if (returnType.equals(boolean.class)) {
						return false;
					}
					return 0;
				}
			}
			else {
				if (!(method.isAnnotationPresent(NullIfZero.class)
				      || returnType.isAnnotationPresent(NullIfZero.class)
				      || (returnType.equals(String.class)
				          && (method.isAnnotationPresent(EmptyStringNull.class)
				              || method.getAnnotatedReturnType().isAnnotationPresent(EmptyStringNull.class))))) {
					if (strict) {
						throw new XivApiDeserializationException("Null object field! %s".formatted(method));
					}
					else {
						log.error("Null object field! {}", method.getName());
					}
				}
			}
		}

		if (value instanceof XivApiObject xao) {
			if (xao.getPrimaryKey() == 0 && method.isAnnotationPresent(NullIfZero.class)) {
				return null;
			}
		}

		return value;
	}

}
