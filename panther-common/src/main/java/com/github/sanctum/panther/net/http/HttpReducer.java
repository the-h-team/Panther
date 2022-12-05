package com.github.sanctum.panther.net.http;

/**
 * HttpGetter that processes data further with a second function to a final result.
 * Useful for requests where you are only interested in one small part of and for extracting a validation bool flag
 *
 * @param <T> the intermediate type
 * @param <R> the desired result type
 */
public interface HttpReducer<T, R> extends HttpGetter<T> {

	/**
	 * Whether the second processing stage has already been performed or not
	 *
	 * @return true if the data has been processed.
	 */
	boolean isProcessed();

	/**
	 * Performs the second stage processing
	 */
	void process();

	/**
	 * Returns the result of the second processing stage.
	 * The data needs to be processed and loaded before that.
	 *
	 * @return the result
	 * @throws IllegalStateException when the data hasn't been processed or loaded.
	 */
	R getResult();

	/**
	 * Starts the second processing stage and returns the result.
	 * The data needs to be loaded before that.
	 *
	 * @return the result
	 * @throws IllegalStateException when the data hasn't been loaded.
	 */
	R processAndGet();

	/**
	 * Loads and processes the data for returning a final result
	 *
	 * @return the result
	 */
	R loadProcessAndGet();

}
