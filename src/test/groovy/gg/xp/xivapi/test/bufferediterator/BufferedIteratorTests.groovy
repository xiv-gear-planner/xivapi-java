package gg.xp.xivapi.test.bufferediterator

import gg.xp.xivapi.pagination.BufferedIterator
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@CompileStatic
@Slf4j
class BufferedIteratorTests {

	@TupleConstructor(includeFields = true, includes = "elements")
	class TrackingIterator<X> implements Iterator<X> {
		private final List<X> elements;
		private int index = 0

		@Override
		boolean hasNext() {
			log.info "hasNext ${index}"
			return index < elements.size()
		}

		@Override
		X next() {
			log.info "next ${index}"
			X out = elements[index]
			index++
			return out
		}
	}

	@Test
	void testBufferedIteratorInit() {
		List<Integer> inputs = (0..<10)

		TrackingIterator<Integer> iter = new TrackingIterator<>(inputs)

		BufferedIterator<Integer> bi = new BufferedIterator<>(iter, 5)

		Thread.sleep(1_000)

		// This is 6, because the capacity check happens when the BufferedIterator adds the element to its queue.
		// It will have already called the hasNext() method and picked the element out of the parent iterator, it just
		// won't be on its internal queue at that point due to capacity.
		Assertions.assertEquals(6, iter.index)

	}

	@Test
	void testBufferedIteratorValues() {
		List<Integer> inputs = (0..<10)

		TrackingIterator<Integer> iter = new TrackingIterator<>(inputs)

		BufferedIterator<Integer> bi = new BufferedIterator<>(iter, 5)

		List<Integer> results = bi.toList()

		Assertions.assertEquals(inputs, results)

		Assertions.assertEquals(10, iter.index)
	}

	@Test
	void testBuffering() {
		List<Integer> inputs = (0..<10)

		TrackingIterator<Integer> iter = new TrackingIterator<>(inputs)

		BufferedIterator<Integer> bi = new BufferedIterator<>(iter, 5)

		inputs.forEach { it ->
			if (!bi.hasNext()) {
				Assertions.fail("Had no more elements")
			}
			Assertions.assertEquals(Math.min(10, it + 6), iter.index)
			int next = bi.next()
			Assertions.assertEquals(it, next)
			// Give the feeder thread time to catch up and check the index again
			Thread.sleep(250)
			Thread.yield()
			Assertions.assertEquals(Math.min(10, it + 7), iter.index)
		}

	}
}
