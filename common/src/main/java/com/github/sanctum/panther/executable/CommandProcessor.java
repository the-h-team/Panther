package com.github.sanctum.panther.executable;

import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherEntry;
import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;
import com.github.sanctum.panther.container.PantherSet;
import com.github.sanctum.panther.util.Applicable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

public abstract class CommandProcessor {

	// when you register a command it saves the known label as the key, the bukkit impl will save to a collection with a command impl that auto registers.
	private final PantherCollection<Executable> executables = new PantherSet<>();

	public void register(@NotNull Executable executable) {
		// get command class impl, have method ready that wraps the bukkit command execution and transforms output to context
		executables.add(executable);
	}

	public void unregister(@NotNull Executable executable) {
		executables.remove(executable);
	}

	public Executable.Inquiry inquire(@NotNull Command.Context context) {
		String label = context.get(0);
		Executable parentExecutable = null;
		PantherEntry.Modifiable<Command, Method> commandToRun = null;
		PantherMap<Executable, PantherCollection<PantherEntry.Modifiable<SubCommand, Method>>> subCommandsToRun = new PantherEntryMap<>();
		boolean canRun = false;
		for (Executable e : executables) {
			for (PantherEntry.Modifiable<Command, Method> s : e.getCommands()) {
				Command command = s.getKey();
				// check first if label matches
				if (command.aliases()[0].equalsIgnoreCase(label)) {
					commandToRun = s;
					parentExecutable = e;
					boolean multipleArgs = context.length() > 1;
					if (multipleArgs) {
						e.getSubCommands().forEach(in -> {
							SubCommand subCommand = in.getKey();
							// add sub commands of relevance.
							SubCommand.Requirement requirement = null;
							if (in.getValue().isAnnotationPresent(SubCommand.Requirement.class)) {
								requirement = in.getValue().getAnnotation(SubCommand.Requirement.class);
							}
							if (subCommand.command().equalsIgnoreCase(label) && subCommand.pos() < context.length()) {
								if (subCommand.pos() < (Math.max(0, context.length() - 1))) return;
								// we have standard to meet around here.. and its not been met.. THE BOOT WITH YAs
								if (requirement != null && ((requirement.pos() >= context.length()) || !context.get(requirement.pos()).equalsIgnoreCase(requirement.arg())))
									return;
								if (subCommand.pos() >= context.length())
									throw new IllegalStateException("Sub-command position cannot be larger than or equal to context length.");
								if (context.get(subCommand.pos()).equalsIgnoreCase(subCommand.aliase())) {
									if (!subCommandsToRun.containsKey(e)) {
										PantherCollection<PantherEntry.Modifiable<SubCommand, Method>> methods = new PantherSet<>();
										methods.add(in);
										subCommandsToRun.put(e, methods);
									} else {
										subCommandsToRun.get(e).add(in);
									}
								}
							}
						});
					}
					// check if the provided context is large enough for the command itself.
					if (context.length() >= command.min()) {
						// check if command has no bounds or matches bounds
						if (command.max() == -1 || context.length() <= command.max()) {
							canRun = true;
						}
					}
				}
			}
		}
		boolean finalCanRun = canRun;
		PantherEntry.Modifiable<Command, Method> finalCommandToRun = commandToRun;
		Executable finalParentExecutable = parentExecutable;
		return new Executable.Inquiry() {

			Consumer<Command.Context> unknownApplicant;

			@Override
			public void run() {
				// select subcommand or select and run the command
				if (subCommandsToRun.isEmpty()) {
					// run command only
					if (finalCommandToRun != null) {
						if (context.length() > 1 && unknownApplicant != null) {
							unknownApplicant.accept(context);
							return;
						}
						try {
							finalCommandToRun.getValue().invoke(finalParentExecutable, context);
						} catch (IllegalAccessException | InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				} else {
					subCommandsToRun.forEach(e -> {
						Executable executable = e.getKey();
						PantherCollection<PantherEntry.Modifiable<SubCommand, Method>> collection = e.getValue();
						collection.forEach(sub -> {
							try {
								sub.getValue().invoke(executable, context);
							} catch (IllegalAccessException | InvocationTargetException ex) {
								ex.printStackTrace();
							}
						});
					});

				}
			}

			@Override
			public void setUnknownHandler(@NotNull Consumer<Command.Context> consumer) {
				this.unknownApplicant = consumer;
			}

			@Override
			public boolean canRun() {
				return finalCanRun; // find way to check permissions
			}

			@Override
			public boolean runAnyway() {
				// run the command, return false if can't and report why
				try {
					run();
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		};
	}

}
