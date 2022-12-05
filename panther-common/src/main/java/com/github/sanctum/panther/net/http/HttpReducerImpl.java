package com.github.sanctum.panther.net.http;

import java.util.Map;
import java.util.function.Function;

class HttpReducerImpl<T, R> extends HttpGetterImpl<T> implements HttpReducer<T, R> {

    private final Function<T, R> processor;
    boolean processed;
    R processedData;

    HttpReducerImpl(final String url, final String subPath, final Map<String, String> arguments,
                    final Function<String, T> dataBuilder, final Function<T, R> processor) {
        super(url, subPath, arguments, dataBuilder);
        this.processor = processor;
    }

    @Override
    public void process() {
        validateLoaded();
        doProcess();
    }

    protected void doProcess() {
        processedData = processor.apply(rawData);
        synchronized (this) {
            processed = true;
        }
    }

    @Override
    public synchronized boolean isProcessed() {
        return processed;
    }

    @Override
    public R getResult() {
        validateLoaded();
        validateProcessed();
        return processedData;
    }

    @Override
    public R processAndGet() {
        validateLoaded();
        doProcess();
        return processedData;
    }

    @Override
    public R loadProcessAndGet() {
        load();
        doProcess();
        return processedData;
    }


    private void validateProcessed() throws IllegalStateException {
        if (!processed) {
            throw new IllegalStateException("Data is not processed yet!");
        }
    }


}
