package com.github.sanctum.panther.container;

import com.github.sanctum.panther.annotation.Note;
import com.github.sanctum.panther.annotation.Ordinal;
import com.github.sanctum.panther.util.Task;
import com.github.sanctum.panther.util.TaskChain;
import java.util.Iterator;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

/**
 * A set of elements linked in the order received, allowing for duplicate entries.
 *
 * This type of collection is special, it responds well to new entries ONLY if the use case doesn't require constant traversing if this queue
 * is being traversed it is considered blocked, meaning your modification will be scheduled for execution only once the queue is no longer blocked (when the queue isn't being actively traversed).
 * Do not use this collection type if it will be constantly traversed as an unwanted side effect may be modifications never get a chance to execute, if you would like
 * to still do this make sure you have a set period of time no shorter than 50 milliseconds between each execution on your own scheduling where the queue has a chance to catch up.
 *
 * @see PantherCollection
 * @param <E> The type of element this queue is for
 */
@Note("All queue modifications run on an in-daemon asynchronous task scheduler with an execution delay of 50 milliseconds.")
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

	public E get(Predicate<? super E> matcher) {
		return stream().filter(matcher).findFirst().orElse(null);
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
	 */
	public E poll() {
		final E element = getFirst();
		if (element != null) {
			boolean blocked = true;
			do {
				if (!isBlocked()) {
					remove(element);
					blocked = false;
				}
			} while (blocked);
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
