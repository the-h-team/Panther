package com.github.sanctum.panther.event;

import com.github.sanctum.panther.recursive.Service;
import com.github.sanctum.panther.recursive.ServiceFactory;
import com.github.sanctum.panther.recursive.ServiceLoader;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherCollectors;
import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherList;
import com.github.sanctum.panther.container.PantherMap;
import com.github.sanctum.panther.container.PantherSet;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public abstract class VentMap implements Service {

	/**
	 * Registers a listener for the given vent host
	 *
	 * @param host     the plugin to set for the listeners
	 * @param listener the listener to be registered
	 */
	public abstract void subscribe(@NotNull Vent.Host host, @NotNull Object listener);

	/**
	 * Adds a subscription to this mapping.
	 *
	 * @param subscription the subscription to be added
	 */
	public abstract void subscribe(@NotNull Vent.Subscription<?> subscription);

	/**
	 * Adds a subscription extender to this mapping.
	 *
	 * @param extender the extender to register.
	 */
	public abstract void subscribe(@NotNull Vent.Subscription.Extender<?> extender);

	/**
	 * Adds multiple subscriptions to this mapping.
	 *
	 * @param subscription  the subscription to be added.
	 * @param subscriptions the other subscriptions.
	 */
	public abstract void subscribeAll(@NotNull Vent.Subscription<?> subscription, @NotNull Vent.Subscription<?>... subscriptions);

	/**
	 * Registers multiple listeners at once for the given plugin
	 *
	 * @param host      the plugin to set for the listeners
	 * @param listeners the listeners to be registered
	 */
	public abstract void subscribeAll(@NotNull Vent.Host host, @NotNull Object... listeners);

	/**
	 * Remove a registered subscription listener from cache.
	 *
	 * @param listener The listener to remove.
	 */
	public abstract void unsubscribe(@NotNull Object listener);

	/**
	 * The most preferred way to remove subscriptions.
	 *
	 * @param subscription the subscription instance to remove;
	 */
	public abstract void unsubscribe(@NotNull Vent.Subscription<?> subscription);

	/**
	 * @param extender
	 */
	public abstract void unsubscribe(@NotNull Vent.Subscription.Extender<?> extender);

	/**
	 * Removes a registered subscription listener by specification.
	 *
	 * @param host the plugin providing the listener
	 * @param key  the key associated with the listener or null if none was specified
	 */
	public abstract void unsubscribe(@NotNull Vent.Host host, @NotNull String key);

	/**
	 * Removes a registered subscription listener by specification.
	 *
	 * @param host the plugin providing the listener
	 * @param key  the key associated with the listener or null if none was specified
	 */
	public abstract void unsubscribe(@NotNull Vent.Host host, @Nullable String key, Object listener);

	/**
	 * Unsubscribe from an event by providing the key of the desired subscription if found.
	 *
	 * @param eventType The {@link Vent} to unsubscribe from.
	 * @param key       The key namespace for the subscription.
	 * @param <T>       The inheritance of vent.
	 * @apiNote As there could be multiple entries for one key, you cannot be sure to remove the right listener!
	 * And in the only case where it would always remove the right key,
	 * the method is equivalent to {{@link #unsubscribeAll(Class, String)}}
	 */
	public abstract <T extends Vent> void unsubscribe(@NotNull Class<T> eventType, @NotNull String key);

	/**
	 * Unsubscribe every found subscription labeled under the specified namespace.
	 *
	 * @param key The key namespace for the subscription.
	 */
	public abstract void unsubscribeAll(@NotNull String key);

	/**
	 * Unsubscribe every found subscription labeled under the specified namespace.
	 *
	 * @param predicate The prerequisite to unsubscribing from a vent.
	 */
	public abstract void unsubscribeAll(@NotNull Predicate<Vent.Subscription<?>> predicate);

	/**
	 * Remove all registered subscription listeners for a plugin from cache.
	 *
	 * @param host The host to query.
	 */
	public abstract void unsubscribeAll(@NotNull Vent.Host host);

	/**
	 * Unsubscribe from an event by providing the key of the desired subscription. All instances of
	 * the found subscription are scheduled for removal.
	 *
	 * @param eventType The {@link Vent} to unsubscribe from.
	 * @param key       The key namespace for the subscription.
	 * @param <T>       The inheritance of vent.
	 */
	public abstract <T extends Vent> void unsubscribeAll(@NotNull Class<T> eventType, @NotNull String key);

	/**
	 * @return
	 */
	public abstract PantherCollection<Vent.Link> getLinks();

	/**
	 * Narrow down a list of all subscription links provided by a single host.
	 *
	 * @param host The host providing the links.
	 * @return A list of all linked subscription pointers.
	 */
	public abstract PantherCollection<Vent.Link> getLinks(@NotNull Vent.Host host);

	/**
	 * @return
	 */
	public abstract PantherCollection<Vent.Subscription<?>> getSubscriptions();

	/**
	 * Narrow down a list of all subscriptions provided by a single plugin.
	 *
	 * @param plugin The plugin providing the subscriptions.
	 * @return A list of all linked subscriptions.
	 */
	public abstract PantherCollection<Vent.Subscription<?>> getSubscriptions(@NotNull Vent.Host plugin);

	/**
	 * @param tClass
	 * @param priority
	 * @param <T>
	 * @return
	 */
	public abstract <T extends Vent> Stream<Vent.Subscription<T>> getSubscriptions(@NotNull Class<T> tClass, @NotNull Vent.Priority priority);

	/**
	 * Get the first found vent listener annotated with the specified key.
	 *
	 * @param key The key for the listener.
	 * @return The registered listener.
	 */
	public abstract Vent.Link getLink(@NotNull String key);

	/**
	 * Get a singular subscription by its relative key if found.
	 *
	 * @param eventType The {@link Vent} to retrieve the subscription for.
	 * @param key       The namespace for the subscription.
	 * @param <T>       The inheritance of vent.
	 * @return The desired subscription if found otherwise null.
	 */
	public abstract <T extends Vent> Vent.Subscription<T> getSubscription(@NotNull Class<T> eventType, @NotNull String key);

	/**
	 * @param key
	 * @return
	 */
	public abstract Stream<Vent.Subscription.Extender<?>> getExtenders(@NotNull String key);

	/**
	 * @return
	 */
	public static @NotNull VentMap getInstance() {
		VentMap instance = ServiceFactory.getInstance().getService(VentMap.class);
		if (instance == null) {
			ServiceLoader loader = ServiceFactory.getInstance().newLoader(VentMap.class).supply(new Default());
			return loader.load();
		}
		return instance;
	}

	/**
	 * Internal implementation for vent map instance.
	 */
	public final static class Default extends VentMap {

		final PantherMap<Vent.Host, PantherMap<String, PantherSet<Vent.Link>>> listeners = new PantherEntryMap<>();
		final PantherMap<Vent.Host, PantherMap<Class<? extends Vent>, PantherMap<Vent.Priority, PantherSet<Vent.Subscription<?>>>>> subscriptions = new PantherEntryMap<>();
		final PantherMap<String, PantherSet<Vent.Subscription.Extender<?>>> extenders = new PantherEntryMap<>();
		final Obligation obligation = () -> "To provide a local cache for custom event handling.";

		@Override
		public @NotNull Obligation getObligation() {
			return obligation;
		}

		@Override
		public void subscribe(Vent.@NotNull Host host, @NotNull Object listener) {
			Vent.Link link = new Vent.Link(host, listener) {
			};
			if (listener instanceof Vent.Link) {
				link = (Vent.Link) listener;
			}
			listeners.computeIfAbsent(host, h -> new PantherEntryMap<>()).computeIfAbsent(link.getKey(), s -> new PantherSet<>())
					.add(link);
		}

		@Override
		public void subscribe(Vent.@NotNull Subscription<?> subscription) {
			subscriptions.computeIfAbsent(subscription.getHost(), p -> new PantherEntryMap<>())
					.computeIfAbsent(subscription.getEventType(), t -> new PantherEntryMap<>())
					.computeIfAbsent(subscription.getPriority(), p -> new PantherSet<>())
					.add(subscription);
		}

		@Override
		public void subscribe(Vent.Subscription.@NotNull Extender<?> extender) {
			extenders.computeIfAbsent(extender.getKey(), s -> new PantherSet<>()).add(extender);
		}

		@Override
		public void subscribeAll(Vent.@NotNull Subscription<?> subscription, Vent.Subscription<?>... subscriptions) {
			subscribe(subscription);
			for (Vent.Subscription<?> sub : subscriptions) {
				subscribe(sub);
			}
		}

		@Override
		public void subscribeAll(Vent.@NotNull Host host, @NotNull Object... listeners) {
			for (Object o : listeners) {
				subscribe(host, o);
			}
		}

		@Override
		public void unsubscribe(@NotNull Object listener) {
			if (listener instanceof Vent.Link) {
				listeners.get(((Vent.Link) listener).getHost()).get(((Vent.Link) listener).getKey()).remove((Vent.Link) listener);
			}
			Optional<Vent.Link> optional = getLinks().stream().filter(l -> l.getParent().equals(listener))
					.findFirst();
			if (optional.isPresent()) {
				Vent.Link link = optional.get();
				listeners.get(link.getHost()).get(link.getKey()).remove(link);
			}
		}

		@Override
		public void unsubscribe(Vent.@NotNull Subscription<?> subscription) {
			Optional.ofNullable(subscriptions.get(subscription.getHost()))
					.map(m -> m.get(subscription.getEventType()))
					.map(m -> m.get(subscription.getPriority()))
					.ifPresent(s -> s.remove(subscription));
		}

		@Override
		public void unsubscribe(Vent.Subscription.@NotNull Extender<?> extender) {
			Optional.ofNullable(extenders.get(extender.getKey())).ifPresent(s -> s.remove(extender));
		}

		@Override
		public void unsubscribe(Vent.@NotNull Host host, @NotNull String key) {
			Optional.ofNullable(listeners.get(host)).map(m -> m.get(key))
					.ifPresent(s -> s.removeIf(l -> key.equals(l.getKey())));
		}

		@Override
		public void unsubscribe(Vent.@NotNull Host host, @Nullable String key, Object listener) {
			Optional.ofNullable(listeners.get(host)).map(m -> m.get(key))
					.ifPresent(s -> s.removeIf(l -> listener.equals(l.getParent())));
		}

		@Override
		public <T extends Vent> void unsubscribe(@NotNull Class<T> eventType, @NotNull String key) {
			Optional<Vent.Subscription<?>> subscription = subscriptions.values().stream().flatMap(
					m -> Optional.ofNullable(m.get(eventType))
							.map(PantherMap::values)
							.map(PantherCollection::stream)
							.map(s -> s.flatMap(PantherCollection::stream)).orElse(Stream.empty())
			).filter(s -> s.getKey().map(key::equals).orElse(false)).findFirst();
			subscription.ifPresent(sub -> subscriptions.get(sub.getHost()).get(eventType).get(sub.getPriority()).remove(sub)
			);
		}

		@Override
		public void unsubscribeAll(@NotNull String key) {

		}

		@Override
		public void unsubscribeAll(@NotNull Predicate<Vent.Subscription<?>> fun) {

		}

		@Override
		public void unsubscribeAll(Vent.@NotNull Host host) {

		}

		@Override
		public <T extends Vent> void unsubscribeAll(@NotNull Class<T> eventType, @NotNull String key) {
			subscriptions.values().forEach(m ->
					Optional.ofNullable(m.get(eventType))
							.map(PantherMap::values)
							.map(PantherCollection::stream)
							.ifPresent(s -> s.forEachOrdered(set -> set.removeIf(sub -> sub.getKey().map(key::equals).orElse(false)))));
		}

		@Override
		public PantherCollection<Vent.Link> getLinks() {
			return listeners.values().stream().map(PantherMap::values).flatMap(PantherCollection::stream).flatMap(PantherSet::stream)
					.collect(PantherCollectors.toList());
		}

		@Override
		public PantherCollection<Vent.Link> getLinks(Vent.@NotNull Host host) {
			return listeners.get(host).values().stream().flatMap(PantherCollection::stream).collect(PantherCollectors.toList());
		}

		@Override
		public PantherCollection<Vent.Subscription<?>> getSubscriptions() {
			return subscriptions.values().stream().flatMap(m -> m.values().stream()).flatMap(m -> m.values().stream())
					.flatMap(PantherSet::stream).collect(PantherCollectors.toList());
		}

		@Override
		public PantherCollection<Vent.Subscription<?>> getSubscriptions(Vent.@NotNull Host host) {
			return Optional.ofNullable(subscriptions.get(host)).map(PantherMap::values)
					.map(PantherCollection::stream)
					.map(s -> s.flatMap(m -> m.values().stream()))
					.map(s -> s.flatMap(PantherCollection::stream))
					.map(s -> s.collect(PantherCollectors.toList()))
					.orElse(new PantherList<>());
		}

		@Override
		public <T extends Vent> Stream<Vent.Subscription<T>> getSubscriptions(@NotNull Class<T> tClass, Vent.@NotNull Priority priority) {
			return subscriptions.values().stream().map(m -> Optional.ofNullable(m.get(tClass)).map(v -> v.get(priority))
							.orElse(new PantherSet<>()))
					.flatMap(PantherCollection::stream).map(s -> (Vent.Subscription<T>) s);
		}

		@Override
		public Vent.Link getLink(@NotNull String key) {
			return getLinks().stream().filter(l -> l.getKey().equals(key)).findAny().orElse(null);
		}

		@Override
		public <T extends Vent> Vent.Subscription<T> getSubscription(@NotNull Class<T> eventType, @NotNull String key) {
			return (Vent.Subscription<T>) subscriptions.values().stream()
					.flatMap(m -> Optional.ofNullable(m.get(eventType)).map(PantherMap::values)
							.map(PantherCollection::stream).orElse(Stream.empty()))
					.flatMap(PantherCollection::stream)
					.filter(s -> s.getKey().map(key::equals).orElse(false)).findAny().orElse(null);
		}

		@Override
		public Stream<Vent.Subscription.Extender<?>> getExtenders(@NotNull String key) {
			return extenders.computeIfAbsent(key, s -> new PantherSet<>()).stream();
		}
	}

}
