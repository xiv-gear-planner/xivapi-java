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
	void testAndAutoFlatten1() {
		var filter = and(
				and(
						and(
								eq('Foo', 5)
						),
						and(
								not(
										and(
												eq('Bar', 'Baz')
										)
								)
						)
				)
		)
		assertEquals '+Foo=5 -Bar="Baz"', filter.toFilterString()
	}

	@Test
	void testAndAutoFlatten2() {
		var filterPart1 = and(eq('Foo', 5), not(eq('Bar', 'Baz')))
		var filterPart2 = and(eq('Foo2', 5), not(eq('Bar2', 'Baz')))
		var filter = and([filterPart1, filterPart2])
		assertEquals '+Foo=5 -Bar="Baz" +Foo2=5 -Bar2="Baz"', filter.toFilterString()
	}

	@Test
	void testOrAutoFlatten1() {
		var filter = or(or(or(eq('Foo', 5)), or(not(or(eq('Bar', 'Baz'))))))
		assertEquals 'Foo=5 (-Bar="Baz")', filter.toFilterString()
	}

	@Test
	void testOrAutoFlatten2() {
		var filterPart1 = or(eq('Foo', 5), not(eq('Bar', 'Baz')))
		var filterPart2 = or(eq('Foo2', 5), not(eq('Bar2', 'Baz')))
		var filter = or([filterPart1, filterPart2])
		assertEquals 'Foo=5 (-Bar="Baz") Foo2=5 (-Bar2="Baz")', filter.toFilterString()
	}

	@Test
	void testOr() {
		var filter = or(eq('Foo', 5), not(eq('Bar', 'Baz')))
		assertEquals 'Foo=5 (-Bar="Baz")', filter.toFilterString()
	}

	@Test
	void testAndOuterNot() {
		var filter = not(and(eq('Foo', 5), not(eq('Bar', 'Baz'))))
		// TODO: perhaps it would be possible to optimize this into `-Foo=5 Bar="Baz"` ?
		assertEquals '-(+Foo=5 -Bar="Baz")', filter.toFilterString()
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

	@Test
	void testNotFlatten1() {
		var filter = not(not(eq('Foo', 5)))
		assertEquals 'Foo=5', filter.toFilterString()
	}

	@Test
	void testNotFlatten2() {
		var filter = not(not(and(eq('Foo', 5), not(eq('Bar', "Baz")))))
		assertEquals '+Foo=5 -Bar="Baz"', filter.toFilterString()
	}

	@Test
	void testNotFlatten3() {
		var filter = not(not(and(not(eq('Bar', "Baz")), eq('Foo', 5))))
		assertEquals '-Bar="Baz" +Foo=5', filter.toFilterString()
	}

	@Test
	void testNotFlatten4() {
		var filter = not(not(not(and(eq('Foo', 5), not(eq('Bar', "Baz"))))))
		assertEquals '-(+Foo=5 -Bar="Baz")', filter.toFilterString()
	}

	@Test
	void testNotFlatten5() {
		var filter = not(not(not(and(not(eq('Bar', "Baz")), eq('Foo', 5)))))
		assertEquals '-(-Bar="Baz" +Foo=5)', filter.toFilterString()
	}
}
