package com.github.sanctum.panther.event;

import com.github.sanctum.panther.annotation.AnnotationDiscovery;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherList;
import com.github.sanctum.panther.util.PantherLogger;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Vent {

	private final Host host;
	private final boolean async;
	private final State state;
	private boolean cancelled;

	protected Vent(@NotNull Host host, boolean isAsync) {
		this.state = State.CANCELLABLE;
		this.host = host;
		this.async = isAsync;
	}

	protected Vent(@NotNull Host host, @NotNull State state, boolean isAsync) {
		this.state = state;
		this.host = host;
		this.async = isAsync;
	}

	public State getState() {
		return state;
	}

	public final Host getHost() {
		return this.host;
	}

	public final Runtime getRuntime() {
		return async ? Runtime.Asynchronous : Runtime.Synchronous;
	}

	public void setCancelled(boolean cancelled) {
		if (this.state == State.IMMUTABLE) throw new IllegalStateException("Cannot cancel immutable events.");
		this.cancelled = cancelled;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public final boolean isAsynchronous() {
		return this.async;
	}

	// All vent methods ^^^^
	// Below are classes and interfaces -----------------------

	public enum State {
		/**
		 * Event can be cancelled.
		 */
		CANCELLABLE,
		/**
		 * Cancellation attempts throw an exception.
		 */
		IMMUTABLE
	}

	public enum Runtime {
		Synchronous, Asynchronous;

		/**
		 * use this on a given runtime to validate that it is able to run the passed event
		 *
		 * @param vent the event that could be run
		 * @throws SubscriptionRuntimeException if the events runtime mismatches this runtime
		 */
		public void validate(Vent vent) throws SubscriptionRuntimeException {
			if (vent.getRuntime() != this) {
				throw new SubscriptionRuntimeException("Vent was tried to run " + this +
													   " but only can be run " + vent.getRuntime());
			}
		}

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	/**
	 * Event handler priority.
	 * LOWER means that it will be run earlier.
	 */
	public enum Priority {

		LOW(1),

		MEDIUM(2),

		HIGH(3),

		HIGHEST(4),

		READ_ONLY(5);

		/**
		 * A list containing all priorities that have write-access to events.
		 */
		private static final List<Priority> writeAccessing =
				Collections.unmodifiableList(Stream.of(LOW, MEDIUM, HIGH, HIGHEST).collect(Collectors.toList()));

		private final int level;

		Priority(int level) {
			this.level = level;
		}

		public static List<Priority> getWriteAccessing() {
			return writeAccessing;
		}

		public int getLevel() {
			return level;
		}
	}

	public static class Subscription<T extends Vent> {

		final VentMap mapInstance = VentMap.getInstance();
		private final Class<T> eventType;
		private final Subscribe.Consumer<T> action;
		private final Priority priority;
		private Host host;
		private String key;
		protected boolean p;

		public Subscription(Class<T> eventType, Host user, Priority priority, Subscribe.Consumer<T> action) {
			this.eventType = eventType;
			this.host = user;
			this.priority = priority;
			this.action = action;
		}

		public Subscription(Class<T> eventType, String key, Host user, Priority priority, Subscribe.Consumer<T> action) {
			this.eventType = eventType;
			this.key = key;
			this.host = user;
			this.priority = priority;
			this.action = action;
		}

		public Subscription(Class<T> eventType, Priority priority, Subscribe.Consumer<T> action) {
			this.eventType = eventType;
			this.priority = priority;
			this.action = action;
		}

		public Subscription(Class<T> eventType, String key, Priority priority, Subscribe.Consumer<T> action) {
			this.eventType = eventType;
			this.key = key;
			this.priority = priority;
			this.action = action;
		}

		public void remove() {
			mapInstance.unsubscribe(this);
		}

		public boolean isParent() {
			return this.p;
		}

		public Optional<String> getKey() {
			return Optional.ofNullable(this.key);
		}

		public Host getHost() {
			return host;
		}

		public Priority getPriority() {
			return priority;
		}

		public Subscribe.Consumer<T> getAction() {
			return action;
		}

		public Class<T> getEventType() {
			return eventType;
		}

		public static final class Builder<T extends Vent> {

			final VentMap mapInstance = VentMap.getInstance();
			private final Class<T> tClass;
			private Subscription<T> subscription;
			private Subscribe.Consumer<T> subscriberCall;
			private String key;
			private Host host;
			private Priority priority;

			private Builder(Class<T> tClass) {
				this.tClass = tClass;
			}

			public static <T extends Vent> Builder<T> of(Class<T> event) {
				return new Builder<>(event);
			}

			public Builder<T> next(String key) {
				this.key = key;
				return this;
			}

			public Builder<T> next(Host host) {
				this.host = host;
				return this;
			}

			public Builder<T> next(Priority priority) {
				this.priority = priority;
				return this;
			}

			public Builder<T> next(Subscribe.Consumer<T> call) {
				this.subscriberCall = call;
				return this;
			}

			/**
			 * Builds the subscription and registers at the VentMap service, if not done previously.
			 * Otherwise, it just returns the built subscription.
			 *
			 * @return the built subscription
			 */
			public Subscription<T> build() throws IllegalStateException {
				boolean register = subscription == null;
				if (register) {
					validate();
					if (this.key != null) {
						this.subscription = new Subscription<>(tClass, key, host, priority, subscriberCall);
					} else {
						this.subscription = new Subscription<>(tClass, host, priority, subscriberCall);
					}
					mapInstance.subscribe(this.subscription);
				}
				return this.subscription;
			}

			private void validate() throws IllegalStateException {
				if (Stream.of(host, priority).anyMatch(Objects::isNull)) {
					throw new IllegalStateException("There are still unassigned builds needed " +
													"to build a Subscription!");
				}
			}

		}


		public static class Extender<T> {

			private final Class<T> type;
			private final java.util.function.Consumer<T> consumer;
			private final String key;
			private final Link link;

			public Extender(final @NotNull Class<T> returnType, final @NotNull java.util.function.Consumer<T> consumer, final @NotNull String key, final @NotNull Link parent) {
				this.type = returnType;
				this.consumer = consumer;
				this.key = key;
				this.link = parent;
			}

			public static void runExtensions(@NotNull String key, @NotNull Object toProcess) {
				VentMap.getInstance().getExtenders(key)
						.filter(e -> e.getType().isAssignableFrom(toProcess.getClass()))
						.forEach(e -> runFinisher(e, toProcess));
			}

			private static <E> void runFinisher(Extender<E> ventExtender, Object toProcess) {
				ventExtender.consumer.accept(ventExtender.getType().cast(toProcess));
			}

			public final @NotNull String getKey() {
				return key;
			}

			public final @NotNull Link getLink() {
				return link;
			}

			public final @NotNull Class<T> getType() {
				return type;
			}
		}
	}

	public static abstract class Call<T extends Vent> {

		protected final T event;
		protected T readOnlyEventCopy;
		protected final Runtime type;

		public Call(@NotNull T event) {
			this.event = event;
			this.readOnlyEventCopy = event;
			this.type = event.getRuntime();
		}

		public final T run() {
			notifySubscribersAndListeners();
			return event;
		}

		@SuppressWarnings("unchecked")
		private void notifySubscribersAndListeners() {
			VentMap map = VentMap.getInstance();
			PantherCollection<Link> listeners = map.getLinks();
			List<Class<? extends Vent>> assignableClasses = generateAssignableClasses(event.getClass());
			Priority.getWriteAccessing().forEach(p -> assignableClasses.forEach(c -> {
				map.getSubscriptions(c, p).map(s -> (Subscription<? super T>) s)
						.forEachOrdered(subscription -> runSubscription(subscription, event));
				listeners.forEach(l -> notifyListeners(l, c, p));
			}));
			assignableClasses.forEach(c -> {
				map.getSubscriptions(c, Priority.READ_ONLY)
						.map(s -> (Subscription<? super T>) s)
						.forEachOrdered(s -> runSubscriptionReadOnly(s, readOnlyEventCopy));
				listeners.forEach(listener -> runReadOnly(listener, c));
			});
		}

		private <E extends Vent> void notifyListeners(Link listener, Class<E> eventSuperClass,
		                                              Priority priority) {
			E vent = eventSuperClass.cast(event);
			listener.getHandlers(eventSuperClass, priority).forEachOrdered(e -> {
				if (vent.getState() == State.CANCELLABLE && !e.handlesCancelled()) {
					if (!vent.isCancelled()) {
						e.accept(vent, null);
						this.readOnlyEventCopy = event;
					}
				} else {
					e.accept(vent, null);
					this.readOnlyEventCopy = event;
				}
			});

		}

		private <E extends Vent> void runReadOnly(Link listener, Class<E> eventSuperClass) {
			listener.getHandlers(eventSuperClass, Priority.READ_ONLY).forEachOrdered(e -> {
				boolean cancelled = readOnlyEventCopy.isCancelled();
				if (!cancelled || e.handlesCancelled()) {
					e.accept(eventSuperClass.cast(readOnlyEventCopy), null);
					if (readOnlyEventCopy.isCancelled()) {
						readOnlyEventCopy.setCancelled(cancelled);
					}
				}
			});
		}

		private void runSubscription(Subscription<? super T> subscription, T event) {
			if (event.getState() != State.CANCELLABLE || !event.isCancelled()) {
				if (!event.isCancelled()) {
					runSub(subscription, event);
				}
			}
		}

		private void runSubscriptionReadOnly(Subscription<? super T> subscription, T event) {
			if (!event.isCancelled()) {
				runSub(subscription, event);
				if (event.isCancelled())
					event.setCancelled(false);
			}
		}

		private <S extends Vent> void runSub(Subscription<S> subscription, S event) {
			subscription.getAction().accept(event, subscription);
		}

		@SuppressWarnings("unchecked")
		private List<Class<? extends Vent>> generateAssignableClasses(Class<? extends Vent> ventClass) {
			List<Class<? extends Vent>> callingClasses = new ArrayList<>();
			Class<? extends Vent> temp = event.getClass();
			do {
				callingClasses.add(temp);
				temp = (Class<? extends Vent>) temp.getSuperclass();
			} while (Vent.class.isAssignableFrom(temp));
			return callingClasses;
		}

	}

	/**
	 * An annotation marking a {@link Link} subscription not valid for runtime usage.
	 *
	 * Example:
	 * <pre>{@code
	 *    @Disabled
	 *    @Subscribe
	 *    public void onMyEvent(Vent e) {
	 *
	 *    }}</pre>
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Disabled {
	
		String until() default "N/A";
	
	}

	/**
	 * Core class for internal listener implementation
	 * Wraps around any objects, detects methods annotated with {@link Subscribe} and creates SubscriberCalls with it.
	 * Also, it recognises methods annotated with {@link Extend} and adds them to the method linking pool
	 * Always has s string as key, which may be "null", when not specified by {@link Key};
	 *
	 * @author Rigo, Hempfest
	 */
	public static abstract class Link {

		protected final Map<Class<? extends Vent>, Map<Priority, Set<Consumer<?>>>> eventMap = new HashMap<>();
		protected final List<Subscription.Extender<?>> extenders = new LinkedList<>();
		protected final Object listener;
		protected final Host host;
		protected final String key;

		/**
		 * Creates an VentListener out of the passed listener object and with the plugin as callback for communication
		 *
		 * @param host     the plugin providing the listener object
		 * @param listener the listener object
		 */
		public Link(Host host, Object listener) {
			this.listener = listener;
			this.host = host;
			this.key = readKey();
			buildEventHandlers();
			buildExtensions();
		}

		/**
		 * Tries to detect a {@link Key} annotation on this class which contains the key.
		 *
		 * @return the string int the detected annotation, or "null" if none is present
		 */
		private String readKey() {
			String result = AnnotationDiscovery.of(Key.class, getParent()).mapFromClass((r, u) -> r.value());
			return Objects.toString(result);
		}

		/**
		 * Detects all annotated methods and converts them into SubscriberCall methods.
		 */
		private void buildEventHandlers() {
			AnnotationDiscovery<Subscribe, ?> discovery = AnnotationDiscovery.of(Subscribe.class, listener);
			AnnotationDiscovery<Disabled, ?> disabled = AnnotationDiscovery.of(Disabled.class, listener);
			discovery.filter(m -> m.getParameters().length == 1 && Vent.class.isAssignableFrom(m.getParameters()[0].getType())
					&& m.isAnnotationPresent(Subscribe.class) && Modifier.isPublic(m.getModifiers())).forEach(m -> {
						Optional<Subscribe> subscribe = discovery.read(m).stream().findAny();
				        Optional<Disabled> disable = disabled.read(m).stream().findAny();
						@SuppressWarnings("unchecked")
						Class<? extends Vent> mClass = (Class<? extends Vent>) m.getParameters()[0].getType();
						if (subscribe.isPresent()) {
							if (!disable.isPresent()) {
								registerSubscription(m, mClass, subscribe.get());
							}
						} else {
							PantherLogger.getInstance().getLogger().severe("Error registering " + m.getDeclaringClass() + "#" +
									m.getName());
						}
					}

			);
		}

		private void buildExtensions() {
			AnnotationDiscovery<Extend, ?> discovery = AnnotationDiscovery.of(Extend.class, listener);
			discovery.filter(m -> m.getParameters().length == 1 && m.isAnnotationPresent(Extend.class)
					&& Modifier.isPublic(m.getModifiers())).forEach(m -> {
				Optional<Extend> extend = discovery.read(m).stream().findAny();
				if (extend.isPresent()) {
					Class<?> parameterClass = m.getParameters()[0].getType();
					registerExtender(m, parameterClass, extend.get());
				} else {
					PantherLogger.getInstance().getLogger().severe("Error registering " + m.getDeclaringClass() + "#" + m.getName());
				}
			});
		}

		private <T> void registerExtender(final Method m, final Class<T> parameterClass, Extend extend) {
			Subscription.Extender<?> extender;
			String key = extend.identifier();
			if (m.getReturnType().equals(Void.TYPE) || extend.resultProcessors().length == 0) {
				extender = new Subscription.Extender<>(parameterClass, t -> invokeAsExtender(m, Object.class, t),
						key, this);
			} else {
				extender = new Subscription.Extender<>(parameterClass,
						buildExtender(t -> invokeAsExtender(m, m.getReturnType(), t), extend.resultProcessors()),
						key, this);
			}
			extenders.add(extender);
			VentMap.getInstance().subscribe(extender);
		}

		/**
		 * Helper method to build and event subscription out of the given method of the listener.
		 * The constructed subscription will include exception catching for any errors occurring while using the
		 * subscriber call and will display an informative message in that case, including the stacktrace.
		 *
		 * @param method    the method to use
		 * @param tClass    the class the method accepts as first and only parameter
		 * @param subscribe the annotation containing the conditions of the registration
		 * @param <T>       the type parameter of tClass
		 */
		private <T extends Vent> void registerSubscription(Method method, Class<T> tClass, Subscribe subscribe) {
			Consumer<T> call;
			boolean useCancelled = subscribe.processCancelled();
			if (method.getReturnType().equals(Void.TYPE) || subscribe.resultProcessors().length == 0) {
				//register as SubscriberCall lambda
				call = new Consumer<>(t -> invokeAsListener(method, tClass.getName(), Object.class, t), useCancelled);
			} else {
				//register as linking object
				Class<?> resultClass = method.getReturnType();
				call = new Consumer<>(buildExtender(t -> invokeAsListener(method, tClass.getName(), resultClass, t),
						subscribe.resultProcessors()), useCancelled);
			}
			eventMap.computeIfAbsent(tClass, c -> new HashMap<>())
					.computeIfAbsent(subscribe.priority(), p -> new HashSet<>())
					.add(call);
		}

		private <T> CallInfo<T> invokeAsListener(Method method, String eventName, Class<T> resultClass, Object... params) {
			String reflectionError = "Internal error hindered " + listener.getClass().getName() + "#"
					+ method.getName() + " from executing. Check method accessibility, parameters & usage!";
			String callError = "Could not pass event " + eventName + " to " + host;
			return invoke(method, reflectionError, callError, resultClass, params);
		}

		private <T> CallInfo<T> invokeAsExtender(Method method, Class<T> resultClass, Object... params) {
			String passed = "passed elements " + Arrays.toString(params);
			String reflectionError = "Internal error hindered " + listener.getClass().getName() + "#"
					+ method.getName() + " from further processing " + passed +
					". Check method accessibility, parameters & usage!";
			String callError = "Could not process" + passed + " at " + host;
			return invoke(method, reflectionError, callError, resultClass, params);
		}

		private <T> CallInfo<T> invoke(Method method, String refError, String callError, Class<T> retC, Object... params) {
			try {
				method.setAccessible(true);
				return new CallInfo<>(true, retC.cast(method.invoke(listener, params)));
			} catch (IllegalAccessException | InvocationTargetException e) {
				PantherLogger.getInstance().getLogger().severe(refError);
				if (e.getCause() != null) {
					e.getCause().printStackTrace();
				} else {
					e.printStackTrace();
				}
			} catch (Exception e) {
				PantherLogger.getInstance().getLogger().severe(callError);
				e.printStackTrace();
			}
			return new CallInfo<>(false, null);
		}

		/**
		 * Method used to retrieve all handling subscriber calls of one specific type and priority.
		 *
		 * @param <T>        type parameter of the eventClass
		 * @param eventClass the type the subscribers should accept
		 * @param priority   the priority the subscribers should have
		 * @return a Stream containing subscriber calls which meet the requirements
		 * @see Vent.Call#run()
		 */
		@SuppressWarnings("unchecked")
		public <T extends Vent> Stream<? extends Consumer<T>> getHandlers(Class<T> eventClass, Priority priority) {
			return Optional.ofNullable(eventMap.get(eventClass)).map(m -> m.get(priority)).map(Set::stream)
					.map(s -> s.map(c -> (Consumer<T>) c)).orElse(Stream.empty());
		}

		/**
		 * Method used to retrieve all disabled subscriber calls of one specific type and priority.
		 *
		 * @param <T>        type parameter of the eventClass
		 * @param eventClass the type the subscribers should accept
		 * @param priority   the priority the subscribers should have
		 * @return a Stream containing subscriber calls which meet the requirements
		 * @see Vent.Call#run()
		 */
		public <T extends Vent> Stream<? extends Consumer<T>> getDisabledHandlers(Class<T> eventClass, Priority priority) {
			AnnotationDiscovery<Disabled, Object> discovery = AnnotationDiscovery.of(Disabled.class, getParent());
			discovery.filter(m -> m.getParameterTypes().length > 0 && eventClass.isAssignableFrom(m.getParameterTypes()[0]), true);
			PantherCollection<Consumer<T>> collection = new PantherList<>();
			discovery.forEach(method -> {
				Subscribe s = method.getAnnotation(Subscribe.class);
				if (s.priority() == priority) {
					collection.add(new Consumer<>(t -> {
						try {
							method.invoke(getParent(), t);
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}
					}, false));
				}
			});
			return collection.stream();
		}

		/**
		 * @return the plugin providing the listener of this object
		 */
		public Host getHost() {
			return host;
		}

		/**
		 * @return the identifier for this link, or "null", if none was set
		 */
		public @Nullable String getKey() {
			return key;
		}

		/**
		 * Get the parent listener this link envelopes.
		 *
		 * @return the instance of this link.
		 */
		public Object getParent() {
			return listener;
		}

		/**
		 * Removes this listener link from the vent map service, so that no more calls will be executed on it.
		 */
		public void remove() {
			VentMap map = VentMap.getInstance();
			map.unsubscribe(this);
			extenders.forEach(map::unsubscribe);
		}


		@Override
		public boolean equals(final Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			final Link that = (Link) o;
			return listener.equals(that.listener) && host.equals(that.host) && key.equals(that.key) && eventMap.equals(that.eventMap);
		}

		@Override
		public int hashCode() {
			return Objects.hash(listener, host, key);
		}

		@Override
		public String toString() {
			return "Vent.Link{" +
					"listener=" + listener +
					", host=" + host +
					", key='" + key + '\'' +
					", eventMap=" + eventMap +
					'}';
		}

		public static class Consumer<T extends Vent> implements Subscribe.Consumer<T> {
			private final java.util.function.Consumer<T> eventHandler;
			private final boolean handleCancelled;

			public Consumer(java.util.function.Consumer<T> eventHandler, final boolean handleCancelled) {
				this.eventHandler = eventHandler;
				this.handleCancelled = handleCancelled;
			}

			@Override
			public void accept(final T event, final Subscription<T> unused) {
				eventHandler.accept(event);
			}

			public boolean handlesCancelled() {
				return handleCancelled;
			}
		}

		private static <T, S> java.util.function.Consumer<T> buildExtender(Function<T, CallInfo<S>> base, String[] targets) {
			return t -> {
				CallInfo<S> callInfo = base.apply(t);
				if (callInfo.success) {
					for (String target : targets) {
						Subscription.Extender.runExtensions(target, callInfo.result);
					}
				}
			};
		}

		static final class CallInfo<T> {
			private final boolean success;
			private final T result;

			CallInfo(final boolean success, final T result) {
				this.success = success;
				this.result = result;
			}
		}

		/**
		 * An annotation that's capable of keying a class. Giving it a unique identifier.
		 */
		@Documented
		@Retention(RetentionPolicy.RUNTIME)
		@Target(ElementType.TYPE)
		public @interface Key {

			@NotNull String value();

		}
	}

	/**
	 * A placeholder class, implement it and use it for usage with the vent system.
	 */
	public interface Host {

		/**
		 * Subscribe to a listener using this host.
		 *
		 * @param listener the listener to subscribe to.
		 */
		default void subscribe(@NotNull Object listener) {
			VentMap.getInstance().subscribe(this, listener);
		}

		/**
		 * Register an event subscription
		 *
		 * @param subscription
		 */
		default void subscribe(@NotNull Subscription<?> subscription) {
			subscription.host = this;
			VentMap.getInstance().subscribe(subscription);
		}

		/**
		 * @param listeners
		 */
		default void subscribeAll(@NotNull Object... listeners) {
			for (Object o : listeners) {
				subscribe(o);
			}
		}

		/**
		 * @param listener
		 */
		default void unsubscribe(@NotNull Object listener) {
			VentMap.getInstance().unsubscribe(listener);
		}

		/**
		 * @param subscription
		 */
		default void unsubscribe(@NotNull Subscription<?> subscription) {
			VentMap.getInstance().unsubscribe(subscription);
		}

		/**
		 * @param listeners
		 */
		default void unsubscribeAll(@NotNull Object... listeners) {
			for (Object o : listeners) {
				VentMap.getInstance().unsubscribe(o);
			}
		}

		/**
		 * @param subscription
		 * @param subscriptions
		 */
		default void unsubscribeAll(@NotNull Subscription<?> subscription, @NotNull Subscription<?>... subscriptions) {
			unsubscribe(subscription);
			for (Subscription<?> sub : subscriptions) {
				unsubscribe(sub);
			}
		}

		/**
		 * Get a vent listener link associated with this host.
		 *
		 * @param key The key of the vent link.
		 * @return a vent link or null if not found.
		 */
		default @Nullable Link getVentLink(@NotNull String key) {
			return getVentLinks().stream().filter(l -> key.equals(l.getKey())).findFirst().orElse(null);
		}

		/**
		 * Get a collection of all known vent link's for this host.
		 *
		 * @return all known vent link's associated with this host.
		 */
		default @NotNull PantherCollection<Link> getVentLinks() {
			return VentMap.getInstance().getLinks(this);
		}

		/**
		 * Get a vent subscription associated with a specific key.
		 *
		 * @param clazz The type of event the subscription is for.
		 * @param key The key of the subscription.
		 * @return a valid subscription if found.
		 */
		default <T extends Vent> @Nullable Subscription<T> getVentSubscription(@NotNull Class<T> clazz, @NotNull String key) {
			return VentMap.getInstance().getSubscription(clazz, key);
		}

		/**
		 * Get a collection of all known vent subscriptions for this host.
		 *
		 * @return all known vent subscriptions associated with this host.
		 */
		default @NotNull PantherCollection<Subscription<?>> getVentSubscriptions() {
			return VentMap.getInstance().getSubscriptions(this);
		}

	}
}
