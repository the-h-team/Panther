package com.github.sanctum.panther.container;

import com.github.sanctum.panther.annotation.Ordinal;
import com.github.sanctum.panther.util.Task;
import com.github.sanctum.panther.util.TaskChain;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;

/**
 * A set of elements linked in the order received, <strong>not</strong> allowing duplicate entries.
 *
 * @see PantherCollection
 * @param <E> The type of set this is
 */
public final class PantherQueue<E> extends PantherCollectionBase<E> {

	final TaskChain chain = TaskChain.getAsynchronous();
	boolean beingTraversed;

	public PantherQueue() {
		super();
	}

	public PantherQueue(int capacity) {
		super(capacity);
	}

	public PantherQueue(Iterable<E> iterable) {
		super(iterable);
	}

	public PantherQueue(Iterable<E> iterable, int capacity) {
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
	 * @see PantherQueue#poll()
	 * @return the oldest element in queue by order, ignoring interruption.
	 */
	public E pollNow() {
		final E element = getFirst();
		if (element != null) {
			final String key = "Collection-Task;" + hashCode() + ":poll:" + element.hashCode();
			if (chain.get(key) == null) {
				Task task = new Task(key, Task.REPEATABLE, chain) {

					@Ordinal
					public void onExecute() {

						if (!PantherQueue.this.isBlocked()) {
							PantherQueue.super.remove(element);
						}

					}

				};
				chain.repeat(task, 0, 50);
			}
		}
		return element;
	}

	/**
	 * Grabs the oldest element in this queue and removes it.
	 * This method is blocking and may wait to make sure the value is removed before passing.
	 *
	 * @return the oldest element in this queue.
	 * @throws InterruptedException if processing removal could not be done.
	 */
	public E poll() throws InterruptedException {
		final E element = getFirst();
		boolean blocked = true;
		try {
			do {
				if (!isBlocked()) {
					remove(element);
					blocked = false;
				}
			} while (blocked);
		} catch (Exception e) {
			throw new InterruptedException("Unable to remove element from queue.");
		}
		return element;
	}

	@Override
	public boolean add(final E e) {
		final String key = "Collection-Task;" + hashCode() + ":add:" + e.hashCode();
		if (isBlocked()) {
			if (chain.get(key) == null) {
				Task task = new Task(key, Task.REPEATABLE, chain) {

					@Ordinal
					public void onExecute() {

						if (!PantherQueue.this.isBlocked()) {
							PantherQueue.super.add(e);
						}

					}

				};
				chain.repeat(task, 0, 50);
			}
			return false;
		} else return super.add(e);
	}

	@Override
	public boolean remove(final E e) {
		final String key = "Collection-Task;" + hashCode() + ":remove:" + e.hashCode();
		if (isBlocked()) {
			if (chain.get(key) == null) {
				Task task = new Task(key, Task.REPEATABLE, chain) {

					@Ordinal
					public void onExecute() {

						if (!PantherQueue.this.isBlocked()) {
							PantherQueue.super.remove(e);
						}

					}

				};
				chain.repeat(task, 0, 50);
			}
			return false;
		} else return super.remove(e);
	}

	public boolean isBlocked() {
		return beingTraversed;
	}

	@NotNull
	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {

			private Node initial;

			{
				initial = head;
			}

			@Override
			public boolean hasNext() {
				PantherQueue.this.beingTraversed = true;
				return initial != null;
			}

			@Override
			public E next() {
				final E data = initial.data;
				initial = initial.next;
				PantherQueue.this.beingTraversed = false;
				return data;
			}
		};
	}

}
