package com.github.sanctum.panther;

import com.github.sanctum.panther.event.Vent;
import com.github.sanctum.panther.event.VentMap;
import com.github.sanctum.panther.file.JsonAdapter;
import com.github.sanctum.panther.recursive.Service;
import com.google.gson.JsonElement;
import java.util.Map;

@Vent.Link.Key("Main class")
public final class Test implements Vent.Host, JsonAdapter<Service> {

	Test() {
	}

	public static void main(String[] args) {


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
