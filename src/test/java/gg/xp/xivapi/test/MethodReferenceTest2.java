package gg.xp.xivapi.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

public class MethodReferenceTest2 {
	private static final Logger log = LoggerFactory.getLogger(MethodReferenceTest2.class);

	public interface SF<X, Y> extends Function<X, Y>, Serializable { }

	public static class Foo {
		public String bar() {
			return "Hello, World!";
		}
	}

	public static void main(String[] args) throws Exception {
		{
			SF<Foo, String> myfunc1 = Foo::bar;
			Method method = extractMethod(myfunc1);
			log.info("Method 1: {}", method);
		}

	}

	public static <T> Method extractMethod(SF<T, ?> methodReference) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
		// Get the writeReplace method from the lambda (it actually exists for serialization purposes)
		Method writeReplaceMethod = ((Serializable) methodReference).getClass().getDeclaredMethod("writeReplace");
		writeReplaceMethod.setAccessible(true);

		// Invoke writeReplace and cast to SerializedLambda
		SerializedLambda serializedLambda = (SerializedLambda) writeReplaceMethod.invoke(methodReference);

		// Extract the class name and method name
		String className = serializedLambda.getImplClass().replace('/', '.');
		String methodName = serializedLambda.getImplMethodName();

		// Load the class
		Class<?> clazz = Class.forName(className);

		// Extract the method based on the method name
		// Since this is a method reference, it could be an instance method,
		// so we search for it assuming a non-static method with the correct return type and parameters
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.getName().equals(methodName) && method.getParameterCount() == 0) {
				return method;
			}
		}

		// Alternatively, if you know the exact parameters and return type, you could directly use:
		// return clazz.getDeclaredMethod(methodName);

		throw new NoSuchMethodException("Method " + methodName + " not found in class " + className);
	}
}
