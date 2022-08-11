package com.github.sanctum.panther.util;

import com.github.sanctum.panther.annotation.Comment;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

@Comment("A delegate to deployable interfacing, conforming one object type into another.")
public final class DeployableMapping<R> implements Deployable<R> {

	private final Function<? super Object, ? extends R> function;
	private final Object parent;
	private final TaskChain chain;
	private R value;

	DeployableMapping(@NotNull TaskChain chain, @NotNull Supplier<Object> o, @NotNull Function<? super Object, ? extends R> function) {
		this.function = function;
		this.chain = chain;
		this.parent = o.get();
	}

	@Override
	public R get() {
		return value;
	}

	@Override
	public DeployableMapping<R> deploy() {
		if (this.value == null) {
			this.value = function.apply(this.parent);
		}
		return this;
	}

	@Override
	public DeployableMapping<R> deploy(@NotNull Consumer<? super R> consumer) {
		if (this.value == null) {
			this.value = function.apply(this.parent);
		}
		consumer.accept(this.value);
		return this;
	}

	@Override
	public DeployableMapping<R> queue() {
		chain.run(this::deploy);
		return this;
	}

	@Override
	public DeployableMapping<R> queue(long wait) {
		chain.wait(this::queue, wait);
		return this;
	}

	@Override
	public DeployableMapping<R> queue(@NotNull Consumer<? super R> consumer, long wait) {
		chain.wait(() -> {
			queue();
			consumer.accept(this.value);
		}, wait);
		return this;
	}

	@Override
	public <O> DeployableMapping<O> map(@NotNull Function<? super R, ? extends O> mapper) {
		return new DeployableMapping<>(chain, () -> deploy().get(), (Function<? super Object, ? extends O>) mapper);
	}

	@Override
	public CompletableFuture<R> submit() {
		return CompletableFuture.supplyAsync(() -> deploy().get());
	}
}
