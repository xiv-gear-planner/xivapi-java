package gg.xp.xivapi.test.bufferediterator

import gg.xp.xivapi.exceptions.XivApiPaginationException
import gg.xp.xivapi.pagination.BufferedIterator
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

import java.lang.ref.Cleaner
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@CompileStatic
@Slf4j
class BufferedIteratorTests {

	@TupleConstructor(includeFields = true, includes = "elements")
	class TrackingIterator<X> implements Iterator<X> {
		private final List<X> elements;
		private int index = 0
		private int failAt = -1

		@Override
		boolean hasNext() {
			log.info "hasNext ${index}"
			return index < elements.size()
		}

		@Override
		X next() {
			log.info "next ${index}"
			if (index == failAt) {
				throw new RuntimeException("intentional failure")
			}
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

		// Give feeder time to initially fill
		Thread.sleep(100)
		Thread.yield()

		inputs.forEach { it ->
			if (!bi.hasNext()) {
				Assertions.fail("Had no more elements")
			}
			Assertions.assertEquals(Math.min(10, it + 6), iter.index)
			int next = bi.next()
			Assertions.assertEquals(it, next)
			// Give the feeder thread time to catch up and check the index again
			Thread.sleep(100)
			Thread.yield()
			Assertions.assertEquals(Math.min(10, it + 7), iter.index)
		}

	}

	@CompileStatic
	AtomicBoolean abandonedIteratorHelper() {
		// true if the BI has been GC'd
		var isCleaned = new AtomicBoolean()

		List<Integer> inputs = (0..<10)

		TrackingIterator<Integer> iter = new TrackingIterator<>(inputs)

		BufferedIterator<Integer> bi = new BufferedIterator<>(iter, 5)

		// Register it with a cleaner
		var cleaner = Cleaner.create()
		cleaner.register bi, {
			log.info("Cleaned")
			isCleaned.set(true)
		}

		// This is the last time we touch the BI
		bi.next()

		return isCleaned
	}

	@Test
	@Timeout(value = 5, unit = TimeUnit.SECONDS)
	void testAbandonedIteratorIsCleanedUp() {
		var cleaned = abandonedIteratorHelper()
		// Give feeder time to initially fill
		Thread.sleep 100
		Thread.yield()
		// GC until it goes away
		while (!cleaned.get()) {
			System.gc()
			Thread.sleep 100
			Thread.yield()
		}
		Assertions.assertTrue cleaned.get()

	}

	@Test
	void testFailureFirstElement() {
		List<Integer> inputs = (0..<10)

		TrackingIterator<Integer> iter = new TrackingIterator<>(inputs)
		// Fail on the very first element
		iter.failAt = 0

		BufferedIterator<Integer> bi = new BufferedIterator<>(iter, 5)

		// Give feeder time to initially fill
		Thread.sleep(100)
		Thread.yield()

		inputs.forEach { it ->
			if (!bi.hasNext()) {
				Assertions.fail("Had no more elements")
			}
			Assertions.assertThrows(XivApiPaginationException, {bi.next()})
		}
	}

	@Test
	void testFailureEarly() {
		List<Integer> inputs = (0..<10)

		TrackingIterator<Integer> iter = new TrackingIterator<>(inputs)
		// Fail during the initial read buffer
		iter.failAt = 2

		BufferedIterator<Integer> bi = new BufferedIterator<>(iter, 5)

		// Give feeder time to initially fill
		Thread.sleep(100)
		Thread.yield()

		inputs.forEach { it ->
			if (!bi.hasNext()) {
				Assertions.fail("Had no more elements")
			}
			if (it < 2) {
				Assertions.assertEquals(it, bi.next())
			}
			else {
				Assertions.assertThrows(XivApiPaginationException, {bi.next()})
			}
		}
	}

	@Test
	void testFailureLate() {
		List<Integer> inputs = (0..<10)

		TrackingIterator<Integer> iter = new TrackingIterator<>(inputs)
		// Fail after the initial read buffer
		iter.failAt = 8

		BufferedIterator<Integer> bi = new BufferedIterator<>(iter, 5)

		// Give feeder time to initially fill
		Thread.sleep(100)
		Thread.yield()

		inputs.forEach { it ->
			if (!bi.hasNext()) {
				Assertions.fail("Had no more elements")
			}
			if (it < 8) {
				Assertions.assertEquals(it, bi.next())
			}
			else {
				Assertions.assertThrows(XivApiPaginationException, {bi.next()})
			}
		}
	}
}
