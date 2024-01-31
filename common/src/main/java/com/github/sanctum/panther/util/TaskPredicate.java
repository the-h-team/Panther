package com.github.sanctum.panther.util;

import java.util.function.Function;

/**
 * An interface responsible for deciding whether a task can either continue to execute or get cancelled.
 *
 * Returning false within a task predicate will stop the initial task from executing again but to fully cancel it make sure to run {@link Task#cancel()}
 *
 * @param <T> The type of task.
 */
@FunctionalInterface
public interface TaskPredicate<T extends Task> {

	boolean accept(T task);

	static <T extends Task> TaskPredicate<T> cancelAfter(int count) {
		return new TaskPredicate<T>() {
			private int i = count;

			@Override
			public boolean accept(Task task) {
				if (i == 0) {
					task.cancel();
					return false;
				}
				i--;
				return true;
			}
		};
	}

	static <T extends Task> TaskPredicate<T> cancelAfter(Function<T, Boolean> consumer) {
		return consumer::apply;
	}

}
