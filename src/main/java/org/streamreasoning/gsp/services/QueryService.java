package org.streamreasoning.gsp.services;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.provider.ListDataProvider;
import de.f0rce.ace.AceEditor;
import graph.ContinuousQuery;
import org.streamreasoning.polyflow.api.processing.ContinuousProgram;
import org.streamreasoning.polyflow.api.stream.data.DataStream;
import org.vaadin.addons.visjs.network.main.Edge;
import org.vaadin.addons.visjs.network.main.NetworkDiagram;
import org.vaadin.addons.visjs.network.main.Node;
import org.vaadin.addons.visjs.network.options.Options;
import org.vaadin.addons.visjs.network.options.edges.ArrowHead;
import org.vaadin.addons.visjs.network.options.edges.Arrows;
import org.vaadin.addons.visjs.network.util.Shape;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class QueryService<I, W, R extends Iterable<?>, O> {

    protected final ContinuousProgram<I, W, R, O> cp;
    protected final Map<String, ContinuousQuery<I, W, R, O>> queries = new HashMap<>();
    protected final Map<String, DataStream<I>> streams = new HashMap<>();
    protected Tab lastTATTab = new Tab("Last Time-Annotated Table");
    protected final AtomicInteger eventCounter = new AtomicInteger(1);
    protected final AtomicInteger idCounter = new AtomicInteger(1);
    protected final AtomicInteger WID = new AtomicInteger(1);

    protected QueryService(ContinuousProgram<I, W, R, O> cp) {
        this.cp = cp;
    }

    public abstract Component sendEvent(String event, String stream);

    protected abstract void registerNewQuery(String inputStream, DataComponent snapshotGraphFunction, DataComponent snapshotGraphSolo, HorizontalLayout tvttab, TimePicker timePicker1, TabSheet processingTabSheet, AceEditor editor, VerticalLayout outputRowContainer);

    public void unregisterQuery(String s) {
        ContinuousQuery<I, W, R, O> remove = queries.remove(s);
//        cp.removeTask(); todo

    }

    public List<String> listQueries() {
        return queries.keySet().stream().toList();
    }

    public List<String> getResultVars(String q) {
        return queries.get(q).getResultVars();
    }

    public NetworkDiagram getQueryPlan(String seraphQuery) {
        final NetworkDiagram plan = new NetworkDiagram(Options.builder().withWidth("100%").withHeight("100%").build());
        final List<Node> nodes = new LinkedList<>();
        final List<Edge> edges = new LinkedList<>();
        AtomicInteger idCounter = new AtomicInteger();

        Node e = new Node("0", "ProduceResults ");
        e.setShape(Shape.square);
        nodes.add(e);

        Node e1 = new Node("1", "Filter ");
        e1.setShape(Shape.square);

        nodes.add(e1);
        Node e2 = new Node("2", "DirectedRelationshipTypeScan ");
        e2.setShape(Shape.square);
        nodes.add(e2);

        Edge e3 = new Edge("0", "1");
        e3.setArrows(new Arrows(new ArrowHead()));
        edges.add(e3);
        Edge e4 = new Edge("1", "2");
        e4.setArrows(new Arrows(new ArrowHead()));
        edges.add(e4);

        final var dataProvider = new ListDataProvider<Node>(nodes);
        final var edgeProvider = new ListDataProvider<Edge>(edges);

        plan.setNodesDataProvider(dataProvider);
        plan.setEdgesDataProvider(edgeProvider);

        return plan;
    }

    public static class MyDataProvider<T> extends ListDataProvider<T> {

        public MyDataProvider(Collection<T> items) {
            super(items);
        }

        @Override
        public Collection<T> getItems() {
            return super.getItems();
        }

        @Override
        public String getId(T item) {
            Objects.requireNonNull(item, "Cannot provide an id for a null item.");
            if (item instanceof Map<?, ?> map) {
                if (map.get("id") != null) return map.get("id").toString();
                else return item.toString();
            } else {
                return item.toString();
            }
        }

    }

    protected <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}