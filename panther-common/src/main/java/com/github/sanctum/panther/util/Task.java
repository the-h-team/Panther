package com.github.sanctum.panther.util;

import com.github.sanctum.panther.annotation.Note;
import com.github.sanctum.panther.annotation.Synchronized;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherList;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.Future;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

@Note("This class requires a no argument method with ordinal level 0")
public abstract class Task implements Applicable {

	public static final TypeAdapter<Task> FLAG = () -> Task.class;

	public static final int SINGULAR = 0;
	public static final int REPEATABLE = 1;
	private static final long serialVersionUID = 5781615452181138418L;

	protected TaskChain parent;
	private final PantherCollection<TaskPredicate<Task>> predicates = new PantherList<>();
	private final OrdinalProcedure<Task> ordinal;
	private final String key;
	private Future<?> future;
	private Synchronizer synchronizer;
	private Runnable runnable;
	private final int type;
	private boolean async;

	public Task(String key, @NotNull TaskChain parent) {
		this.type = SINGULAR;
		this.key = key;
		this.parent = parent;
		this.ordinal = OrdinalProcedure.of(this);
	}

	public Task(String key, @NotNull TaskChain parent, @NotNull Runnable runnable) {
		this.type = SINGULAR;
		this.key = key;
		this.runnable = runnable;
		this.parent = parent;
		this.ordinal = OrdinalProcedure.of(this);
	}

	public Task(String key, @MagicConstant(intValues = {SINGULAR, REPEATABLE}) int type, @NotNull TaskChain parent) {
		this.parent = parent;
		this.type = type;
		this.key = key;
		this.ordinal = OrdinalProcedure.of(this);
	}

	public Task(String key, @MagicConstant(intValues = {SINGULAR, REPEATABLE}) int type, @NotNull TaskChain parent, @NotNull Runnable runnable) {
		this.parent = parent;
		this.type = type;
		this.key = key;
		this.runnable = runnable;
		this.ordinal = OrdinalProcedure.of(this);
	}

	public final Task setSynchronizer(@NotNull Synchronizer synchronizer) {
		this.synchronizer = synchronizer;
		return this;
	}

	public final Task setAsync(boolean async) {
		this.async = async;
		return this;
	}

	public final Task setChain(@NotNull TaskChain parent) {
		this.parent = parent;
		return this;
	}

	public final void setFuture(@NotNull Future<?> future) {
		this.future = future;
	}

	public final Task listen(@NotNull TaskPredicate<?>... predicates) {
		for (TaskPredicate<?> p : predicates) {
			this.predicates.add((TaskPredicate<Task>) p);
		}
		return this;
	}

	public final String getKey() {
		return this.key;
	}

	public final boolean isCancelled() {
		return future != null && future.isCancelled();
	}

	public final boolean isDone() {
		return future != null && future.isDone();
	}

	public final boolean isAsync() {
		return async;
	}

	public final <T extends Task> T cast(TypeAdapter<T> flag) {
		return flag.cast(this);
	}

	public final boolean isRepeatable() {
		return getClass().isAnnotationPresent(Period.class);
	}

	@Override
	public final void run() {
		try {
			final Runnable r = runnable != null ? () -> {
				if (!predicates.isEmpty() && predicates.stream().anyMatch(p -> !p.accept(this))) return;
				runnable.run();
			} : () -> {
				if (!predicates.isEmpty() && predicates.stream().anyMatch(p -> !p.accept(this))) return;
				ordinal.run(0);
			};
			if (isAsync()) {
				r.run();
			} else {
				synchronizer.sync(r);
			}
			if (type == Task.SINGULAR && parent != null) {
				parent.map.remove(getKey());
			}
		} catch (Exception e) {
			e.printStackTrace();
			cancel();
		}
	}

	/**
	 * IMMEDIATELY stop this task in its tracks.
	 */
	public final boolean cancel() {
		if (!future.isCancelled()) {
			if (parent != null) parent.map.remove(this.key);
			return future.cancel(true);
		}
		return false;
	}

	/**
	 * A flag for specific delay on a marked task.
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(value = {ElementType.TYPE, ElementType.TYPE_USE})
	public @interface Delay {

		long value() default 0L;

	}

	/**
	 * A flag for specific period of execution on a marked task.
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(value = {ElementType.TYPE, ElementType.TYPE_USE})
	public @interface Period {

		long value() default 0L;

	}

	public interface Synchronizer {

		@Synchronized void sync(@NotNull Runnable runnable);

	}

}
