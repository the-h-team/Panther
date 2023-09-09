package com.github.sanctum.panther.executable;

import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;
import com.github.sanctum.panther.util.Applicable;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Executable {

	default PantherMap<Command, Method> getCommands() {
		PantherMap<Command, Method> methodMap = new PantherEntryMap<>();
		for (Method m : getClass().getMethods()) {
			try {
				if (!m.isAccessible()) {
					m.setAccessible(true);
				}
				if (m.isAnnotationPresent(Command.class)) {
					Command c = m.getAnnotation(Command.class);
					methodMap.put(c, m);
				}
			} catch (Exception ignored) {
			}
		}
		return methodMap;
	}

	default PantherMap<SubCommand, Method> getSubCommands() {
		PantherMap<SubCommand, Method> methodMap = new PantherEntryMap<>();
		for (Method m : getClass().getMethods()) {
			try {
				if (!m.isAccessible()) {
					m.setAccessible(true);
				}
				if (m.isAnnotationPresent(SubCommand.class)) {
					SubCommand c = m.getAnnotation(SubCommand.class);
					methodMap.put(c, m);
				}
			} catch (Exception ignored) {
			}
		}
		return methodMap;
	}

	interface Inquiry extends Applicable {

		void setUnknownHandler(@NotNull Consumer<Command.Context> consumer);

		boolean canRun();

		boolean runAnyway();

	}

}
