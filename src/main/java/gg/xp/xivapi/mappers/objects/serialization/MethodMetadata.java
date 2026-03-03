package gg.xp.xivapi.mappers.objects.serialization;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * Serializes Method objects by their name, parameter types, and declaring class.
 */
public final class MethodMetadata implements Serializable {
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
		Class<?>[] parameterTypes = new Class<?>[parameterTypeNames.length];
		for (int i = 0; i < parameterTypeNames.length; i++) {
			parameterTypes[i] = Class.forName(parameterTypeNames[i]);
		}
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
