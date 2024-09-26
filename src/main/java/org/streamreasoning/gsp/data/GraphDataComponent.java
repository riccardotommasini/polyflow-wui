package org.streamreasoning.gsp.data;

import com.vaadin.flow.data.provider.ListDataProvider;
import org.streamreasoning.gsp.services.DataComponent;
import org.vaadin.addons.visjs.network.main.Edge;
import org.vaadin.addons.visjs.network.main.NetworkDiagram;
import org.vaadin.addons.visjs.network.main.Node;
import org.vaadin.addons.visjs.network.options.Options;

public class GraphDataComponent extends NetworkDiagram implements DataComponent {

    ListDataProvider<Node> dataProvider;
    ListDataProvider<Edge> edgeProvider;

    public GraphDataComponent(Options options, ListDataProvider<Node> nodes, ListDataProvider<Edge> edges) {
        super(options);
        dataProvider = nodes;
        edgeProvider = edges;
        this.setNodesDataProvider(nodes);
        this.setEdgesDataProvider(edges);
    }

    @Override
    public void clear() {
        dataProvider.getItems().clear();
        edgeProvider.getItems().clear();
    }

    @Override
    public void add(Object o) {
        if (o instanceof Node) {
            dataProvider.getItems().add((Node) o);
        } else if (o instanceof Edge) {
            edgeProvider.getItems().add((Edge) o);
        } else return;
    }

    @Override
    public void refreshAll() {
        dataProvider.refreshAll();
        edgeProvider.refreshAll();
        diagramFit();
        diagamRedraw();
    }
}
