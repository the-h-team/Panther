package com.github.sanctum.panther.net.http;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

class HttpConsumerImpl<T> extends HttpGetterImpl<T> implements HttpConsumer<T> {

    private final List<Consumer<T>> consumers;
    private final boolean restricted;
    private boolean consumed;


    HttpConsumerImpl(final String url, final String subPath, final Map<String, String> arguments,
                     final Function<String, T> dataBuilder, final List<Consumer<T>> consumers,
                     final boolean restricted) {
        super(url, subPath, arguments, dataBuilder);
        this.consumers = consumers;
        this.restricted = restricted;
    }

    public void doConsume() {
        consumers.forEach(c -> c.accept(getData()));
        synchronized (this) {
            consumed = true;
        }
    }

    @Override
    public void load() {
        super.load();
        synchronized (this) {
            consumed = false;
        }
    }

    @Override
    public synchronized boolean isConsumed() {
        return restricted && consumed;
    }

    @Override
    public void consume() {
        validateLoaded();
        validateNotConsumed();
        doConsume();
    }

    @Override
    public void loadAndConsume() {
        load();
        doConsume();
    }

    private void validateNotConsumed() throws IllegalStateException {
        if (isConsumed()) {
            throw new IllegalStateException("Data has already been consumed");
        }
    }

}
