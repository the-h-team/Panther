package com.github.sanctum.panther.executable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jetbrains.annotations.NotNull;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

	String[] aliases();

	String usage() default "";

	String help() default "";

	String description();

	int min() default 0;

	int max() default -1;

	String permission() default "";

	String[] args() default "";

	interface Context {

		Executor DEFAULT = new PantherExecutor();

		@NotNull Executor getExecutor();

		String get(int index);

		String[] get();

		int length();

		static @NotNull Context of(@NotNull String text) {
			return new Context() {

				@Override
				public @NotNull Executor getExecutor() {
					return DEFAULT;
				}

				@Override
				public String get(int index) {
					return text.split(" ")[index];
				}

				@Override
				public String[] get() {
					return text.split(" ");
				}

				@Override
				public int length() {
					return get().length;
				}
			};
		}

	}

}
