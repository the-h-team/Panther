package com.github.sanctum.panther.util;

import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherCollectors;
import com.github.sanctum.panther.container.PantherList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

/**
 * A page of elements retaining the order they were added in.
 *
 * @param <T> The type of elements this page contains.
 */
public interface Page<T> extends Iterable<T> {

	/**
	 * Check if this page is modifiable.
	 *
	 * @return true if this page can have elements be removed/added
	 */
	boolean isModifiable();

	/**
	 * Get this page as its number.
	 *
	 * @return the number representation for this page.
	 */
	int getNumber();

	/**
	 * @return the size of this page (number of elements)
	 */
	int size();

	/**
	 * Get an element from this page.
	 *
	 * @throws IndexOutOfBoundsException if the specified index goes beyond the natural scope.
	 * @param index The index to retrieve.
	 * @return The object at the specified index
	 */
	T get(int index);

	/**
	 * Add an element to this page if it's modifiable.
	 *
	 * @param t The element to add.
	 * @return true if the element was added.
	 */
	boolean add(T t);

	/**
	 * Remove an element from this page if it's modifiable.
	 *
	 * @param t The element to remove
	 * @return true if the element was removed.
	 */
	boolean remove(T t);

	/**
	 * Check if this page contains a specific element.
	 *
	 * @param t The element to check existence for.
	 * @return true if this page contains the specified element.
	 */
	boolean contains(T t);

	/**
	 * Reorders this specific page by its parents' filtration sequences.
	 *
	 * <p>The use of this will allow you to delve further and apply ordering on specific pages but
	 * at the same time making it mess up the <strong>over-all</strong> ordering, if that's the case
	 * this can easily be fixed by re-instating the method {@link AbstractPaginatedCollection#reorder()} on the parent of this page.</p>
	 *
	 * @return The same page reordered.
	 */
	// This is an optional method, you should never need to use it FIXME why not?
	// and instead call the main AbstractPaginatedCollection#reorder() method.
	Deployable<Page<T>> reorder();

	default boolean isEmpty() {
		return size() == 0;
	}

	class Impl<T> implements Page<T> {

		private final AbstractPaginatedCollection<T> parent;
		private PantherCollection<T> collection;
		private final int page;

		public Impl(AbstractPaginatedCollection<T> parent, int number) {
			this.parent = parent;
			this.collection = new PantherList<>();
			this.page = number;
		}

		@Override
		public boolean isModifiable() {
			return true;
		}

		@Override
		public int getNumber() {
			return this.page;
		}

		@Override
		public int size() {
			return collection.size();
		}

		@Override
		public T get(int index) {
			return collection.get(index);
		}

		@Override
		public boolean add(T t) {
			if (!isModifiable()) return false;
			return collection.add(t);
		}

		@Override
		public boolean remove(T t) {
			if (!isModifiable()) return false;
			return collection.remove(t);
		}

		@Override
		public boolean contains(T t) {
			return collection.contains(t);
		}

		@Override
		public @NotNull Iterator<T> iterator() {
			return collection.iterator();
		}

		@Override
		public Deployable<Page<T>> reorder() {
			return Deployable.of(() -> {
				PantherCollection<T> copy = collection;
				if (parent.predicate != null) {
					copy = collection.stream().filter(parent.predicate).collect(PantherCollectors.toList());
				}
				if (parent.comparator != null) {
					copy = copy.stream().sorted(parent.comparator).collect(PantherCollectors.toSet());
				}
				collection.clear();
				collection = copy;
				return this;
			}, 0);
		}

	}

}
