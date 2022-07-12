package com.github.sanctum.panther.container;

/**
 * Represents a {@link PantherCollection} that cannot be modified only read from.
 *
 * @param <K> The type of collection this is.
 */
public abstract class ImmutablePantherCollection<K> extends PantherCollectionBase<K> {

	ImmutablePantherCollection(Iterable<K> assortment) {
		for (K k : assortment) {
			super.add(k);
		}
	}

	@Override
	public boolean add(K k) {
		throw warning();
	}

	@Override
	public boolean addAll(Iterable<K> iterable) {
		throw warning();
	}

	@Override
	public boolean remove(K k) {
		throw warning();
	}

	@Override
	public boolean removeAll(Iterable<K> iterable) {
		throw warning();
	}

	@Override
	public void clear() {
		throw warning();
	}

	RuntimeException warning() {
		return new ImmutableStorageException("Element modifications cannot be made to immutable collections!");
	}

	public static <K> ImmutablePantherCollection<K> of(Iterable<K> assortment) {
		return new ImmutablePantherCollection<K>(assortment) {
		};
	}

	public static <K> Builder<K> builder() {
		return new Builder<>();
	}

	public static final class Builder<K> {

		private final PantherCollection<K> internal;

		Builder() {
			internal = new PantherList<>();
		}

		public Builder<K> add(K key) {
			internal.add(key);
			return this;
		}

		public ImmutablePantherCollection<K> build() {
			return of(internal);
		}

	}

}
