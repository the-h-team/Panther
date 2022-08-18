package com.github.sanctum.panther.util;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * <h3>Goodbye void... Hello deployable!</h3>
 * A modular extension to modifying sourced material or an overall replacement to simply making a method return void.
 *
 * <p>The included functionality this interface provides insinuates that stacked operations within specific time intervals need processing.</p>
 *
 * <p>Using one or more objects finalize data results, <strong>this object can be used as a staging process to object building/handling.</strong></p>
 *
 * @param <T> The data type this deployable references
 */
public interface Deployable<T> {

	/**
	 * Instantly run the relative meta data attached to this deployable skipping queue.
	 *
	 * <p>The usage of this method suggests that the information being passed doesn't need any scheduling and can be provided right now</p>
	 *
	 * @see Deployable#deploy(Consumer)
	 */
	Deployable<T> deploy();

	/**
	 * Instantly run the relative meta data attached to this deployable skipping queue then apply an additional operation
	 * using the sourced material.
	 *
	 * @param consumer The operation to run.
	 * @see Deployable#deploy()
	 */
	Deployable<T> deploy(@NotNull Consumer<? super T> consumer);

	/**
	 * Queue the relative meta data attached to this deployable to run on the next micro tick.
	 *
	 * @apiNote The calculation of "micro ticks" is simply milliseconds.
	 * @see Deployable#queue(long)
	 */
	Deployable<T> queue();

	/**
	 * Queue the relative meta data attached to this deployable to run after a specified interval.
	 *
	 * @param wait The amount of milliseconds to wait.
	 */
	Deployable<T> queue(long wait);

	/**
	 * Queue the relative meta data attached to this deployable to run after a specified interval then apply
	 * an additional operation using the sourced material.
	 *
	 * @param consumer The operation to run after the interval is reached.
	 * @param wait     The amount of milliseconds to wait.
	 * @see Deployable#queue()
	 * @see Deployable#queue(long)
	 */
	Deployable<T> queue(@NotNull Consumer<? super T> consumer, long wait);

	/**
	 * Map the provided source material into an object processor.
	 *
	 * @param mapper The operation to run.
	 * @param <O>    The resulting type.
	 * @return The desired return type.
	 */
	<O> DeployableMapping<O> map(@NotNull Function<? super T, ? extends O> mapper);

	/**
	 * Run a completable future with the attached source material.
	 *
	 * @return A new completable future.
	 */
	CompletableFuture<T> submit();

	/**
	 * Check if this deployable has been deployed.
	 *
	 * @return true if the resulting object of this deployable has already been processed.
	 */
	default boolean isDeployed() {
		return get() != null;
	}

	/**
	 * Complete the deployable information processing and get the object reference.
	 *
	 * @return The object reference from this deployable.
	 */
	default T complete() {
		deploy();
		return submit().join();
	}

	/**
	 * Get the object reference.
	 *
	 * @return The object reference from this deployable.
	 */
	default T get() {
		return complete();
	}

	/**
	 * Get the object reference or provide another one instead.
	 *
	 * @param other The other object to provide.
	 * @return An object reference.
	 */
	default T orElse(T other) {
		return Optional.ofNullable(get()).orElse(other);
	}

	/**
	 * Get the object reference or provide another one instead.
	 *
	 * @param supplier The other object to provide.
	 * @return An object reference.
	 */
	default T orElseGet(Supplier<T> supplier) {
		return Optional.ofNullable(get()).orElseGet(supplier);
	}

	/**
	 * Create a new deployable instance using a runnable.
	 *
	 * @param data The runnable data
	 * @param runtime the runtime to use for task scheduling.
	 * @return A deployable runnable sequence.
	 */
	static @NotNull Deployable<Void> of(@NotNull Runnable data, int runtime) {
		return new Deployable<Void>() {
			final TaskChain taskChain = TaskChain.getChain(runtime);
			boolean deployed = false;
			@Override
			public Deployable<Void> deploy() {
				data.run();
				deployed = true;
				return this;
			}

			@Override
			public Deployable<Void> deploy(@NotNull Consumer<? super Void> consumer) {
				deploy();
				consumer.accept(null);
				return this;
			}

			@Override
			public Deployable<Void> queue() {
				taskChain.run(this::deploy);
				return this;
			}

			@Override
			public Deployable<Void> queue(long wait) {
				taskChain.wait(this::deploy, UUID.randomUUID().toString(), wait);
				return this;
			}

			@Override
			public Deployable<Void> queue(@NotNull Consumer<? super Void> consumer, long wait) {
				taskChain.wait(() -> {
					deploy();
					consumer.accept(null);
				}, UUID.randomUUID().toString(), wait);
				return this;
			}

			@Override
			public <O> DeployableMapping<O> map(@NotNull Function<? super Void, ? extends O> mapper) {
				return null;
			}

			@Override
			public CompletableFuture<Void> submit() {
				return CompletableFuture.supplyAsync(() -> deploy().get());
			}

			@Override
			public boolean isDeployed() {
				return deployed;
			}

			@Override
			public Void get() {
				throw new NullPointerException("No return values used with runnable deployable sequences.");
			}
		};
	}


	/**
	 * Create a new deployable instance using a supplier.
	 *
	 * @param supplier The supplier of data
	 * @param runtime the runtime to use for task scheduling.
	 * @param <T> The type of object being worked with.
	 * @return A deployable supplier sequence.
	 */
	static <T> @NotNull Deployable<T> of(@NotNull Supplier<T> supplier, int runtime) {
		return new Deployable<T>() {
			T element;
			final TaskChain taskChain = TaskChain.getChain(runtime);
			@Override
			public Deployable<T> deploy() {
				this.element = supplier.get();
				return this;
			}

			@Override
			public Deployable<T> deploy(@NotNull Consumer<? super T> consumer) {
				deploy();
				consumer.accept(element);
				return this;
			}

			@Override
			public Deployable<T> queue() {
				taskChain.run(this::deploy);
				return this;
			}

			@Override
			public Deployable<T> queue(long wait) {
				taskChain.wait(this::deploy, wait);
				return this;
			}

			@Override
			public Deployable<T> queue(@NotNull Consumer<? super T> consumer, long wait) {
				taskChain.wait(() -> {
					deploy();
					consumer.accept(element);
				}, wait);
				return this;
			}

			@Override
			public <O> DeployableMapping<O> map(@NotNull Function<? super T, ? extends O> mapper) {
				return new DeployableMapping<>(taskChain, () -> element, (Function<? super Object, ? extends O>) mapper);
			}

			@Override
			public CompletableFuture<T> submit() {
				return CompletableFuture.supplyAsync(() -> deploy().get());
			}

			@Override
			public boolean isDeployed() {
				return element != null;
			}

			@Override
			public T get() {
				if (!isDeployed()) {
					deploy();
					Logger logger = PantherLogger.getInstance().getLogger();
					logger.warning("               !!![WARNING]!!!");
					logger.warning("================================================");
					logger.warning("Illegal sequence retrieval w/ element " + element);
					logger.warning("The ability to do this will be removed in the future!");
					logger.warning("Make the subsequent calls to 'deploy' or 'queue' while processing deployables!");
					logger.warning("================================================");
				}
				return Check.forNull(element, "Sequence not deployed, no object found.");
			}
		};
	}

	/**
	 * Create a new deployable instance using an object instance and consumer.
	 *
	 * @param supplier The object to use modify.
	 * @param operation The object modifier.
	 * @param runtime The runtime to use for task scheduling.
	 * @param <T> The type of object being worked with.
	 * @return A deployable object modifying sequence.
	 */
	static <T> @NotNull Deployable<T> of(@NotNull T supplier, @NotNull Consumer<T> operation, int runtime) {
		return new Deployable<T>() {
			final T element = supplier;
			boolean deployed = false;
			final TaskChain taskChain = TaskChain.getChain(runtime);
			@Override
			public Deployable<T> deploy() {
				operation.accept(element);
				deployed = true;
				return this;
			}

			@Override
			public Deployable<T> deploy(@NotNull Consumer<? super T> consumer) {
				deploy();
				consumer.accept(element);
				return this;
			}

			@Override
			public Deployable<T> queue() {
				taskChain.run(this::deploy);
				return this;
			}

			@Override
			public Deployable<T> queue(long wait) {
				taskChain.wait(this::deploy, UUID.randomUUID().toString(), wait);
				return this;
			}

			@Override
			public Deployable<T> queue(@NotNull Consumer<? super T> consumer, long wait) {
				taskChain.wait(() -> {
					deploy();
					consumer.accept(element);
				}, UUID.randomUUID().toString(), wait);
				return this;
			}

			@Override
			public <O> DeployableMapping<O> map(@NotNull Function<? super T, ? extends O> mapper) {
				return new DeployableMapping<>(taskChain, () -> element, (Function<? super Object, ? extends O>) mapper);
			}

			@Override
			public CompletableFuture<T> submit() {
				return CompletableFuture.supplyAsync(() -> deploy().get());
			}

			@Override
			public boolean isDeployed() {
				return deployed;
			}

			@Override
			public T get() {
				if (!deployed) {
					Logger logger = PantherLogger.getInstance().getLogger();
					logger.warning("               !!![WARNING]!!!");
					logger.warning("================================================");
					logger.warning("Illegal sequence retrieval w/ element " + element);
					logger.warning("The ability to do this will be removed in the future!");
					logger.warning("Make the subsequent calls to 'deploy' or 'queue' while processing deployables!");
					logger.warning("================================================");
					deploy();
				}
				return Check.forNull(element, "Sequence invalid or not deployed, no object found.");
			}
		};
	}


}
