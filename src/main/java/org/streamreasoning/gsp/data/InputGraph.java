package org.streamreasoning.gsp.data;

import org.vaadin.addons.visjs.network.main.NetworkDiagram;
import org.vaadin.addons.visjs.network.options.Options;

public class InputGraph extends NetworkDiagram {
    public final Long timestamp;

    public InputGraph(Options options, Long timestamp) {
        super(options);
        this.timestamp = timestamp;
    }
}
