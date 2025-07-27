package com.github.sanctum.panther;

import com.github.sanctum.panther.event.Vent;
import com.github.sanctum.panther.executable.Command;
import com.github.sanctum.panther.executable.CommandProcessor;
import com.github.sanctum.panther.executable.Executable;
import com.github.sanctum.panther.executable.TestExecutable;
import com.github.sanctum.panther.file.JsonAdapter;
import com.github.sanctum.panther.file.Node;
import com.github.sanctum.panther.util.EasyTypeAdapter;
import com.github.sanctum.panther.util.HUID;
import com.github.sanctum.panther.util.TypeAdapter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

@Vent.Link.Key("Main class")
@Node.Pointer("com.github.sanctum.Test")
public final class Test implements JsonAdapter<Test>, Vent.Host {

	String data;

	public Test() {
		this.data = "Borf";
	}

	Test(String d) {
		this.data = d;
	}

	public static void main(String[] args) throws Exception {
		/*
		CommandProcessor processor = new CommandProcessor() {
		};
		processor.register(new TestExecutable());
		Executable.Inquiry inquiry = processor.inquire(Command.Context.of("clan base"));
		inquiry.setUnknownHandler(context -> {
			System.out.println("You used an unknown clan sub-command. Please refer to 'clan help'");
		});
		if (inquiry.canRun()) {
			inquiry.run();
		}
		 */
		ClanTest test = new ClanTest();
		String string = test.getValue("port.now");
		System.out.println("Got adapter " + string);
	}

	@Override
	public JsonElement write(Test test) {
		JsonObject o = new JsonObject();
		o.addProperty("data", data);
		return o;
	}

	@Override
	public Test read(Map<String, Object> object) {
		String d = object.get("data").toString();
		return new Test(d);
	}

	@Override
	public Class<? extends Test> getSerializationSignature() {
		return Test.class;
	}

	@Override
	public @NotNull String getName() {
		return "TestApp";
	}

	@Override
	public @NotNull File getDataFolder() {
		return new File("testing");
	}
}
