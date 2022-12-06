package com.github.sanctum.panther.net.http;

/**
 * Representation of an HTTP Data Downloader.
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
     * Checks whether a data set has already been retrieved.
     *
     * @return true if data is present, false otherwise
     */
    boolean isLoaded();

    /**
     * Downloads a new set of data.
     */
    void load();

    /**
     * Gets the intermediate data.
     * This method needs the getter having loaded data already, which you can tell by {@link #isLoaded()}
     *
     * @return the intermediate result data
     * @throws IllegalStateException if no data has been loaded yet
     */
    T getData();

    /**
     * Downloads new data and gets the intermediate result.
     *
     * @return the intermediate result data
     */
    T loadAndGet();

}
