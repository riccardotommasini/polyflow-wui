package org.streamreasoning.gsp.services;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.f0rce.ace.AceEditor;
import graph.ContinuousQuery;
import graph.jena.datatypes.JenaGraphOrBindings;
import graph.jena.syntax.RSPQLQueryFactory;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.engine.binding.Binding;
import org.springframework.stereotype.Service;
import org.streamreasoning.gsp.data.InputGraph;
import org.streamreasoning.polyflow.api.sds.SDS;
import org.streamreasoning.polyflow.api.stream.data.DataStream;
import org.streamreasoning.polyflow.base.processing.ContinuousProgramImpl;
import org.vaadin.addons.visjs.network.main.Edge;
import org.vaadin.addons.visjs.network.main.NetworkDiagram;
import org.vaadin.addons.visjs.network.main.Node;
import org.vaadin.addons.visjs.network.options.Interaction;
import org.vaadin.addons.visjs.network.options.Options;
import org.vaadin.addons.visjs.network.options.edges.ArrowHead;
import org.vaadin.addons.visjs.network.options.edges.Arrows;
import org.vaadin.addons.visjs.network.options.physics.Physics;
import org.vaadin.addons.visjs.network.options.physics.Repulsion;
import org.vaadin.addons.visjs.network.util.Shape;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;


@Service
public class RSPService extends QueryService<Graph, Graph, JenaGraphOrBindings, Binding> {

    public RSPService() {
        super(new ContinuousProgramImpl<>());
    }

    public static Graph fromFile(String fileName) {
        System.out.println(fileName);
        return RDFDataMgr.loadGraph(RSPService.class.getClassLoader().getResource(fileName).getPath());
    }

    private String register(String seraphQL, String stream) {
        ContinuousQuery<Graph, Graph, JenaGraphOrBindings, Binding> q = RSPQLQueryFactory.parse(seraphQL);
        DataStream<Graph> inputStreamColors = q.instream().get(0);
        streams.put(stream, inputStreamColors);
        DataStream<Binding> outStream = q.outstream();
        queries.put(q.id(), q);
        cp.buildTask(q.getTask(), Collections.singletonList(inputStreamColors), Collections.singletonList(outStream));
        return q.id();
    }


    public static InputGraph loadEvent(String filename) {
        return loadEvent(fromFile(filename));
    }

    public static InputGraph loadEvent(Graph pGraph) {
        Physics physics = new Physics();
        physics.setEnabled(true);
        physics.setSolver(Physics.Solver.repulsion);
        Repulsion repulsion = new Repulsion();
        repulsion.setNodeDistance(1000);
        physics.setRepulsion(repulsion);

        final InputGraph event = new InputGraph(Options.builder()
                //.withWidth(s).withHeight(s).withAutoResize(true)
//                        .withLayout(layout)
                .withPhysics(physics).withInteraction(Interaction.builder().withMultiselect(true).build()).build(), System.currentTimeMillis());

        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        pGraph.stream().forEach(t -> {

            String subject = getID(t.getSubject());
            String predicate = getID(t.getPredicate());
            String object = getID(t.getObject());

            Node subjectNode = findNodeById(subject, nodes);
            if (subjectNode == null) {
                subjectNode = new Node(subject, subject);
                subjectNode.setShape(Shape.dot);
                nodes.add(subjectNode);
            }

            Node objectNode = findNodeById(object, nodes);
            if (objectNode == null) {
                objectNode = new Node(object, object);
                objectNode.setShape(Shape.dot);
                nodes.add(objectNode);
            }

            Edge pred = new Edge(subjectNode, objectNode);
            pred.setLabel(predicate);
            pred.setArrows(new Arrows(new ArrowHead()));
            edges.add(pred);

        });

        LinkedList<Node> ns = new LinkedList<>(nodes);
        final var dataProvider = new ListDataProvider<>(ns);
        LinkedList<Edge> es = new LinkedList<>(edges);
        final var edgeProvider = new ListDataProvider<>(es);

        event.setNodesDataProvider(dataProvider);
        event.setEdgesDataProvider(edgeProvider);

        return event;
    }

    @Override
    public InputGraph sendEvent(String event, String stream) {
        Graph pGraph = nextEvent(event, stream);
        return loadEvent(pGraph);
    }

    @Override
    public void registerNewQuery(String inputStream, DataComponent snapshotGraphFunction, DataComponent snapshotGraphSolo, HorizontalLayout tvttab, TimePicker timePicker1, TabSheet processingTabSheet, AceEditor editor, VerticalLayout outputRowContainer) {
        Grid<Binding> lastTAT;
        Component component = processingTabSheet.getComponent(lastTATTab);
        if (component == null) {
            processingTabSheet.add(lastTATTab, lastTAT = new Grid<>(Binding.class));
            lastTAT.setId("lastTAT");
            lastTAT.setWidth("100%");
            lastTAT.setHeight("100%");
            lastTAT.setPageSize(10);
            lastTAT.getStyle().setFontSize("1em");
        } else
            lastTAT = (Grid<Binding>) component;

        Grid<Binding> nowgrid;
        tvttab.replace(tvttab.getChildren().toList().get(2), nowgrid = new Grid<>(Binding.class));

        nowgrid.setId("nowgrid");
        nowgrid.setWidth("100%");
        nowgrid.setHeight("100%");
        nowgrid.setPageSize(10);
        nowgrid.getStyle().setFontSize("12px");

        String cqe = register(editor.getValue(), inputStream);

        HorizontalLayout outputRow = new HorizontalLayout();
        String id = cqe; //TODO nel task
        outputRow.setId(id);
        outputRow.addClassName(LumoUtility.Gap.MEDIUM);
        outputRow.setWidth("100%");
        outputRow.setHeight("200px");

        outputRow.add(new H4(id));

        outputRowContainer.add(outputRow);

        List<Binding> res = new ArrayList<>();
        ListDataProvider<Binding> resultDataProvider = new MyDataProvider<>(res);

        lastTAT.setDataProvider(resultDataProvider);
        nowgrid.setDataProvider(resultDataProvider);

        queries.get(cqe).getResultVars().forEach(k -> {
            lastTAT.addColumn(map -> map.get(k)).setHeader(k);
            nowgrid.addColumn(map -> map.get(k)).setHeader(k);
        });

        lastTAT.getColumnByKey("empty").setVisible(false);
        nowgrid.getColumnByKey("empty").setVisible(false);

        lastTAT.addColumn(map -> map.get("Id")).setHeader("Id");
        nowgrid.addColumn(map -> map.get("Id")).setHeader("Id");

        lastTAT.addColumn(map -> map.get("win_start")).setHeader("win_start");
        nowgrid.addColumn(map -> map.get("win_start")).setHeader("win_start");

        lastTAT.addColumn(map -> map.get("win_end")).setHeader("win_end");
        nowgrid.addColumn(map -> map.get("win_end")).setHeader("win_end");

//        addConsumer(id, lastTAT, nowgrid, res, resultDataProvider, timePicker1, outputRow, snapshotGraphFunction, snapshotGraphSolo, nodes, edges);

        queries.get(id).outstream().addConsumer((out, result, ts) -> {
//            result.put("Id", idCounter.getAndIncrement());

            lastTAT.getColumnByKey("empty").setVisible(false);
            nowgrid.getColumnByKey("empty").setVisible(false);

            res.add(result);

            resultDataProvider.refreshAll();

            //TODO add focus based on selected query
            appendResultTable(outputRow, result, ts, res);
            SDS<JenaGraphOrBindings> sds = queries.get(id).getTask().getSDS();

            updateSnapshotGraphFromContent((NetworkDiagram) snapshotGraphFunction, sds);
            updateSnapshotGraphFromContent((NetworkDiagram) snapshotGraphSolo, sds);
            timePicker1.setValue(LocalTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault()));
            System.out.println(result);
        });

        Notification.show("Query " + cqe + " Was successfully registered");
    }

    private Graph nextEvent(String event, String stream) {
        eventCounter.compareAndSet(4, 1);
        Graph pGraph2 = RDFDataMgr.loadGraph(RSPService.class.getClassLoader().getResource(event + (eventCounter.getAndIncrement()) + ".jsonld").getPath());
        streams.computeIfPresent(stream, (s, pGraphDataStream) -> {
            pGraphDataStream.put(pGraph2, System.currentTimeMillis());
            return pGraphDataStream;
        });
        return pGraph2;
    }

    private void updateSnapshotGraphFromContent(NetworkDiagram snapshotGraph, SDS<JenaGraphOrBindings> sds) {
        List<Graph> elements = sds.toStream().map(JenaGraphOrBindings::getContent).toList();

        ListDataProvider<Edge> edgesDataProvider = (ListDataProvider<Edge>) snapshotGraph.getEdgesDataProvider();
        ListDataProvider<Node> nodesDataProvider = (ListDataProvider<Node>) snapshotGraph.getNodesDataProvider();

        Collection<Edge> edges1 = edgesDataProvider.getItems();

        edges1.clear();

        Collection<Node> nodes1 = nodesDataProvider.getItems();

        nodes1.clear();

        elements.stream().flatMap(Graph::stream).forEach(t -> {

            String subject = getID(t.getSubject());
            String predicate = getID(t.getPredicate());
            String object = getID(t.getObject());

            Node subjectNode = findNodeById(subject, nodes1);
            if (subjectNode == null) {
                subjectNode = new Node(subject, subject);
                subjectNode.setShape(Shape.dot);
                nodes1.add(subjectNode);
            }

            Node objectNode = findNodeById(object, nodes1);
            if (objectNode == null) {
                objectNode = new Node(object, object);
                objectNode.setShape(Shape.dot);
                nodes1.add(objectNode);
            }

            Edge pred = new Edge(subjectNode, objectNode);
            pred.setLabel(predicate);
            pred.setArrows(new Arrows(new ArrowHead()));
            edges1.add(pred);

        });

        edgesDataProvider.refreshAll();
        nodesDataProvider.refreshAll();

    }

    private void appendResultTable(HorizontalLayout outputRow, Binding arg, long ts, List<Binding> res) {
        outputRow.getChildren()
                .filter(component -> !(component instanceof H4))
                .map(obj -> (Grid) obj)
                .filter(grid -> grid.getId().filter(id -> id.equals(ts + ""))
                        .isPresent())
                .findFirst().or(() -> {
                    Grid<Binding> g = new Grid(Binding.class);
                    List<Binding> items = new ArrayList<>();
                    MyDataProvider<Binding> mapDP = new MyDataProvider<>(items);
                    g.setDataProvider(mapDP);
                    g.setId(ts + "");

                    //This part depends on query output
                    arg.varsMentioned().forEach(k -> g.addColumn(map -> map.get(k)).setHeader(k.getVarName()));

                    res.clear();

                    g.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
                    g.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
                    g.setWidth("100%");
                    g.setHeight("100%");
                    g.setPageSize(10);
                    outputRow.add(g);
                    return Optional.of(g);
                }).ifPresent(g -> ((ListDataProvider) g.getDataProvider()).getItems().add(arg));
    }


    private static String getID(org.apache.jena.graph.Node n) {
        if (n.isBlank()) return n.getBlankNodeLabel();
        if (n.isLiteral()) return n.getLiteral().getLexicalForm();
        return n.getURI();
    }

    private static Node findNodeById(String id, Collection<Node> nodes) {
        for (Node node : nodes) {
            if (node.getId().equals(id)) {
                return node;
            }
        }
        return null;
    }

}
