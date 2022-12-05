package com.github.sanctum.panther.net.http;

/**
 * HttpGetter that is able to pass data for further processing
 *
 * @param <T> the intermediate result type for further processing
 */
public interface HttpConsumer<T> extends HttpGetter<T> {

	/**
	 * Whether the data has been consumed already.
	 * Important for avoiding unnecessary updates.
	 *
	 * @return true if the data has been consumed
	 */
	boolean isConsumed();

	/**
	 * Starts the consumption of the intermediate result.
	 * The data has to be downloaded before that.
	 *
	 * @throws IllegalStateException if the data hasn't been loaded yet.
	 */
	void consume();

	/**
	 * Downloads a new set of data and starts its consumption.
	 */
	void loadAndConsume();

}
