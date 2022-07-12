package com.github.sanctum.panther.container;

/**
 * A set of elements linked in the order received, <strong>not</strong> allowing duplicate entries.
 *
 * @see PantherCollection
 * @param <E> The type of set this is
 */
public final class PantherSet<E> extends PantherCollectionBase<E> {

	public PantherSet() {
		super();
	}

	public PantherSet(int capacity) {
		super(capacity);
	}

	public PantherSet(Iterable<E> iterable) {
		super(iterable);
	}

	public PantherSet(Iterable<E> iterable, int capacity) {
		super(iterable, capacity);
	}

	/**
	 * @return the first element in this set or null.
	 */
	public E getFirst() {
		if (head == null) return null;
		return head.data;
	}

	/**
	 * @return the last element in this set, could be the same as the first element or null.
	 */
	public E getLast() {
		if (tail == null) return null;
		return tail.data;
	}

	/**
	 * Attempt to add a new element at the tail end of this set.
	 *
	 * If the submitted element already exists it won't be added again.
	 *
	 * @param e The element to add.
	 * @return true if the element was added, false if it already exists
	 */
	@Override
	public boolean add(E e) {
		if (contains(e)) return false;
		return super.add(e);
	}
}
