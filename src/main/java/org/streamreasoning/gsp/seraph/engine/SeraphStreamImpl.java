package org.streamreasoning.gsp.seraph.engine;

import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.Consumer;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.util.ArrayList;
import java.util.List;

public class SeraphStreamImpl<T> implements DataStream<T> {

    protected String stream_uri;
    protected List<Consumer<T>> consumers = new ArrayList<>();

    public SeraphStreamImpl(String stream_uri) {
        this.stream_uri = stream_uri;
    }

    @Override
    public void addConsumer(Consumer<T> c) {
        consumers.add(c);
    }

    @Override
    public void put(T e, long ts) {
        consumers.forEach(graphConsumer -> graphConsumer.notify(e, ts));
    }

    @Override
    public String getName() {
        return stream_uri;
    }

    public String uri() {
        return stream_uri;
    }
}
