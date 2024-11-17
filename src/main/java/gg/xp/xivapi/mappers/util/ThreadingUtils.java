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
}
