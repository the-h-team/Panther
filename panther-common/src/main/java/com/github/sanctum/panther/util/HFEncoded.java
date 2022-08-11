package com.github.sanctum.panther.util;

import com.github.sanctum.panther.annotation.Note;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class for serializing/deserializing object's with support for custom serialization/deserialization!
 */
public class HFEncoded {

	private final Object obj;

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

	/**
	 * Convert the object into a byte array using base 64 encryption.
	 *
	 * @return The inputted object as a byte array
	 */
	public byte[] toByteArray(@Nullable Function<ByteArrayOutputStream, ObjectOutputStream> function) {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ObjectOutputStream outputStream;
			if (function != null) {
				outputStream = function.apply(output);
			} else {
				outputStream = new ObjectOutputStream(output);
			}
			outputStream.writeObject(obj);
			outputStream.flush();
			return output.toByteArray();
		} catch (IOException e) {
			throw new IllegalStateException("This should never happen", e);
		}
	}

	/**
	 * The original stored object retaining all values converted to a string.
	 *
	 * @return a serialized, encoded form of this object with retained values
	 * @throws IllegalStateException if unable to write the object
	 */
	public String serialize(@Nullable Function<ByteArrayOutputStream, ObjectOutputStream> function) throws IllegalStateException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			ObjectOutputStream outputStream;
			if (function != null) {
				outputStream = function.apply(output);
			} else {
				outputStream = new ObjectOutputStream(output);
			}
			outputStream.writeObject(obj);
			outputStream.flush();
		} catch (IOException e) {
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
	public <T> T fromByteArray(@Nullable Function<ByteArrayInputStream, ObjectInputStream> function) throws IOException, ClassNotFoundException {
		if (obj == null || !byte[].class.isAssignableFrom(obj.getClass())) return null;
		byte[] ar = (byte[]) obj;
		ByteArrayInputStream input = new ByteArrayInputStream(ar);
		ObjectInputStream inputStream;
		if (function != null) {
			inputStream = function.apply(input);
		} else {
			inputStream = new ObjectInputStream(input);
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
	public Object deserialized(@Nullable Function<ByteArrayInputStream, ObjectInputStream> function, ClassLookup... lookups) throws IOException, ClassNotFoundException {
		byte[] serial = Base64.getDecoder().decode(obj.toString());
		ByteArrayInputStream input = new ByteArrayInputStream(serial);
		ObjectInputStream inputStream;
		if (function != null) {
			inputStream = function.apply(input);
		} else {
			inputStream = new ObjectInputStream(input);
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
	public <T> T fromByteArray(@Nullable Function<ByteArrayInputStream, ObjectInputStream> function, ClassLookup... lookups) throws IOException, ClassNotFoundException {
		if (obj == null || !byte[].class.isAssignableFrom(obj.getClass())) return null;
		byte[] ar = (byte[]) obj;
		ByteArrayInputStream input = new ByteArrayInputStream(ar);
		ObjectInputStream inputStream;
		if (function != null) {
			inputStream = function.apply(input);
		} else {
			inputStream = new ObjectInputStream(input);
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
	public Object deserialized(@Nullable Function<ByteArrayInputStream, ObjectInputStream> function) throws IOException, ClassNotFoundException {
		byte[] serial = Base64.getDecoder().decode(obj.toString());
		ByteArrayInputStream input = new ByteArrayInputStream(serial);
		ObjectInputStream inputStream;
		if (function != null) {
			inputStream = function.apply(input);
		} else {
			inputStream = new ObjectInputStream(input);
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
	public <T> T fromByteArray(@Nullable Function<ByteArrayInputStream, ObjectInputStream> function, @NotNull ClassLoader classLoader) throws IOException, ClassNotFoundException {
		if (obj == null || !byte[].class.isAssignableFrom(obj.getClass())) return null;
		byte[] ar = (byte[]) obj;
		ByteArrayInputStream input = new ByteArrayInputStream(ar);
		ObjectInputStream inputStream;
		if (function != null) {
			inputStream = function.apply(input);
		} else {
			inputStream = new ObjectInputStream(input);
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
	public Object deserialized(@Nullable Function<ByteArrayInputStream, ObjectInputStream> function, @NotNull ClassLoader classLoader) throws IOException, ClassNotFoundException {
		byte[] serial = Base64.getDecoder().decode(obj.toString());
		ByteArrayInputStream input = new ByteArrayInputStream(serial);
		ObjectInputStream inputStream;
		if (function != null) {
			inputStream = function.apply(input);
		} else {
			inputStream = new ObjectInputStream(input);
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
	public <R> @Nullable R deserialize(@Nullable Function<ByteArrayInputStream, ObjectInputStream> function, @NotNull Class<R> type) {
		try {
			Object o = deserialized(function);
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
	public <R> @Nullable R deserialize(@Nullable Function<ByteArrayInputStream, ObjectInputStream> function, @NotNull Class<R> type, @NotNull ClassLoader classLoader) {
		try {
			Object o = deserialized(function, classLoader);
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
