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
import graph.seraph.events.PGraph;
import graph.seraph.events.PGraphImpl;
import graph.seraph.events.PGraphOrResult;
import graph.seraph.events.Result;
import graph.seraph.syntax.SeraphQueryFactory;
import org.springframework.stereotype.Service;
import org.streamreasoning.gsp.data.InputGraph;
import org.streamreasoning.polyflow.api.sds.SDS;
import org.streamreasoning.polyflow.api.stream.data.DataStream;
import org.streamreasoning.polyflow.base.processing.ContinuousProgramImpl;
import org.vaadin.addons.visjs.network.main.Edge;
import org.vaadin.addons.visjs.network.main.Node;
import org.vaadin.addons.visjs.network.options.Interaction;
import org.vaadin.addons.visjs.network.options.Options;
import org.vaadin.addons.visjs.network.options.edges.ArrowHead;
import org.vaadin.addons.visjs.network.options.edges.Arrows;
import org.vaadin.addons.visjs.network.options.physics.Physics;
import org.vaadin.addons.visjs.network.options.physics.Repulsion;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;


@Service
public class SeraphService extends QueryService<PGraph, PGraph, PGraphOrResult, Result> {

    public SeraphService() {
        super(new ContinuousProgramImpl<>());
    }

    public static PGraph fromFile(String fileName) {
        System.out.println(fileName);
        try {
            return PGraphImpl.fromJson(new FileReader(SeraphService.class.getClassLoader().getResource(fileName).getPath()));
        } catch (FileNotFoundException e) {
            return PGraphImpl.createEmpty();
        }
    }

    public static InputGraph loadEvent(String filename) {
        return loadEvent(fromFile(filename));
    }

    public static InputGraph loadEvent(PGraph pGraph) {
        Physics physics = new Physics();
        physics.setEnabled(true);
        physics.setSolver(Physics.Solver.repulsion);
        Repulsion repulsion = new Repulsion();
        repulsion.setNodeDistance(1000);
        physics.setRepulsion(repulsion);

        final InputGraph event =
                new InputGraph(Options.builder()
                        //.withWidth(percentage).withHeight(percentage)
                        .withAutoResize(true)
//                        .withLayout(layout)
                        .withPhysics(physics)
                        .withInteraction(Interaction.builder()
                                .withMultiselect(true).build()).build(), pGraph.timestamp());

        List<Node> ns = Arrays.stream(pGraph.nodes()).sequential().map(n -> {
            Node node = new Node(n.id() + "", n.labels()[0] + "\n" + n.id());
            if ("Station".equals(n.labels()[0])) {
                node.setColor("red");
            }
            return node;
        }).toList();

        List<Edge> edges1 = Arrays.stream(pGraph.edges()).map(e -> {
                    Edge edge = new Edge(e.from() + "", e.to() + "");
                    edge.setLabel(e.labels()[0] + "\n" +
                                  "user_id:" + e.property("user_id") + "\n" +
                                  "val_time:" + e.property("val_time") + "\n" + "");

                    return edge;
                })
                .map(edge -> {
                    if (edge.getLabel().contains("returnedAt")) {
                        edge.setColor("orange");
                    }
                    Arrows arrowsObject = new Arrows(new ArrowHead());
                    edge.setArrows(arrowsObject);
                    return edge;
                }).toList();


        LinkedList<Node> nodes = new LinkedList<>(ns);
        final var dataProvider = new ListDataProvider<>(nodes);
        LinkedList<Edge> edges = new LinkedList<>(edges1);
        final var edgeProvider = new ListDataProvider<>(edges);

        event.setNodesDataProvider(dataProvider);
        event.setEdgesDataProvider(edgeProvider);


        event.addAttachListener(event1 -> {
            event.diagamRedraw();
            event.diagramFit();
        });

        event.addSelectListener(click -> {
            Notification.show("The event timestamp is [" +
                              event.timestamp +
                              "]");
        });

        return event;


    }

    private String register(String seraphQL, String stream) {
        try {
            ContinuousQuery<PGraph, PGraph, PGraphOrResult, Result> q = SeraphQueryFactory.parse(seraphQL, stream);
            DataStream<PGraph> inputStreamColors = q.instream().get(0);
            streams.put(stream, inputStreamColors);
            DataStream<Result> outStream = q.outstream();
            queries.put(q.id(), q);
            cp.buildTask(q.getTask(), Collections.singletonList(inputStreamColors), Collections.singletonList(outStream));
            return q.id();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputGraph sendEvent(String event, String stream) {
        PGraph pGraph = nextEvent(event, stream);
        return loadEvent(pGraph);
    }

    public void registerNewQuery(String inputStream, DataComponent snapshotGraphFunction, DataComponent snapshotGraphSolo, HorizontalLayout tvttab, TimePicker timePicker1, TabSheet processingTabSheet, AceEditor editor, VerticalLayout outputRowContainer) {
//todo if processingTabSheet contains already, ti should not recreate it.
        Grid<Result> lastTAT;
        Component component = processingTabSheet.getComponent(lastTATTab);
        if (component == null) {
            processingTabSheet.add(lastTATTab, lastTAT = new Grid<>(Result.class));
            lastTAT.setId("lastTAT");
            lastTAT.setWidth("100%");
            lastTAT.setHeight("100%");
            lastTAT.setPageSize(10);
            lastTAT.getStyle().setFontSize("1em");
        } else
            lastTAT = (Grid<Result>) component;

        Grid<Result> nowgrid;
        tvttab.replace(tvttab.getChildren().toList().get(2), nowgrid = new Grid<>(Result.class));

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

        List<Result> res = new ArrayList<>();
        ListDataProvider<Result> resultDataProvider = new MyDataProvider<>(res);

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
            result.put("Id", idCounter.getAndIncrement());

            lastTAT.getColumnByKey("empty").setVisible(false);
            nowgrid.getColumnByKey("empty").setVisible(false);

            res.add(result);

            resultDataProvider.refreshAll();

            //TODO add focus based on selected query
            appendResultTable(outputRow, result, ts, res);

            updateSnapshotGraphFromContent(snapshotGraphFunction, queries.get(id).getTask().getSDS());
            updateSnapshotGraphFromContent(snapshotGraphSolo, queries.get(id).getTask().getSDS());
            timePicker1.setValue(LocalTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault()));
            System.out.println(result);
        });

        Notification.show("Query " + cqe + " Was successfully registered");
    }

    private PGraph nextEvent(String event, String stream) {
        eventCounter.compareAndSet(10, 1);
        URL url = SeraphService.class.getClassLoader().getResource(event + (eventCounter.getAndIncrement()) + ".json");
        try (FileReader fileReader = new FileReader(url.getPath())) {
            PGraph pGraph2 = PGraphImpl.fromJson(fileReader);
            streams.computeIfPresent(stream, (s, pGraphDataStream) -> {
                pGraphDataStream.put(pGraph2, System.currentTimeMillis());
                return pGraphDataStream;
            });
            return pGraph2;
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    private void updateSnapshotGraphFromContent(DataComponent snapshotGraph, SDS<PGraphOrResult> sds) {
        List<PGraph> elements = sds.toStream().map(PGraphOrResult::getContent).toList();
//
//        ListDataProvider<Node> nodesDataProvider = (ListDataProvider<Node>) snapshotGraph.getNodesDataProvider();
//        ListDataProvider<Edge> edgesDataProvider = (ListDataProvider<Edge>) snapshotGraph.getEdgesDataProvider();
//
//        Collection<Edge> edges = edgesDataProvider.getItems();
//        Collection<Node> nodes = nodesDataProvider.getItems();

//        edges.clear();
//        nodes.clear();

        snapshotGraph.clear();

        elements
                .stream().flatMap(pGraph -> {
                    Stream<PGraph.Node> stream = Arrays.stream(pGraph.nodes());
                    return stream;
                })
                .map(n -> {
                    Node node = new Node(n.id() + "", n.labels()[0] + "\n" + n.id());
                    if ("Station".equals(n.labels()[0])) {
                        node.setColor("red");
                    }
                    return node;
                })
                .filter(distinctByKey(Node::getId))
                .forEach(snapshotGraph::add);


        elements.stream()
                .flatMap(pGraph -> Arrays.stream(pGraph.edges()))
                .map(e -> {
                    Edge edge = new Edge(e.from() + "", e.to() + "");
                    edge.setLabel(e.labels()[0] + "\n" +
                                  "user_id:" + e.property("user_id") + "\n" +
                                  "val_time:" + e.property("val_time") + "\n" + "");

                    return edge;
                })
                .map(edge -> {
                    if (edge.getLabel().contains("returnedAt")) {
                        edge.setColor("orange");
                    }
                    Arrows arrowsObject = new Arrows(new ArrowHead());
                    edge.setArrows(arrowsObject);
                    return edge;
                })
                .forEach(snapshotGraph::add);

        snapshotGraph.refreshAll();
//        nodesDataProvider.refreshAll();

    }

    private void appendResultTable(HorizontalLayout outputRow, Result arg, long ts, List<Result> res) {
        outputRow.getChildren()
                .filter(component -> !(component instanceof H4))
                .map(obj -> (Grid) obj)
                .filter(grid -> grid.getId().filter(id -> id.equals(ts + ""))
                        .isPresent())
                .findFirst().or(() -> {
                    Grid<Result> g = new Grid(Result.class);
                    List<Result> items = new ArrayList<>();
                    MyDataProvider<Result> mapDP = new MyDataProvider<>(items);
                    g.setDataProvider(mapDP);
                    g.setId(ts + "");

                    //This part depends on query output
                    arg.keySet().forEach(k -> g.addColumn(map -> map.get(k)).setHeader(k));

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


}
