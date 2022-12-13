package com.github.sanctum.panther.net.http;

import java.util.function.Consumer;

/**
 * Representation of an HttpGetter that is able to pass data for further processing.
 *
 * @since 1.0.2
 * @author Rigobert0
 * @param <T> the intermediate result type for further processing
 */
public interface HttpConsumer<T> extends HttpGetter<T> {

    /**
     * Checks whether the data has been consumed already.
     * <p>
     * If false is returned, {@link #consume()} will throw an exception by contract.
     *
     * @return false by default if no restriction flag has been set,
     * true if single consumption flag has been set and the data has been consumed already
     * @see HttpUtils.ConsumerBuilder#restrictMultipleUsage(boolean)
     */
    boolean isConsumed();

    /**
     * Starts consumption of the intermediate result.
     * <p>
     * All consumers attached to this HttpConsumer will get the result to process.
     * <p>
     * The data has to be already downloaded before that.
     *
     * @throws IllegalStateException when the data hasn't been loaded yet or
     *                               the current data set has already been consumed when consumption restriction has been set.
     * @see HttpUtils.HttpGetterBuilder#addConsumer(Consumer)
     */
    void consume();

    /**
     * Downloads a new set of data and starts its consumption.
     */
    void loadAndConsume();

}
