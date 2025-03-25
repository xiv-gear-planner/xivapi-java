package gg.xp.xivapi.pagination;

import gg.xp.xivapi.mappers.util.ThreadingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

public class BufferedIterator<X> implements Iterator<X> {

	private static final Logger log = LoggerFactory.getLogger(BufferedIterator.class);

	// bufferSize is how many items we can have in the buffer
	private final int bufferSize;
	private final Queue<X> buffer = new ArrayDeque<>();
	private final Object lock = new Object();
	/**
	 * 'done' is true when no more items will be added to the buffer.
	 * 'done' does not imply that the BufferedIterator cannot hold any more items - this iterator is finished if
	 * 'done' is true and the buffer is empty.
	 */
	private volatile boolean done;

	public BufferedIterator(Iterator<X> iter, int bufferSize) {
		this.bufferSize = bufferSize;
		var thisRef = new WeakReference<>(this);
		startFeedLoop(thisRef, lock, bufferSize, buffer, iter);
	}

	// Separate method for the actual worker loopto avoid unintentionally capturing 'this' in the ctor.
	// This allows the BufferedIterator to get garbage collected (hence why it is passed as a WeakReference), and then
	// the loop will stop.
	private static <Y> void startFeedLoop(WeakReference<BufferedIterator<Y>> thisRef, Object lock, int bufferSize, Queue<Y> buffer, Iterator<Y> iter) {
		ThreadingUtils.tryStartVirtualThread(() -> {
			try {
				while (iter.hasNext()) {
					// If the BufferedIterator got gc'd, stop
					if (thisRef.get() == null) {
						return;
					}
					Y next = iter.next();
					synchronized (lock) {
						while (buffer.size() >= bufferSize) {
							if (thisRef.get() == null) {
								return;
							}
							try {
								// Sleep if buffer is full
								lock.wait(1_000);
							}
							catch (InterruptedException e) {
								throw new RuntimeException(e);
							}
						}
						// Add item once there is space
						buffer.add(next);
						lock.notifyAll();
					}
				}
			}
			catch (Throwable t) {
				log.error("BufferedIterator failed to read from {}", iter, t);
			}
			finally {
				BufferedIterator<Y> thisResolved = thisRef.get();
				if (thisResolved != null) {
					thisResolved.done();
				}
			}
		});

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
