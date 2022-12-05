package com.github.sanctum.panther.net.http;

/**
 * Represents an HTTP Data Downloader.
 * <p>
 * It is capable of loading and updating data via HTTP, storing the results in
 * an intermediate format of type <code>T</code>.
 * Note that the first data loading has to be called manually, making this
 * callable from a network thread.
 *
 * @param <T> the intermediate type
 */
public interface HttpGetter<T> {

    /**
     * Check whether a data set has already been retrieved.
     *
     * @return the loaded state
     */
    boolean isLoaded();

    /**
     * Download a new set of data.
     */
    void load();

    /**
     * Get the intermediate data.
     *
     * @return the intermediate result data
     * @throws IllegalStateException if no data has been loaded yet
     */
    T getData();

    /**
     * Download data and get the intermediate result.
     *
     * @return the intermediate result data
     */
    T loadAndGet();

}
