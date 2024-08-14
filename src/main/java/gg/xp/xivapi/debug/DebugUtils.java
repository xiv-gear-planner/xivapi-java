package gg.xp.xivapi.debug;

import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.clienttypes.XivApiStruct;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;

public final class DebugUtils {
	private DebugUtils() {
	}

	public static Map<Method, Object> extractMethodValueMap(XivApiObject object) {
		return extractMethodValueMapInt(object);
	}

	public static Map<Method, Object> extractMethodValueMap(XivApiStruct object) {
		return extractMethodValueMapInt(object);
	}

	// Must have --add-opens=java.base/java.net=ALL-UNNAMED
	private static Map<Method, Object> extractMethodValueMapInt(Object object) {
		if (object instanceof Proxy proxy) {
			try {
				var handlerField = Proxy.class.getDeclaredField("h");
				handlerField.setAccessible(true);
				InvocationHandler handler = (InvocationHandler) handlerField.get(proxy);
				var handlerFields = handler.getClass().getDeclaredFields();
				var mapField = Arrays.stream(handlerFields).filter(field -> field.getType().equals(Map.class)).findFirst().orElseThrow(() -> new RuntimeException("foo"));
				mapField.setAccessible(true);
				//noinspection unchecked
				return (Map<Method, Object>) mapField.get(handler);
			}
			catch (NoSuchFieldException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		else {
			throw new IllegalArgumentException("Not a proxy: " + object);
		}
	}
}
