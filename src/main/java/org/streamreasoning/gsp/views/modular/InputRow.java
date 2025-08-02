package org.streamreasoning.gsp.views.modular;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import org.vaadin.addons.visjs.network.main.Edge;
import org.vaadin.addons.visjs.network.main.NetworkDiagram;
import org.vaadin.addons.visjs.network.main.Node;
import org.vaadin.addons.visjs.network.options.HierarchicalLayout;
import org.vaadin.addons.visjs.network.options.Interaction;
import org.vaadin.addons.visjs.network.options.Options;
import org.vaadin.addons.visjs.network.options.edges.Layout;

import java.util.LinkedList;
import java.util.List;

public class InputRow extends HorizontalLayout {

    HorizontalLayout streamView;

    public InputRow() {

        List<Node> placehodlerNodes = new LinkedList<>();

        Node n1 = new Node("A");
        n1.setColor("#f0f0f0");
        Node n2 = new Node("B");
        n2.setColor("#f0f0f0");

        placehodlerNodes.add(n1);
        placehodlerNodes.add(n2);

        Edge ee = new Edge(n1, n2);
        ee.setColor("black");

        List<Edge> placehodlerEdges = new LinkedList<>();
        placehodlerEdges.add(ee);

        Layout layout = new Layout();
        HierarchicalLayout h = new HierarchicalLayout();
        h.setLayout(HierarchicalLayout.LayoutStyle.direction);
        h.setDirection(HierarchicalLayout.Direction.UD);

        final NetworkDiagram placeHolder1 = new NetworkDiagram(Options.builder().withWidth("50px").withHeight("100%").withLayout(layout).withInteraction(Interaction.builder().withMultiselect(true).build()).build());
        final NetworkDiagram placeHolder2 = new NetworkDiagram(Options.builder().withWidth("50px").withHeight("100%").withLayout(layout).withInteraction(Interaction.builder().withMultiselect(true).build()).build());

        final var dataProvider1 = new ListDataProvider<Node>(placehodlerNodes);
        final var edgeProvider1 = new ListDataProvider<Edge>(placehodlerEdges);

        placeHolder1.setEdgesDataProvider(edgeProvider1);
        placeHolder2.setEdgesDataProvider(edgeProvider1);
        placeHolder1.setNodesDataProvider(dataProvider1);
        placeHolder2.setNodesDataProvider(dataProvider1);

        this.add(placeHolder1);

        placeHolder1.diagramFit();
        placeHolder2.diagramFit();

        this.streamView = new HorizontalLayout();
        streamView.setHeight("100%");
        streamView.getStyle().setBorder("dotted");
        streamView.getStyle().set("border-color", "red");
        streamView.getStyle().set("overflow-x", "auto");
        streamView.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        streamView.getStyle().set("margin-left", "20px");
        streamView.getStyle().set("margin-right", "20px");
        streamView.getStyle().set("background-color", "#f0f0f0"); // Use your desired color code
        this.add(streamView);
        this.setFlexGrow(1.0, streamView);
        this.add(placeHolder2);
    }

    public HorizontalLayout getStreamView() {
        return streamView;
    }
}
