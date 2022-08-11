package com.github.sanctum.panther.util;

import com.github.sanctum.panther.annotation.Ordinal;
import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public abstract class TaskChain {

	private static final PantherMap<Integer, TaskChain> chainMap = new PantherEntryMap<>();
	protected final PantherMap<String, Task> map = new PantherEntryMap<>();
	protected final ScheduledExecutorService defaultTimer = Executors.newSingleThreadScheduledExecutor();

	// apply async task chain. No provision needed as default is light-weight.
	static {
		chainMap.put(1, new TaskChain() {

			@Override
			public @NotNull TaskChain run(final @NotNull Task task) {
				task.parent = this;
				task.setFuture(defaultTimer.submit(task.setAsync(true)));
				return this;
			}

			@Override
			public @NotNull TaskChain run(final @NotNull Runnable data) {
				defaultTimer.submit(data);
				return this;
			}

			@Override
			public @NotNull TaskChain wait(@NotNull Task task) {
				long time;
				if (task.getClass().isAnnotationPresent(Task.Delay.class)) {
					time = task.getClass().getAnnotation(Task.Delay.class).value();
				} else throw new IllegalStateException("Task Delay annotation missing!");
				return wait(task, time);
			}

			@Override
			public @NotNull TaskChain wait(final @NotNull Task task, long delay) {
				task.parent = this;
				task.setFuture(defaultTimer.scheduleWithFixedDelay(task.setAsync(true), delay, delay, TimeUnit.MILLISECONDS));
				if (task.getKey() != null) {
					map.put(task.getKey(), task);
				}
				return this;
			}

			@Override
			public @NotNull TaskChain wait(@NotNull Runnable data, long delay) {
				return wait(data, UUID.randomUUID().toString(), delay);
			}

			@Override
			public @NotNull TaskChain wait(final @NotNull Runnable data, @NotNull String key, long delay) {
				Task task = new Task(key, Task.SINGULAR, this) {
					private static final long serialVersionUID = 5064153492626085962L;

					@Ordinal
					public void execute() {
						data.run();
					}
				};
				task.setFuture(defaultTimer.scheduleWithFixedDelay(task.setAsync(true), delay, delay, TimeUnit.MILLISECONDS));
				if (task.getKey() != null) {
					map.put(task.getKey(), task);
				}
				return this;
			}

			@Override
			public @NotNull TaskChain repeat(@NotNull Task task) {
				long delay;
				long period;
				if (task.getClass().isAnnotationPresent(Task.Delay.class)) {
					delay = task.getClass().getAnnotation(Task.Delay.class).value();
				} else throw new IllegalStateException("Task Delay annotation missing!");
				if (task.getClass().isAnnotationPresent(Task.Period.class)) {
					period = task.getClass().getAnnotation(Task.Period.class).value();
				} else throw new IllegalStateException("Task Period annotation missing!");
				return repeat(task, delay, period);
			}

			@Override
			public @NotNull TaskChain repeat(final @NotNull Task task, long delay, long period) {
				if (!map.containsKey(task.getKey())) {
					task.setFuture(defaultTimer.scheduleAtFixedRate(task.setAsync(true), delay, period, TimeUnit.MILLISECONDS));
					if (task.getKey() != null) {
						map.put(task.getKey(), task);
					}
				}
				return this;
			}

			@Override
			public @NotNull TaskChain repeat(@NotNull Runnable task, long delay, long period) {
				return repeat(task, UUID.randomUUID().toString(), delay, period);
			}

			@Override
			public @NotNull TaskChain repeat(@NotNull Runnable data, @NotNull String key, long delay, long period) {
				Task task = new Task(key, Task.SINGULAR, this) {
					private static final long serialVersionUID = 5064153492626085962L;

					@Ordinal
					public void execute() {
						data.run();
					}
				};
				task.setFuture(defaultTimer.scheduleAtFixedRate(task.setAsync(true), delay, period, TimeUnit.MILLISECONDS));
				map.put(key, task);
				return this;
			}

			@Override
			public @NotNull <T> Future<T> submit(@NotNull Callable<T> data) {
				return defaultTimer.submit(data);
			}

			@Override
			public @NotNull <T> Future<T> submit(@NotNull Callable<T> data, long delay) {
				return defaultTimer.schedule(data, delay, TimeUnit.MILLISECONDS);
			}

			@Override
			public @NotNull <T> List<Future<T>> submit(@NotNull Collection<Callable<T>> data, long delay) throws InterruptedException {
				return defaultTimer.invokeAll(data, delay, TimeUnit.MILLISECONDS);
			}

			@Override
			public boolean shutdown() {
				if (!map.isEmpty()) {
					map.values().forEach(Task::cancel);
					map.clear();
					defaultTimer.shutdown();
					return true;
				}
				return false;
			}

			@Override
			public Task get(String key) {
				return map.get(key);
			}
		});
	}

	public abstract @NotNull TaskChain run(final @NotNull Task task);

	public abstract @NotNull TaskChain run(final @NotNull Runnable data);

	public abstract @NotNull TaskChain wait(final @NotNull Task task);

	public abstract @NotNull TaskChain wait(final @NotNull Task task, long delay);

	public abstract @NotNull TaskChain wait(final @NotNull Runnable data, long delay);

	public abstract @NotNull TaskChain wait(final @NotNull Runnable data, @NotNull String key, long delay);

	public abstract @NotNull TaskChain repeat(final @NotNull Task task);

	public abstract @NotNull TaskChain repeat(final @NotNull Task task, long delay, long period);

	public abstract @NotNull TaskChain repeat(final @NotNull Runnable task, long delay, long period);

	public abstract @NotNull TaskChain repeat(final @NotNull Runnable task, @NotNull String key, long delay, long period);

	public abstract <T> @NotNull Future<T> submit(final @NotNull Callable<T> data);

	public abstract <T> @NotNull Future<T> submit(final @NotNull Callable<T> data, long delay);

	public abstract <T> @NotNull List<Future<T>> submit(final @NotNull Collection<Callable<T>> data, long delay) throws InterruptedException;

	public abstract boolean shutdown();

	public abstract Task get(String key);

	public static void setChain(int runtime, @NotNull TaskChain chain) {
		chainMap.put(runtime, chain);
	}

	public static TaskChain getAsynchronous() {
		return getChain(1);
	}

	public static TaskChain getSynchronous() {
		return getChain(0);
	}

	public static TaskChain getChain(int runtime) {
		return chainMap.get(runtime);
	}


}
