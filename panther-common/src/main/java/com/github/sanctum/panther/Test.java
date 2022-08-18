package com.github.sanctum.panther;

import com.github.sanctum.panther.event.Vent;
import com.github.sanctum.panther.file.JsonAdapter;
import com.github.sanctum.panther.file.Node;
import com.github.sanctum.panther.util.ResourceLookup;
import com.github.sanctum.panther.util.TaskChain;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;

@Vent.Link.Key("Main class")
@Node.Pointer("com.github.sanctum.Test")
public final class Test implements JsonAdapter<Test> {

	String data;

	public Test() {
		this.data = "Borf";
	}

	Test(String d) {
		this.data = d;
	}

	public static void main(String[] args) throws Exception {

		ResourceLookup lookup = new ResourceLookup(Thread.currentThread().getContextClassLoader(), "com.github.sanctum");
		TaskChain.getAsynchronous().wait(() -> {
			Class<?> cl = lookup.getClasses().get(c -> c.getSimpleName().equals("TriadConsumer"));
			System.out.println(cl.getName());
			TaskChain.getAsynchronous().shutdown();
		}, 500);

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
}
