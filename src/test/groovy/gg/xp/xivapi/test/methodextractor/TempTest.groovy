package gg.xp.xivapi.test.methodextractor

import gg.xp.xivapi.filters.Getter
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import java.lang.reflect.Method

@CompileStatic
class TempTest {

	@CompileStatic
	static class Foo {
		String bar() {
			return toString();
		}
	}

	@Test
	@Disabled
	void tempTest() {
		// This doesn't work because groovy does these a little differently. It generates a class like with a long-form
		// Java lambda, which we can't introspect (project Babylon notwithstanding).
		Getter<Foo, String> getter1 = Foo::bar
		// Groovy method closure doesn't work right because it isn't fully type checked at compile time
		Getter<Foo, String> getter2 = Foo.&bar

		Method method1 = getter1.method
		Method method2 = getter2.method

		println "foo"
	}

}
