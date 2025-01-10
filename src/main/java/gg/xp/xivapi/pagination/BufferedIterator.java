package gg.xp.xivapi.pagination;

import gg.xp.xivapi.mappers.util.ThreadingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

public class BufferedIterator<X> implements Iterator<X> {

	private static final Logger log = LoggerFactory.getLogger(BufferedIterator.class);

	private final int bufferSize;
	private final Queue<X> buffer = new ArrayDeque<>();
	private final Object lock = new Object();
	private volatile boolean done;

	public BufferedIterator(Iterator<X> iter, int bufferSize) {
		this.bufferSize = bufferSize;
		ThreadingUtils.tryStartVirtualThread(() -> {
			try {
				iter.forEachRemaining(this::add);
			}
			catch (Throwable t) {
				log.error("BufferedIterator failed to read from {}", iter, t);
			}
			finally {
				done();
			}
		});
	}

	// Add to queue
	private void add(X item) {
		// Acquire lock
		synchronized (lock) {
			while (buffer.size() >= bufferSize) {
				try {
					// Sleep if buffer is full
					lock.wait(10_000);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			// Add item once there is space
			buffer.add(item);
			lock.notifyAll();
		}
	}

	private void done() {
		synchronized (lock) {
			done = true;
			lock.notifyAll();
		}
	}

	@Override
	public boolean hasNext() {
		// Wait until either there is an item to take off the queue, or we are done iterating
		synchronized (lock) {
			while (true) {
				X next = buffer.peek();
				if (next != null) {
					return true;
				}
				else if (done) {
					return false;
				}
				else {
					try {
						lock.wait(10_000);
					}
					catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	@Override
	public X next() {
		// Wait until either there is an item to take off the queue, or we are done iterating
		synchronized (lock) {
			while (true) {
				X next = buffer.poll();
				if (next != null) {
					lock.notifyAll();
					return next;
				}
				else if (done) {
					throw new NoSuchElementException("Iteration done");
				}
				else {
					try {
						lock.wait(10_000);
					}
					catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}
}
