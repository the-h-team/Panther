package com.github.sanctum.panther.container;

import java.lang.reflect.Array;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.NotNull;

/**
 * A custom collection type, this class will retain each element fed into it in the exact order it was received.
 * <p>
 * The practices of this collection type explicitly follows o(1) time complexity when inserting new tail elements,
 * when removing or retrieving an element o(n) time complexity takes place.
 *
 * @param <K> The type of element this collection is for.
 */
public interface PantherCollection<K> extends Iterable<K> {

	/**
	 * Get an element from this collection at a specific index.
	 *
	 * @param index the index of the value to retrieve.
	 * @return The value or null.
	 * @throws IndexOutOfBoundsException if the specified index goes beyond the natural scope.
	 */
	K get(int index) throws IndexOutOfBoundsException;

	/**
	 * Add a new element to the tail end of this collection.
	 *
	 * @param k The element to add.
	 * @return true if the element was added false if something went wrong.
	 */
	boolean add(K k);

	/**
	 * Add an iterable of relative source type to this collection.
	 *
	 * @param iterable the iterable to consume.
	 * @return true if all elements from the iterable were successfully added.
	 */
	boolean addAll(Iterable<K> iterable);

	/**
	 * Remove an element contained within this collection.
	 *
	 * @param k The element to be removed.
	 * @return true if the element was successfully removed.
	 */
	boolean remove(K k);

	/**
	 * Remove elements from this collection matching contents from the relative iterable.
	 *
	 * @param iterable the iterable to remove elements from
	 * @return true if all elements from the iterable were removed from this collection.
	 */
	boolean removeAll(Iterable<K> iterable);

	/**
	 * Check if this collection contains a specific element.
	 *
	 * @param k The element to check existence for.
	 * @return true if this collection contains the specified element.
	 */
	boolean contains(K k);

	/**
	 * Check if this collection contains all elements provided from the iterable.
	 *
	 * @param iterable The iterable to query.
	 * @return true if this collection contains the iterated elements.
	 */
	boolean containsAll(Iterable<K> iterable);

	/**
	 * @return the size of this collection.
	 */
	int size();

	/**
	 * Clear all retained elements from this collection.
	 */
	void clear();

	/**
	 * Check if this collection is empty.
	 *
	 * @return true if this collection is empty.
	 */
	default boolean isEmpty() {
		return size() <= 0;
	}

	/**
	 * Convert this collection into an array of generic type.
	 *
	 * @param a a dummy array used only to specify type
	 * @param <T> The type of array for reference.
	 * @return an array containing all elements from this collection in order.
	 */
	default <T> @NotNull T[] toArray(T[] a) {
		T[] copy = (T[]) Array.newInstance(a.getClass().getComponentType(), size());
		for (int i = 0; i < size(); i++) {
			copy[i] = (T) get(i);
		}
		return copy;
	}

	/**
	 * Convert this collection into an array of generic type.
	 *
	 * @param a a dummy array used only to specify type for creation
	 * @return an array containing all elements from this collection in order.
	 */
	default @NotNull K[] toArray(IntFunction<K[]> a) {
		return stream().toArray(a);
	}

	/**
	 * @return A new element stream containing all the elements from this collection.
	 */
	default Stream<K> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	/**
	 * @param predicate The element matcher.
	 */
	default void removeIf(@NotNull Predicate<K> predicate) {
		forEach(k -> {
			if (predicate.test(k)) remove(k);
		});
	}

}
