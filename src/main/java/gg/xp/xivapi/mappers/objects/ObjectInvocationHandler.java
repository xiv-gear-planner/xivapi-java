package gg.xp.xivapi.mappers.objects;

import gg.xp.xivapi.annotations.EmptyStringNull;
import gg.xp.xivapi.annotations.NullIfZero;
import gg.xp.xivapi.clienttypes.XivApiBase;
import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.exceptions.XivApiDeserializationException;
import gg.xp.xivapi.mappers.util.MappingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ObjectInvocationHandler implements InvocationHandler, Serializable {

	private static final Logger log = LoggerFactory.getLogger(ObjectInvocationHandler.class);

	@Serial
	private static final long serialVersionUID = -7240936731264081326L;

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

	@Serial
	private Object writeReplace() throws ObjectStreamException {
		Map<MethodMetadata, Object> metaMap = new HashMap<>(methodValueMap.size());
		for (var entry : methodValueMap.entrySet()) {
			metaMap.put(MethodMetadata.fromMethod(entry.getKey()), entry.getValue());
		}
		return new SerializableForm(metaMap, strict);
	}

	// TODO: figure out a way to also allow this to make use of KeyedAlikeMap (check if worth)
	private record SerializableForm(Map<MethodMetadata, Object> methodMetaMap, boolean strict) implements Serializable {
		@Serial
		private Object readResolve() {
			Map<Method, Object> methodMap = new HashMap<>(methodMetaMap.size());
			for (var entry : methodMetaMap.entrySet()) {
				try {
					methodMap.put(entry.getKey().toMethod(), entry.getValue());
				}
				catch (NoSuchMethodException | ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
			return new ObjectInvocationHandler(methodMap, strict);
		}
	}

	// TODO: this should be de-duplicated with the equivalent in StructInvocationHandler, but that would break
	// serialization compatibility, so hold off for now.
	public static final class MethodMetadata implements Serializable {
		@Serial
		private static final long serialVersionUID = 1L;

		private final String methodName;
		private final String[] parameterTypeNames;
		private final String interfaceClassName;

		private MethodMetadata(String methodName, String[] parameterTypeNames, String interfaceClassName) {
			this.methodName = methodName.intern();
			for (int i = 0; i < parameterTypeNames.length; i++) {
				parameterTypeNames[i] = parameterTypeNames[i].intern();
			}
			this.parameterTypeNames = parameterTypeNames;
			this.interfaceClassName = interfaceClassName.intern();
		}

		public static MethodMetadata fromMethod(Method method) {
			return new MethodMetadata(
					method.getName(),
					Arrays.stream(method.getParameterTypes()).map(Class::getName).toArray(String[]::new),
					method.getDeclaringClass().getName()
			);
		}

		public Method toMethod() throws NoSuchMethodException, ClassNotFoundException {
			Class<?> interfaceClass = Class.forName(interfaceClassName);
			Class<?>[] parameterTypes = Arrays.stream(parameterTypeNames)
					.map(name -> {
						try {
							return Class.forName(name);
						}
						catch (ClassNotFoundException e) {
							throw new RuntimeException(e);
						}
					}).toArray(Class<?>[]::new);
			return interfaceClass.getMethod(methodName, parameterTypes);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null || getClass() != obj.getClass()) return false;
			MethodMetadata that = (MethodMetadata) obj;
			return methodName.equals(that.methodName) &&
			       Arrays.equals(parameterTypeNames, that.parameterTypeNames) &&
			       interfaceClassName.equals(that.interfaceClassName);
		}

		@Override
		public int hashCode() {
			int result = Objects.hash(methodName, interfaceClassName);
			result = 31 * result + Arrays.hashCode(parameterTypeNames);
			return result;
		}
	}
}
