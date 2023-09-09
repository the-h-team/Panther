package com.github.sanctum.panther.executable;

public class TestExecutable implements Executable {

	@Command(aliases = "clan", description = "A clan command", usage = "clan [sub-command]", max = 3)
	public void test(Command.Context context) {
		Executor e = context.getExecutor();
		System.out.println("Executor " + e.getName() + " used the clan command");
	}


	@SubCommand(aliase = "info", command = "clan")
	public void info(Command.Context context) {
		Executor e = context.getExecutor();
		String[] args = context.get();
		System.out.println("This is your clan info!");
	}

	@SubCommand(aliase = "base", command = "clan")
	public void base(Command.Context context) {
		System.out.println("Clan base bitch!");
	}

	@SubCommand(aliase = "base", command = "clan", pos = 2)
	@SubCommand.Requirement(arg = "set", pos = 1)
	public void setBase(Command.Context context) {
		Executor e = context.getExecutor();
		String[] args = context.get();
		System.out.println("Executor " + e.getName() + " set the new base!!");
	}

}
