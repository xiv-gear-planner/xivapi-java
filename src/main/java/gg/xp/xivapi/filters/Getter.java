package gg.xp.xivapi.filters;

import gg.xp.xivapi.exceptions.XivApiException;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@FunctionalInterface
public interface Getter<ItemType, FieldType> extends Serializable {

	FieldType get(ItemType item);

	default Method getMethod() {

		try {
			// Get the writeReplace method from the lambda (it actually exists for serialization purposes)
			Method writeReplaceMethod = ((Serializable) this).getClass().getDeclaredMethod("writeReplace");
			writeReplaceMethod.setAccessible(true);

			// Invoke writeReplace and cast to SerializedLambda
			SerializedLambda serializedLambda = (SerializedLambda) writeReplaceMethod.invoke(this);

			// Extract the class name and method name
			String className = serializedLambda.getImplClass().replace('/', '.');
			String methodName = serializedLambda.getImplMethodName();

			// Load the class
			Class<?> clazz = Class.forName(className);

			// Extract the method based on the method name
			return clazz.getDeclaredMethod(methodName);
		}
		catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
			throw new XivApiException(e);
		}

	}
}
