package com.github.sanctum.panther.net.http;

/**
 * HttpGetter that processes data further with a second function
 * to a final result.
 * <p>
 * Useful for requests you are only interested in one small part of
 * and for extracting a validation flag.
 *
 * @param <T> the intermediate type
 * @param <R> the desired result type
 */
public interface HttpReducer<T, R> extends HttpGetter<T> {

    /**
     * Whether the second processing stage has already been performed.
     *
     * @return true if the data has been processed
     */
    boolean isProcessed();

    /**
     * Perform the second stage processing.
     */
    void process();

    /**
     * Get the result of the second processing stage.
     * <p>
     * <b>The data must be processed and loaded before calling this method!</b>
     *
     * @return the results of the second processing stage
     * @throws IllegalStateException if the data hasn't been processed or loaded
     */
    R getResult();

    /**
     * Start the second processing stage and get the result.
     * <p>
     * <b>The data must be loaded before calling this method!</b>
     *
     * @return the result of second processing
     * @throws IllegalStateException if the data hasn't been loaded
     */
    R processAndGet();

    /**
     * Load and process the data for returning a final result.
     *
     * @return the result
     */
    R loadProcessAndGet();

}
