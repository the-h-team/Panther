package com.github.sanctum.panther;

import com.github.sanctum.panther.event.Vent;
import com.github.sanctum.panther.file.JsonAdapter;
import com.github.sanctum.panther.file.Node;
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
		Test tes = new Test();
		Configurable.Editor editor = Configurable.view(tes).get("test", Configurable.Type.JSON);
		editor.write(t -> {
			t.set("Steve.farts", 32);
			t.set("Steve.eggs", 4);
			t.set("Steve.bio", "Haha");
		});
		Configurable.Editor editor2 = Configurable.view(tes).get("test", Configurable.Type.JSON);

		System.out.println("Done.");
		 */
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
