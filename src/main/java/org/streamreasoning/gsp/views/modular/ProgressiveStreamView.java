package org.streamreasoning.gsp.views.modular;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import org.streamreasoning.gsp.data.InputGraph;
import org.streamreasoning.gsp.services.SeraphService;
import org.vaadin.addons.visjs.network.main.Edge;
import org.vaadin.addons.visjs.network.main.NetworkDiagram;
import org.vaadin.addons.visjs.network.main.Node;
import org.vaadin.addons.visjs.network.options.HierarchicalLayout;
import org.vaadin.addons.visjs.network.options.Interaction;
import org.vaadin.addons.visjs.network.options.Options;
import org.vaadin.addons.visjs.network.options.edges.Layout;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgressiveStreamView extends HorizontalLayout {

    private int capacity;
    private final HorizontalLayout streamView = new HorizontalLayout();
    private final Icon refreshIcon = new Icon(VaadinIcon.ARROW_UP);
    public final AtomicInteger eventCounter = new AtomicInteger(0);
    public String currentEventIndex;

    public ProgressiveStreamView(int capacity) {
        this.capacity = capacity;
        refreshIcon.getStyle().set("color", "blue").set("width", "24px").set("height", "24px");
        for (int i = 0; i <= capacity; i++) {
            HorizontalLayout cursor = new HorizontalLayout();
            cursor.setHeight("5%");
            NetworkDiagram networkDiagram = newEvent();
            networkDiagram.setHeight("80%");

            if (i == 0) {
                cursor.add(refreshIcon);
            }

            VerticalLayout verticalLayout = new VerticalLayout(networkDiagram, cursor);
            verticalLayout.setSpacing(false);
            verticalLayout.setMargin(false);
            verticalLayout.setPadding(false);
            verticalLayout.setAlignItems(Alignment.CENTER);
            verticalLayout.setHeight("100%");
            streamView.add(verticalLayout);
        }

        this.add(streamView);
    }

    private static NetworkDiagram newEvent() {
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

        final var dataProvider1 = new ListDataProvider<Node>(placehodlerNodes);
        final var edgeProvider1 = new ListDataProvider<Edge>(placehodlerEdges);

        placeHolder1.setEdgesDataProvider(edgeProvider1);
        placeHolder1.setNodesDataProvider(dataProvider1);
        return placeHolder1;
    }


    public void reload(String event, int capacity) {
        streamView.removeAll();
        eventCounter.set(0);
        this.currentEventIndex = event;
        this.capacity = capacity;
        for (int i = 0; i <= capacity; i++) {
            HorizontalLayout cursor = new HorizontalLayout();
            cursor.setHeight("5%");
            String filename = event + (i % 10) + ".json";
            System.out.println(filename);
            InputGraph e = SeraphService.loadEvent(filename);
            e.setHeight("80%");
            if (i == 0) {
                cursor.add(refreshIcon);
            }
            this.currentEventIndex = event;
            streamView.add(new VerticalLayout(e, cursor, e.popup()));
        }
    }

    public Object getCurrentEvent() {
        if (eventCounter.compareAndSet(capacity, 0)) {
            reload(currentEventIndex, capacity);
            return ((InputGraph) streamView.getChildren().toList().get(eventCounter.get()).getChildren().toList().get(0)).event;
        }

        VerticalLayout component = (VerticalLayout) streamView.getChildren().toList().get(eventCounter.getAndIncrement());
        InputGraph g = (InputGraph) component.getChildren().toList().get(0);
        HorizontalLayout cursor = (HorizontalLayout) component.getChildren().toList().get(1);
        cursor.removeAll();

        HorizontalLayout newCursor = (HorizontalLayout) streamView.getChildren().toList().get(eventCounter.get()).getChildren().toList().get(1);
        newCursor.add(refreshIcon);

        return g.event;
    }
}
