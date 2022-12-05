package com.github.sanctum.panther.net.http;

/**
 * HttpGetter that is able to pass data for further processing.
 *
 * @param <T> the intermediate result type for further processing
 */
public interface HttpConsumer<T> extends HttpGetter<T> {

    /**
     * Check whether the data has been consumed already.
     * <p>
     * Returns false by default, this feature can be enabled in the building process to avoid unnecessary updates.
     *
     * @return true if the data has been consumed. In this case, {@link #consume()} will throw an exception.
     * @see HttpUtils.ConsumerBuilder#restrictMultipleUsage(boolean)
     */
    boolean isConsumed();

    /**
     * Start consumption of the intermediate result.
     * <p>
     * The data has to be downloaded before that.
     *
     * @throws IllegalStateException if the data hasn't been loaded yet or
     *                               the current data set has already been consumed.
     */
    void consume();

    /**
     * Download a new set of data and starts its consumption.
     */
    void loadAndConsume();

}
