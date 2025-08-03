package org.streamreasoning.gsp.data;

import org.vaadin.addons.visjs.network.main.NetworkDiagram;
import org.vaadin.addons.visjs.network.options.Options;

public class InputGraph extends NetworkDiagram {
    public final Long timestamp;
    public final Object event;

    public InputGraph(Options options, Long timestamp, Object e) {
        super(options);
        this.timestamp = timestamp;
        this.event = e;
    }

}
