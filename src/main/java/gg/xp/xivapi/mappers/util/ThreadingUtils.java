package gg.xp.xivapi.mappers.util;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ThreadFactory;

public final class ThreadingUtils {
	private ThreadingUtils() {
	}

	public static ThreadFactory namedDaemonThreadFactory(String nameStub) {
		return new BasicThreadFactory.Builder()
				.namingPattern(nameStub + "-%d")
				.daemon(true)
				.build();
	}

	/**
	 * Try to start a virtual thread with the given runnable. If running on a Java version that does not support
	 * virtual threads, start a platform thread instead.
	 *
	 * @param runnable The task to run
	 */
	public static void tryStartVirtualThread(final Runnable runnable) {
		try {
			Thread.startVirtualThread(runnable);
		}
		catch (Throwable e) {
			new Thread(runnable).start();
		}
	}
}
