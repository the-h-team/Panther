package com.github.sanctum.panther.file;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jetbrains.annotations.NotNull;

/**
 * An object that encapsulates data storage services.
 * Save, delete, read or modify existing data in multi-dimensional spaces.
 *
 * <p>The end of this specific node could imply that its an object or another node so parsing or knowledge of locations is needed.</p>
 *
 * @author Hempfest
 * @version 1.0
 */
public interface Node extends Root, MemorySpace {

	/**
	 * Get the object attached to this node if present.
	 *
	 * @return The object under this node or null.
	 */
	Object get();

	/**
	 * If the following node represents that of an object not another node
	 * use this method to parse information in the case of primitive data.
	 *
	 * @return The primitive object parser for this node.
	 */
	Primitive toPrimitive();

	/**
	 * If the following node represents that of an object not another node
	 * use this method to parse information in the case of generic data.
	 *
	 * @param clazz The generic processing class to use.
	 * @param <T> The generic processor type
	 * @return The generic object parser for this nodes configurable.
	 */
	<T extends Generic> T toGeneric(@NotNull Class<T> clazz);

	/**
	 * If the result of this node ends in an object instead of another node parse its type here.
	 *
	 * <p>The use of this method implies the targeted value is not a primitive but of a custom object type.</p>
	 *
	 *
	 * @param type The object type.
	 * @param <T>  The type.
	 * @return The object at the end of this node parsed to a desirable type.
	 */
	<T> T get(Class<T> type);

	/**
	 * Set the object that this node represents.
	 *
	 * <p>If the existing node is a section and not an object modifying this results in a great chance of unwanted data removal</p>
	 *
	 * <p>All objects being saved must be either a known primitive or {@link Pointer} type</p>
	 *
	 * @param o The object to add.
	 */
	void set(Object o);

	@Override
	boolean create();

	/**
	 * Get the node this node stem's from if present, may return itself.
	 *
	 * @return The parent node to this current node.
	 */
	Node getParent();

	/**
	 * Convert all known data from this node to json text.
	 *
	 * @return The data from this node converted to JSON text.
	 */
	String toJson();

	/**
	 * This annotation is used to identify a json serializable object.
	 *
	 * @author Hempfest
	 * @version 2.0
	 * <p> Naming the value under this annotation anything other than the class it represents will result in
	 * failure to to read.</p>
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface Pointer {

		/**
		 * @return The alias for this object.
		 */
		String value() default "";

		/**
		 * Optional re-direction.
		 *
		 * @return The implementation for adapting.
		 */
		Class<? extends JsonAdapter<?>> type() default JsonAdapter.Dummy.class;

	}
}
