package com.github.sanctum.panther.util;

import com.github.sanctum.panther.annotation.Note;
import com.github.sanctum.panther.annotation.Voluntary;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class for serializing/deserializing object's with support for custom serialization/deserialization!
 */
public class HFEncoded {

	private final Object obj;
	Class<? extends ObjectOutputStream> output = ObjectOutputStream.class;
	Class<? extends ObjectInputStream> input = ObjectInputStream.class;

	/**
	 * Convert the entire object into a string while retaining all of its values.
	 *
	 * <p>WARNING: Making changes to objects then attempting to attach/reuse older un-modified objects
	 * could have negative effects, ensure you have proper object handling when dealing with serialization.</p>
	 *
	 * @param obj a Serializable object (Java Serializable and/or Bukkit's ConfigurationSerializable)
	 */
	public HFEncoded(Object obj) {
		this.obj = obj;
	}

	@Note("Delegation for both serialization & deserialization transactions")
	public static HFEncoded of(@NotNull Object obj) {
		return new HFEncoded(obj);
	}

	@Voluntary("Set a custom output stream handler.")
	public @NotNull HFEncoded setOutput(@NotNull Class<? extends ObjectOutputStream> clazz) {
		this.output = clazz;
		return this;
	}

	@Voluntary("Set a custom input stream handler.")
	public @NotNull HFEncoded setInput(@NotNull Class<? extends ObjectInputStream> clazz) {
		this.input = clazz;
		return this;
	}

	/**
	 * Convert the object into a byte array using base 64 encryption.
	 *
	 * @return The inputted object as a byte array
	 */
	public byte[] toByteArray() {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			Constructor<? extends ObjectOutputStream> constructor = this.output.getDeclaredConstructor(OutputStream.class);
			constructor.setAccessible(true);
			ObjectOutputStream outputStream = constructor.newInstance(output);
			outputStream.writeObject(obj);
			outputStream.flush();
			return output.toByteArray();
		} catch (IOException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException("This should never happen", e);
		}
	}

	/**
	 * The original stored object retaining all values converted to a string.
	 *
	 * @return a serialized, encoded form of this object with retained values
	 * @throws IllegalStateException if unable to write the object
	 */
	public String serialize() throws IllegalStateException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			Constructor<? extends ObjectOutputStream> constructor = this.output.getDeclaredConstructor(OutputStream.class);
			constructor.setAccessible(true);
			ObjectOutputStream outputStream = constructor.newInstance(output);
			outputStream.writeObject(obj);
			outputStream.flush();
		} catch (IOException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException("This should never happen", e);
		}

		byte[] serial = output.toByteArray();
		return Base64.getEncoder().encodeToString(serial);
	}

	/**
	 * If the provided object is an encoded byte array deserialize it back into its object state.
	 *
	 * @return The object the byte array once was or null if not an encoded byte array.
	 * @throws IOException            if an I/O error occurs while reading stream heade
	 * @throws ClassNotFoundException if the class of the serialized object cannot be found
	 */
	public <T> T fromByteArray() throws IOException, ClassNotFoundException {
		if (obj == null || !byte[].class.isAssignableFrom(obj.getClass())) return null;
		byte[] ar = (byte[]) obj;
		ByteArrayInputStream input = new ByteArrayInputStream(ar);
		ObjectInputStream inputStream = new ObjectInputStream(input);
		if (this.input != null) {
			try {
				Constructor<? extends ObjectInputStream> constructor = this.input.getDeclaredConstructor(InputStream.class);
				constructor.setAccessible(true);
				inputStream = constructor.newInstance(input);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new IllegalStateException("This should never happen", e);
			}
		}
		return (T) inputStream.readObject();

	}

	/**
	 * The original stored object retaining all values converted back to an object.
	 * <p>
	 * WARN: You will need to pass a type to the object upon use.
	 *
	 * @param lookups The individual class initializers to use.
	 * @return the deserialized form of an object with its original state
	 * @throws IOException            typically, if a class has been modified in comparison
	 *                                to its original structure
	 * @throws ClassNotFoundException if the class could not be located properly
	 */
	public Object deserialized(ClassLookup... lookups) throws IOException, ClassNotFoundException {
		byte[] serial = Base64.getDecoder().decode(obj.toString());
		ByteArrayInputStream input = new ByteArrayInputStream(serial);
		ObjectInputStream inputStream = new ObjectInputStream(input);
		if (this.input != null) {
			try {
				Constructor<? extends ObjectInputStream> constructor = this.input.getDeclaredConstructor(InputStream.class);
				constructor.setAccessible(true);
				inputStream = constructor.newInstance(input);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new IllegalStateException("This should never happen", e);
			}
		}
		for (ClassLookup l : lookups) {
			if (inputStream instanceof ClassLookup.Input) {
				((ClassLookup.Input) inputStream).add(l);
			}
		}
		return inputStream.readObject();
	}

	/**
	 * If the provided object is an encoded byte array deserialize it back into its object state.
	 *
	 * @param lookups The individual class initializers to use.
	 * @return The object the byte array once was or null if not an encoded byte array.
	 * @throws IOException            if an I/O error occurs while reading stream heade
	 * @throws ClassNotFoundException if the class of the serialized object cannot be found
	 */
	public <T> T fromByteArray(ClassLookup... lookups) throws IOException, ClassNotFoundException {
		if (obj == null || !byte[].class.isAssignableFrom(obj.getClass())) return null;
		byte[] ar = (byte[]) obj;
		ByteArrayInputStream input = new ByteArrayInputStream(ar);
		ObjectInputStream inputStream = new ObjectInputStream(input);
		if (this.input != null) {
			try {
				Constructor<? extends ObjectInputStream> constructor = this.input.getDeclaredConstructor(InputStream.class);
				constructor.setAccessible(true);
				inputStream = constructor.newInstance(input);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new IllegalStateException("This should never happen", e);
			}
		}
		for (ClassLookup l : lookups) {
			if (inputStream instanceof ClassLookup.Input) {
				((ClassLookup.Input) inputStream).add(l);
			}
		}
		return (T) inputStream.readObject();

	}

	/**
	 * The original stored object retaining all values converted back to an object.
	 * <p>
	 * WARN: You will need to pass a type to the object upon use.
	 *
	 * @return the deserialized form of an object with its original state
	 * @throws IOException            typically, if a class has been modified in comparison
	 *                                to its original structure
	 * @throws ClassNotFoundException if the class could not be located properly
	 */
	public Object deserialized() throws IOException, ClassNotFoundException {
		byte[] serial = Base64.getDecoder().decode(obj.toString());
		ByteArrayInputStream input = new ByteArrayInputStream(serial);
		ObjectInputStream inputStream = new ObjectInputStream(input);
		if (this.input != null) {
			try {
				Constructor<? extends ObjectInputStream> constructor = this.input.getDeclaredConstructor(InputStream.class);
				constructor.setAccessible(true);
				inputStream = constructor.newInstance(input);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new IllegalStateException("This should never happen", e);
			}
		}
		return inputStream.readObject();
	}

	/**
	 * If the provided object is an encoded byte array deserialize it back into its object state.
	 *
	 * @param classLoader The classloader to use for deserialization.
	 * @return The object the byte array once was or null if not an encoded byte array.
	 * @throws IOException            if an I/O error occurs while reading stream heade
	 * @throws ClassNotFoundException if the class of the serialized object cannot be found
	 */
	public <T> T fromByteArray(@NotNull ClassLoader classLoader) throws IOException, ClassNotFoundException {
		if (obj == null || !byte[].class.isAssignableFrom(obj.getClass())) return null;
		byte[] ar = (byte[]) obj;
		ByteArrayInputStream input = new ByteArrayInputStream(ar);
		ObjectInputStream inputStream = new ObjectInputStream(input);
		if (this.input != null) {
			try {
				Constructor<? extends ObjectInputStream> constructor = this.input.getDeclaredConstructor(InputStream.class);
				constructor.setAccessible(true);
				inputStream = constructor.newInstance(input);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new IllegalStateException("This should never happen", e);
			}
		}
		if (inputStream instanceof LoaderInput) {
			((LoaderInput) inputStream).setLoader(classLoader);
		}
		return (T) inputStream.readObject();

	}

	/**
	 * The original stored object retaining all values converted back to an object.
	 * <p>
	 * WARN: You will need to pass a type to the object upon use.
	 *
	 * @param classLoader The classloader to use for deserialization.
	 * @return the deserialized form of an object with its original state
	 * @throws IOException            typically, if a class has been modified in comparison
	 *                                to its original structure
	 * @throws ClassNotFoundException if the class could not be located properly
	 */
	public Object deserialized(@NotNull ClassLoader classLoader) throws IOException, ClassNotFoundException {
		byte[] serial = Base64.getDecoder().decode(obj.toString());
		ByteArrayInputStream input = new ByteArrayInputStream(serial);
		ObjectInputStream inputStream = new ObjectInputStream(input);
		if (this.input != null) {
			try {
				Constructor<? extends ObjectInputStream> constructor = this.input.getDeclaredConstructor(InputStream.class);
				constructor.setAccessible(true);
				inputStream = constructor.newInstance(input);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new IllegalStateException("This should never happen", e);
			}
		}
		if (inputStream instanceof LoaderInput) {
			((LoaderInput) inputStream).setLoader(classLoader);
		}
		return inputStream.readObject();
	}

	/**
	 * Deserialize an object of specified type from a string.
	 * <p>
	 * Primarily for misc use, deserialization is handled internally for normal object use from containers.
	 *
	 * @param type the type this object represents
	 * @param <R>  the type this object represents
	 * @return a deserialized object or null
	 */
	public <R> @Nullable R deserialize(@NotNull Class<R> type) {
		try {
			Object o = deserialized();
			if (o == null) return null;
			if (type.isAssignableFrom(o.getClass())) {
				return (R) o;
			} else {
				throw new IllegalArgumentException(o.getClass().getSimpleName() + " is not assignable from " + type.getSimpleName());
			}
		} catch (IOException | ClassNotFoundException e) {
			PantherLogger.getInstance().getLogger().severe("- " + e.getMessage());
		}
		return null;
	}

	/**
	 * Deserialize an object of specified type from a string.
	 * <p>
	 * Primarily for misc use, deserialization is handled internally for normal object use from containers.
	 *
	 * @param classLoader The classloader to use for deserialization.
	 * @param type        the type this object represents
	 * @param <R>         the type this object represents
	 * @return a deserialized object or null
	 */
	public <R> @Nullable R deserialize(@NotNull Class<R> type, @NotNull ClassLoader classLoader) {
		try {
			Object o = deserialized(classLoader);
			if (o == null) return null;
			if (type.isAssignableFrom(o.getClass())) {
				return (R) o;
			} else {
				throw new IllegalArgumentException(o.getClass().getSimpleName() + " is not assignable from " + type.getSimpleName());
			}
		} catch (IOException | ClassNotFoundException e) {
			PantherLogger.getInstance().getLogger().severe("- " + e.getMessage());
		}
		return null;
	}

	/**
	 * A delegation interface for assigning alternative classloader provision.
	 */
	public interface LoaderInput {

		void setLoader(@NotNull ClassLoader classLoader);

	}

	@FunctionalInterface
	public interface ClassLookup {

		Class<?> accept(@NotNull String className);

		/**
		 * A delegation interface for input streams that require alternative class locating.
		 */
		interface Input {

			void add(@NotNull ClassLookup lookup);

		}

	}

}
