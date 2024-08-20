package gg.xp.xivapi.mappers.objects;

import gg.xp.xivapi.annotations.NullIfZero;
import gg.xp.xivapi.clienttypes.XivApiBase;
import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.exceptions.XivApiDeserializationException;
import gg.xp.xivapi.mappers.util.MappingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

public class ObjectInvocationHandler implements InvocationHandler, Serializable {

	private static final Logger log = LoggerFactory.getLogger(ObjectInvocationHandler.class);

	@Serial
	private static final long serialVersionUID = -7240936731264081327L;

	private static final Method equalsMethod;
	private static final Method hashCodeMethod;
	private static final Method mapMethod;

	static {
		try {
			equalsMethod = Object.class.getMethod("equals", Object.class);
			hashCodeMethod = Object.class.getMethod("hashCode");
			mapMethod = XivApiBase.class.getMethod("getMethodValueMap");
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	private final Map<Method, Object> methodValueMap;
	private final boolean strict;

	public ObjectInvocationHandler(Map<Method, Object> methodValueMap, boolean strict) {
		this.methodValueMap = methodValueMap;
		this.strict = strict;
//		this.hashCode = methodValueMap.hashCode();
	}


	@Override
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
			// Handle default java object methods
			if (method.getDeclaringClass().equals(Object.class)) {
				if (method.equals(hashCodeMethod)) {
					return methodValueMap.hashCode();
				}
				else if (method.equals(equalsMethod)) {
					Object that = args[0];
					if (that == proxy) {
						return true;
					}
					else if (that instanceof XivApiObject other) {
						var otherValueMap = other.getMethodValueMap();
						return MappingUtils.methodMapEquals(methodValueMap, otherValueMap);
					}
					else {
						return false;
					}
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
				      || returnType.isAnnotationPresent(NullIfZero.class))) {
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
