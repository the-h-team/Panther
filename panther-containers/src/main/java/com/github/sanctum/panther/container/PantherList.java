package com.github.sanctum.panther.container;

/**
 * A list of elements in linked order, allowing for duplicate entries.
 *
 * @see PantherCollection
 * @param <E> The type of list this is
 */
public final class PantherList<E> extends PantherCollectionBase<E> {

	public PantherList() {
		super();
	}

	public PantherList(int capacity) {
		super(capacity);
	}

	public PantherList(Iterable<E> iterable) {
		super(iterable);
	}

	public PantherList(Iterable<E> iterable, int capacity) {
		super(iterable, capacity);
	}

	/**
	 * @return the first element in this list or null.
	 */
	public E getFirst() {
		if (head == null) return null;
		return head.data;
	}

	/**
	 * @return the last element in this list, could be the same as the first element or null.
	 */
	public E getLast() {
		if (size == 1) return getFirst();
		if (tail == null) return null;
		return tail.data;
	}


	@Override
	public boolean addAll(Iterable<E> iterable) {
		for (E e : iterable) {
			add(e);
		}
		return true;
	}

}
