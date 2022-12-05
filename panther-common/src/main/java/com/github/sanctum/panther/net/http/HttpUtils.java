package com.github.sanctum.panther.net.http;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Entry point to the HTTP submodule
 * This class provides access to builders for common HTTP request objects
 */
public class HttpUtils {

	/**
	 * Creates a new Builder for a Get Request Object which will parse the result to the desired form.
	 * The settings you choose in the process will determine the capabilities of
	 * the resulting {@link HttpGetter <Object>} subtype, which will be capable of holding intermediate results,
	 * updating state and re-downloading the data either way. <br>
	 * Usage example:
	 * <code>
	 * <br>HttpUtils.newGetBuilder("https://jsonplaceholder.typicode.com", JsonParser::parseString)
	 * <br>.setSubPath("/posts")
	 * <br>.setProcessor(j -> j.getAsJsonArray().get(0).getAsJsonObject())
	 * <br>.buildToResult();
	 * </code>
	 *
	 * @param url         the webserver address to get the data from, without any subpath on that page.
	 * @param dataBuilder the data building function which will be used to pre-process the retrieved string data to a more convenient data type.
	 * @param <T>         the type the pre-processed data will have
	 * @return a new builder object with the given settings.
	 */
	public static <T> GeneralBuilder<T> newGetBuilder(String url, Function<String, T> dataBuilder) {
		return new GeneralBuilder<>(url, dataBuilder);
	}

	/**
	 * Base class for all HttpGetter Builders.
	 * Used to store common data
	 *
	 * @param <T> the intermediate result type of Getters produced by this builder
	 * @param <B> the current builder subtype
	 */
	@SuppressWarnings("unchecked")
	private static abstract class BuilderBase<T, B extends BuilderBase<T, B>> {
		private final String url;
		private final Function<String, T> dataBuilder;
		protected String subPath;
		protected Map<String, String> arguments = new HashMap<>();

		private BuilderBase(final String url, final Function<String, T> dataBuilder) {
			this.url = url;
			if (dataBuilder == null) {
				throw new IllegalArgumentException("You must provide a data building Function!");
			}
			this.dataBuilder = dataBuilder;
		}

		/**
		 * The document path to use
		 *
		 * @param subPath the path
		 * @return this builder instance
		 */
		public B setSubPath(final String subPath) {
			this.subPath = subPath;
			return (B) this;
		}

		/**
		 * Adds http request arguments to the HttpGetter.
		 *
		 * @param arguments the arguments to be added.
		 * @return this builder instance
		 */
		public B addArguments(final Map<String, String> arguments) {
			this.arguments.putAll(arguments);
			return (B) this;
		}

		/**
		 * Adds a http request argument to the HttpGetter.
		 *
		 * @param key   the parameter name
		 * @param value the value of the parameter
		 * @return this builder instance
		 */
		public B addArgument(String key, String value) {
			arguments.put(key, value);
			return (B) this;
		}

		protected Function<String, T> getDataBuilder() {
			return dataBuilder;
		}

		protected String getUrl() {
			return url;
		}
	}

	/**
	 * The builder construct for a simple {@link HttpGetter <T>}
	 *
	 * @param <T> the type of the intermediate result of the getter
	 */
	public static final class GeneralBuilder<T> extends BuilderBase<T, GeneralBuilder<T>> {

		private GeneralBuilder(final String url, Function<String, T> dataBuilder) {
			super(url, dataBuilder);
		}

		/**
		 * Attaches a processing function to the HttpGetter, which will turn the result type to a {@link HttpReducer}.
		 * The function is intended to split up the work of a possible networking thread and worker thread.
		 * This also can be useful to extract a validation flag of the intermediate result before continuing processing it.
		 *
		 * @param processor the result postprocessing function
		 * @param <R>       the result type the gotten data can be reduced to
		 * @return a {@link ReducerBuilder} containing the state of this builder
		 */
		public <R> ReducerBuilder<T, R> setProcessor(final Function<T, R> processor) {
			return new ReducerBuilder<>(this, processor);
		}

		/**
		 * Attaches a collection of processing functions to the HttpGetter,
		 * which will turn the result type to a {@link HttpConsumer}.
		 * Those functions will be performed on the intermediate result.
		 * Useful to reference multiple extraction and updating functions that rely on the state that has been retrieved.
		 *
		 * @param consumers the consumers to be attached
		 * @return a {@link ReducerBuilder} instance, containing the state of this builder instance
		 */
		public ConsumerBuilder<T> addConsumers(Collection<Consumer<T>> consumers) {
			return new ConsumerBuilder<>(this, consumers);
		}

		/**
		 * Attaches a  processing function to the HttpGetter,
		 * which will turn the result type to a {@link HttpConsumer}.
		 * This function will be performed on the intermediate result.
		 * Useful to reference multiple extraction and updating functions that rely on the state that has been retrieved.
		 *
		 * @param consumer the consumer to be attached
		 * @return a {@link ReducerBuilder} instance, containing the state of this builder instance
		 */
		public ConsumerBuilder<T> addConsumer(Consumer<T> consumer) {
			return new ConsumerBuilder<>(this, consumer);
		}

		/**
		 * Creates a new {@link HttpGetter <T>} instance which is ready to download data.
		 *
		 * @return the created object
		 */
		public HttpGetter<T> build() {
			return new HttpGetterImpl<>(getUrl(), subPath, arguments, getDataBuilder());
		}

		/**
		 * Creates a new {@link HttpGetter <T>} instance and downloads the data.
		 *
		 * @return the created object.
		 */
		public HttpGetter<T> buildAndLoad() {
			HttpGetter<T> getter = build();
			getter.load();
			return getter;
		}

		/**
		 * Uses a freshly created {@link HttpGetter <T>} instance to download the data.
		 * Note that this instance will be discarded, you will only get the result.
		 *
		 * @return the intermediate result
		 */
		public T buildAndGet() {
			return build().loadAndGet();
		}

	}

	/**
	 * Specific builder for {@link HttpReducer} instances
	 *
	 * @param <T> the intermediate type to be used
	 * @param <R> the final type to be used
	 */
	public static final class ReducerBuilder<T, R> extends BuilderBase<T, ReducerBuilder<T, R>> {

		private final Function<T, R> processor;

		private ReducerBuilder(GeneralBuilder<T> stage1, Function<T, R> processor) {
			super(stage1.getUrl(), stage1.getDataBuilder());
			this.subPath = stage1.subPath;
			this.arguments = stage1.arguments;
			this.processor = processor;
		}

		/**
		 * Creates a new {@link HttpReducer} instance which is ready to download data.
		 *
		 * @return the new instance
		 */
		public HttpReducer<T, R> build() {
			return new HttpReducerImpl<>(getUrl(), subPath, arguments, getDataBuilder(), processor);
		}

		/**
		 * Creates a new {@link HttpReducer} instance and downloads the data.
		 *
		 * @return the new instance
		 */
		public HttpReducer<T, R> buildAndLoad() {
			HttpReducer<T, R> result = build();
			result.load();
			return result;
		}

		/**
		 * Creates a new {@link HttpReducer} instance,
		 * downloads and processes the data to a final result which is ready to be retrieved.
		 *
		 * @return the new instance
		 */
		public HttpReducer<T, R> buildAndProcess() {
			HttpReducer<T, R> result = build();
			result.load();
			result.process();
			return result;
		}

		/**
		 * Uses a freshly created {@link HttpReducer <T>} instance to download and process the data.
		 * Note that the instance will be discarded, you will only get the result.
		 *
		 * @return the result
		 */
		public R buildToResult() {
			HttpReducer<T, R> httpReducer = build();
			httpReducer.load();
			httpReducer.process();
			return httpReducer.getResult();
		}

	}

	/**
	 * Specific builder for {@link HttpConsumer} instances
	 *
	 * @param <T> the intermediate type to be used
	 */
	public static final class ConsumerBuilder<T> extends BuilderBase<T, ConsumerBuilder<T>> {

		private final List<Consumer<T>> consumers = new LinkedList<>();
		private boolean restrictMultipleUsage;


		private ConsumerBuilder(GeneralBuilder<T> stage1, Consumer<T> consumer) {
			super(stage1.getUrl(), stage1.getDataBuilder());
			this.subPath = stage1.subPath;
			this.arguments = stage1.arguments;
			consumers.add(consumer);
		}

		private ConsumerBuilder(GeneralBuilder<T> stage1, Collection<Consumer<T>> consumers) {
			super(stage1.getUrl(), stage1.getDataBuilder());
			this.subPath = stage1.subPath;
			this.arguments = stage1.arguments;
			this.consumers.addAll(consumers);
		}

		/**
		 * Attaches a collection of processing functions to the HttpConsumer.
		 * Those functions will be performed on the intermediate result.
		 * Useful to reference multiple extraction and updating functions that rely on the state that has been retrieved.
		 *
		 * @param consumers the consumers to be attached
		 * @return this builder instance
		 */
		public ConsumerBuilder<T> addConsumers(Collection<Consumer<T>> consumers) {
			this.consumers.addAll(consumers);
			return this;
		}

		/**
		 * Attaches a  processing function to the {@link HttpConsumer}.
		 * This function will be performed on the intermediate result.
		 * Useful to reference multiple extraction and updating functions that rely on the state that has been retrieved.
		 *
		 * @param consumer the consumer to be attached
		 * @return this builder instance
		 */
		public ConsumerBuilder<T> addConsumer(Consumer<T> consumer) {
			consumers.add(consumer);
			return this;
		}

		/**
		 * Helper to enforce that the data is only consumed once per update.
		 * If the restriction is desired, a {@link IllegalStateException} will be thrown when trying to process the same set of data twice.
		 *
		 * @param restrictMultipleUsage true for restriction
		 * @return this builder instance
		 */
		public ConsumerBuilder<T> restrictMultipleUsage(boolean restrictMultipleUsage) {
			this.restrictMultipleUsage = restrictMultipleUsage;
			return this;
		}

		/**
		 * Creates a new {@link HttpConsumer} instance which is ready to download data.
		 *
		 * @return the new instance
		 */
		public HttpConsumer<T> build() {
			return new HttpConsumerImpl<>(getUrl(), subPath, arguments, getDataBuilder(), consumers, restrictMultipleUsage);
		}

		/**
		 * Creates a new {@link HttpConsumer} instance and downloads the data.
		 *
		 * @return the new instance
		 */
		public HttpConsumer<T> buildAndLoad() {
			HttpConsumer<T> consumer = build();
			consumer.load();
			return consumer;
		}

		/**
		 * Creates a new {@link HttpReducer} instance, downloads and consumes the data.
		 *
		 * @return the new instance
		 */
		public HttpConsumer<T> buildAndConsume() {
			HttpConsumer<T> consumer = build();
			consumer.loadAndConsume();
			return consumer;
		}

	}

}
