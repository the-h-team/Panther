package com.github.sanctum.panther.file;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.util.Map;

/**
 * An object capable of Json serialization & deserialization.
 * <p>
 * Annotated with {@link Node.Pointer} location information.
 *
 * @param <T> The object type this serializer represents.
 * @author Hempfest
 * @version 1.0
 */
public interface JsonAdapter<T> {

	/**
	 * Serialize the corresponding element for json.
	 *
	 * @param t The object to serialize.
	 * @return The serialized json object.
	 */
	JsonElement write(T t);

	/**
	 * Deserialize the corresponding value from its map into a fresh instance.
	 *
	 * @param object The map of information to read from.
	 * @return The deserialized object.
	 */
	T read(Map<String, Object> object);

	/**
	 * Get the type of class this adapter works with.
	 *
	 * @return the type of class this adapter works with.
	 */
	Class<? extends T> getSerializationSignature();

	/**
	 * @return a json modification object builder.
	 */
	static GsonBuilder getJsonBuilder() {
		GsonBuilder builder = new GsonBuilder();
		Configurable.serializers.forEach((key, value) -> builder.registerTypeHierarchyAdapter(value.getSerializationSignature(), value));
		return builder;
	}

	/**
	 * @see Configurable#registerClass(Class)
	 */
	static void register(Class<? extends JsonAdapter<?>> adapterClass) {
		Configurable.registerClass(adapterClass);
	}

	/**
	 * @see Configurable#registerClass(Class, Object...)
	 */
	static void register(Class<? extends JsonAdapter<?>> adapterClass, Object... args) {
		Configurable.registerClass(adapterClass, args);
	}


	/**
	 * A placeholder implementation for a non existent class pointer.
	 */
	interface Dummy extends JsonAdapter<Object> {}
}
