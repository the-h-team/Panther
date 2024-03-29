package com.github.sanctum.panther.file;

import java.util.Map;
import java.util.Set;

/**
 * An object holding multiple possible nodes or objects full of their own respective data whether it be more nodes or objects.
 *
 * @author Hempfest
 * @version 1.0
 */
public interface MemorySpace {

	/**
	 * @return The full path for this memory space.
	 */
	String getPath();

	/**
	 * Checks if a node exists under the current namespace.
	 *
	 * @param key the name space.
	 * @return false if the specified name space is nonexistent or equal to an object not a node otherwise true
	 */
	boolean isNode(String key);

	/**
	 * Get a child node of this current node under the current namespace.
	 *
	 * <p>Nodes of nodes will automatically append each others key paths</p>
	 *
	 * @param key the name space.
	 * @return The existing node or a new one depending on implementation.
	 * Default {@link Configurable} usage provides fresh nodes if non-existent.
	 */
	Node getNode(String key);

	/**
	 * Get the name spaces behind every object within this memory space.
	 *
	 * @param deep Whether to look through multiple layers or not.
	 * @return All data keys within this memory space.
	 */
	Set<String> getKeys(boolean deep);

	/**
	 * Get the values within this memory space.
	 *
	 * @param deep Whether to look through multiple layers or not.
	 * @return All data within this memory space.
	 */
	Map<String, Object> getValues(boolean deep);

}
