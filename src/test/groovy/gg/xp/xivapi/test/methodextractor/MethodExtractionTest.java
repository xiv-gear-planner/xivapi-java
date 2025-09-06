package gg.xp.xivapi.test.methodextractor;

import gg.xp.xivapi.filters.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

class MethodExtractionTest {

	static class Foo {
		String bar() {
			return toString();
		}
	}

	@Test
	void testMethodExtraction() throws NoSuchMethodException {
		Getter<Foo, String> getter = Foo::bar;
		Method method = getter.getMethod();
		Method expectedMethod = Foo.class.getDeclaredMethod("bar");
		Assertions.assertEquals(expectedMethod, method);
	}

}
