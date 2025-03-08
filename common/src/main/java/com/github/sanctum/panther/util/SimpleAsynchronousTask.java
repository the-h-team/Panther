package com.github.sanctum.panther.util;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import org.jetbrains.annotations.NotNull;

/**
 * A simple asynchronous task execution service, not meant to be publicly used too often.
 */
public final class SimpleAsynchronousTask {

	static final Timer timer = new Timer(true);

	public static void runNow(@NotNull Runnable runnable) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				runnable.run();
			}
		}, 0);
	}

	public static void runLater(@NotNull Runnable runnable, long wait) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				runnable.run();
			}
		}, wait);
	}

	public static void runLater(@NotNull Runnable runnable, Date wait) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				runnable.run();
			}
		}, wait);
	}

}
