package gg.xp.xivapi.test.search

import org.junit.jupiter.api.Test
import static gg.xp.xivapi.filters.SearchFilters.*
import static org.junit.jupiter.api.Assertions.*

class SearchFilterOperatorTests {

    @Test
    void testOrOperator() {
        def f1 = eq('Foo', 1)
        def f2 = eq('Bar', 2)
        def combined = f1 | f2
        def expected = or(f1, f2)
        assertEquals('Foo=1 Bar=2', combined.toFilterString())
        assertTrue(combined.getClass().getSimpleName().contains('SearchFilterOr'))
        assertEquals(expected, combined)
        assertEquals(expected.hashCode(), combined.hashCode())
    }

    @Test
    void testAndOperator() {
        def f1 = eq('Foo', 1)
        def f2 = eq('Bar', 2)
        def combined = f1 & f2
        def expected = and(f1, f2)
        assertEquals('+Foo=1 +Bar=2', combined.toFilterString())
        assertTrue(combined.getClass().getSimpleName().contains('SearchFilterAnd'))
        assertEquals(expected, combined)
        assertEquals(expected.hashCode(), combined.hashCode())
    }

    @Test
    void testNotOperator() {
        def f = eq('Foo', 1)
        def negated = ~f
        def expected = not(f)
        assertEquals('-Foo=1', negated.toFilterString())
        assertTrue(negated.getClass().getSimpleName().contains('SearchFilterNot'))
        assertEquals(expected, negated)
        assertEquals(expected.hashCode(), negated.hashCode())
    }

    @Test
    void testOperatorCollapsing() {
        def f1 = eq('A', 1)
        def f2 = eq('B', 2)
        def f3 = eq('C', 3)

        // AND collapsing
        def andCombined = f1 & f2 & f3
        def andExpected = and(f1, f2, f3)
        assertEquals('+A=1 +B=2 +C=3', andCombined.toFilterString())
        // In SearchFilterAnd record, it should have 3 filters
        assertEquals(3, andCombined.filters().size())
        assertEquals(andExpected, andCombined)
        assertEquals(andExpected.hashCode(), andCombined.hashCode())

        // OR collapsing
        def orCombined = f1 | f2 | f3
        def orExpected = or(f1, f2, f3)
        assertEquals('A=1 B=2 C=3', orCombined.toFilterString())
        assertEquals(3, orCombined.filters().size())
        assertEquals(orExpected, orCombined)
        assertEquals(orExpected.hashCode(), orCombined.hashCode())
    }

    @Test
    void testComplexOperators() {
        def f1 = eq('A', 1)
        def f2 = eq('B', 2)
        def f3 = eq('C', 3)

        def combined = (f1 | f2) & ~f3
        def expected = and(or(f1, f2), not(f3))
        assertEquals('+(A=1 B=2) -C=3', combined.toFilterString())
        assertEquals(expected, combined)
        assertEquals(expected.hashCode(), combined.hashCode())
    }
}
