package com.github.sanctum.panther;

import com.github.sanctum.panther.event.Vent;
import com.github.sanctum.panther.event.VentMap;
import com.github.sanctum.panther.file.JsonAdapter;
import com.github.sanctum.panther.file.JsonConfiguration;
import com.github.sanctum.panther.recursive.Service;
import com.google.gson.JsonElement;
import java.io.File;
import java.util.Map;

@Vent.Link.Key("Main class")
public final class Test implements Vent.Host, JsonAdapter<Service> {

	public static void main(String[] args) {

		File f = new File("items");

		JsonConfiguration c = new JsonConfiguration(f, "data", null);


	}

	@Override
	public JsonElement write(Service service) {
		return null;
	}

	@Override
	public Service read(Map<String, Object> object) {
		return null;
	}

	@Override
	public Class<? extends Service> getSerializationSignature() {
		return VentMap.class;
	}
}
