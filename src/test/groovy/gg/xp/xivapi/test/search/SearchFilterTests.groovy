package gg.xp.xivapi.test.search


import org.junit.jupiter.api.Test

import static gg.xp.xivapi.filters.SearchFilters.*
import static org.junit.jupiter.api.Assertions.assertEquals

class SearchFilterTests {
	@Test
	void testBasicFilter() {
		var filter = of('foo')
		assertEquals 'foo', filter.toFilterString()
	}

	@Test
	void testEqFilterNumber() {
		var filter = eq('Foo', 5)
		assertEquals 'Foo=5', filter.toFilterString()
	}

	@Test
	void testEqFilterBool() {
		var filter = eq('Foo', false)
		assertEquals 'Foo=false', filter.toFilterString()
	}

	@Test
	void testEqFilterString() {
		var filter = eq('Foo', 'Stuff')
		assertEquals 'Foo=\"Stuff\"', filter.toFilterString()
	}

	@Test
	void testGt() {
		var filter = gt('Foo', 2.5)
		assertEquals 'Foo>2.5', filter.toFilterString()
	}

	@Test
	void testGte() {
		var filter = gte('Foo', 2.5)
		assertEquals 'Foo>=2.5', filter.toFilterString()
	}

	@Test
	void testLt() {
		var filter = lt('Foo', 2.5)
		assertEquals 'Foo<2.5', filter.toFilterString()
	}

	@Test
	void testLte() {
		var filter = lte('Foo', 2.5)
		assertEquals 'Foo<=2.5', filter.toFilterString()
	}

	@Test
	void testNot() {
		var filter = not(eq('Foo', 5))
		assertEquals '-Foo=5', filter.toFilterString()
	}

	@Test
	void testAnd() {
		var filter = and(eq('Foo', 5), not(eq('Bar', 'Baz')))
		assertEquals '+Foo=5 -Bar="Baz"', filter.toFilterString()
	}

	@Test
	void testOr() {
		// TODO add docs about or + not
		var filter = or(eq('Foo', 5), not(eq('Bar', 'Baz')))
		assertEquals 'Foo=5 (-Bar="Baz")', filter.toFilterString()
	}

	@Test
	void testAndOuterNot() {
		var filter = not(and(eq('Foo', 5), not(eq('Bar', 'Baz'))))
		assertEquals '-(+Foo=5 -(Bar="Baz"))', filter.toFilterString()
	}

	@Test
	void testAndOr() {
		var filter = and(or(eq('Foo', 5), not(eq('Bar', 'Baz'))), gt('Stuff', 2.5))
		assertEquals '+(Foo=5 (-Bar="Baz")) +Stuff>2.5', filter.toFilterString()
	}

	@Test
	void testIsTrue() {
		var filter = isTrue('Foo')
		assertEquals 'Foo=true', filter.toFilterString()
	}

	@Test
	void testIsFalse() {
		var filter = isFalse('Foo')
		assertEquals 'Foo=false', filter.toFilterString()
	}

	@Test
	void testStrPart() {
		var filter = strPart('Foo', 'PartialText')
		assertEquals 'Foo~"PartialText"', filter.toFilterString()
	}

	@Test
	void testCustomBinary() {
		var filter = binary('Foo', '_', 'Bar')
		assertEquals 'Foo_"Bar"', filter.toFilterString()
	}

	@Test
	void testAny() {
		var filter = eq(any('Foo'), 5)
		// TODO: specific index
		assertEquals 'Foo[]=5', filter.toFilterString()
	}
}
