package com.github.sanctum.panther.util;

import java.io.Serializable;
import org.jetbrains.annotations.NotNull;

/**
 * Use this functional interface to form lambdas/references that execute on run time for you.
 * Either passing values or running code, this can come in handy and also extends functionality from the Serializable interface.
 *
 * @author Hempfest
 */
@FunctionalInterface
public interface Applicable extends Runnable, Serializable {

	@Override
	void run();

	/**
	 * Creates a new applicable that will run this applicable before a provided one.
	 */
	default @NotNull Applicable applyBefore(@NotNull Applicable applicable) {
		return () -> {
			run();
			applicable.run();
		};
	}

	/**
	 * Creates a new applicable that will run this applicable after a provided one.
	 */
	default @NotNull Applicable applyAfter(@NotNull Applicable applicable) {
		return () -> {
			applicable.run();
			run();
		};
	}

	/**
	 * Schedule this applicable to run asynchronously after the specified interval.
	 *
	 * @param milliseconds the interval in which to execute this applicable.
	 * @return a task holding delegated applicable information.
	 */
	default @NotNull Task schedule(long milliseconds) {
		final TaskChain chain = TaskChain.getAsynchronous();
		final String key = "ApplicableTask;" + hashCode();
		return chain.wait(this, key, milliseconds).get(key);
	}

	/**
	 * Schedule this applicable to run asynchronously always.
	 *
	 * @param delay the initial delay for task execution.
	 * @param period the period in which to repeat the execution
	 * @return a task holding delegated applicable information.
	 */
	default @NotNull Task scheduleAlways(long delay, long period) {
		final TaskChain chain = TaskChain.getAsynchronous();
		final String key = "ApplicableTask;" + hashCode();
		return chain.repeat(this, key, delay, period).get(key);
	}
}