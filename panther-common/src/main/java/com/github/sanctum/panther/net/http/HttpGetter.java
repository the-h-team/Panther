package com.github.sanctum.panther.net.http;

/**
 * Represents an HTTP Data Downloader
 * It is capable of loading and updating data via HTTP, storing the results in an intermediate format of type T
 * Note that the first data loading has to be called manually, making this callable from a network thread
 *
 * @param <T> the intermediate type
 */
public interface HttpGetter<T> {

    /**
     * Checks whether a data set already has been retrieved
     *
     * @return the loaded state
     */
    boolean isLoaded();

    /**
     * Downloads a new set of data
     */
    void load();

    /**
     * @return the intermediate result data
     * @throws IllegalStateException if no data has been loaded yet
     */
    T getData();

    /**
     * Downloads the data and returns the intermediate result
     *
     * @return the intermediate result data
     */
    T loadAndGet();

}
