package com.github.sanctum.panther.net.http;

/**
 * Representation of an HttpGetter that processes data further with a second function
 * to a final result.
 * <p>
 * Useful for requests that you are only interested in one small part of.
 * Also, it can be used for extracting a validation flag before continuing processing the intermediate data
 *
 * @since 1.0.2
 * @author Rigobert0
 * @param <T> the intermediate type
 * @param <R> the desired result type
 */
public interface HttpReducer<T, R> extends HttpGetter<T> {

    /**
     * Checks whether the second processing stage has already been performed.
     *
     * @return true if the data has been processed, false if no valid processing result is present
     */
    boolean isProcessed();

    /**
     * Performs the processing stage.
     * <br>
     * <b>This needs having downloaded data to process!<b/>
     *
     * @throws IllegalStateException when no data has been downloaded to process
     */
    void process();

    /**
     * Gets the result of the second processing stage.
     * <p>
     * <b>The data must be processed and loaded before calling this method!</b>
     *
     * @return the results of the second processing stage
     * @throws IllegalStateException if the data hasn't been processed or loaded
     */
    R getResult();

    /**
     * Performs the second processing stage and get the result.
     * <p>
     * <b>The data must be loaded before calling this method!</b>
     *
     * @return the result of second processing
     * @throws IllegalStateException if the data hasn't been loaded
     */
    R processAndGet();

    /**
     * Performs the data loading and the data processing to a final result.
     *
     * @return the result
     */
    R loadProcessAndGet();

}
