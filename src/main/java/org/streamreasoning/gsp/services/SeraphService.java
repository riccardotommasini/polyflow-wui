package org.streamreasoning.gsp.services;

import com.vaadin.flow.data.provider.ListDataProvider;
import graph.seraph.events.PGraph;
import graph.seraph.events.PGraphImpl;
import graph.seraph.events.PGraphOrResult;
import graph.seraph.events.Result;
import graph.seraph.op.PGraphStreamGenerator;
import graph.seraph.streams.PGStream;
import graph.seraph.syntax.QueryFactory;
import graph.seraph.syntax.SeraphQuery;
import org.springframework.stereotype.Service;
import org.streamreasoning.gsp.data.InputGraph;
import org.streamreasoning.gsp.views.OldPGS;
import org.streamreasoning.rsp4j.api.coordinators.ContinuousProgram;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;
import org.vaadin.addons.visjs.network.main.Edge;
import org.vaadin.addons.visjs.network.main.NetworkDiagram;
import org.vaadin.addons.visjs.network.main.Node;
import org.vaadin.addons.visjs.network.options.Interaction;
import org.vaadin.addons.visjs.network.options.Options;
import org.vaadin.addons.visjs.network.options.edges.ArrowHead;
import org.vaadin.addons.visjs.network.options.edges.Arrows;
import org.vaadin.addons.visjs.network.options.physics.Physics;
import org.vaadin.addons.visjs.network.options.physics.Repulsion;
import shared.coordinators.ContinuousProgramImpl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;


@Service
public class SeraphService {

    private final AtomicInteger eventCounter = new AtomicInteger(1);
    private final ContinuousProgram<PGraph, PGraph, PGraphOrResult, Result> cp;
    private final PGraphStreamGenerator generator;
    private final Map<String, SeraphQuery<PGraph, PGraph, PGraphOrResult, Result>> queries = new HashMap<>();

    private final Map<String, DataStream<PGraph>> streams = new HashMap<>();


    public SeraphService() {
        this.cp = new ContinuousProgramImpl<>();
        this.generator = new PGraphStreamGenerator();

    }

    public static PGraph fromJson(String fileName) {
        System.out.println(fileName);
        try {
            return PGraphImpl.fromJson(new FileReader(OldPGS.class.getClassLoader().getResource(fileName).getPath()));
        } catch (FileNotFoundException e) {
            return PGraphImpl.createEmpty();
        }
    }

    public void updateSnapshotGraphFromContent(NetworkDiagram snapshotGraph, List<Node> nodes, List<Edge> edges, String id) {
        List<PGraph> elements = queries.get(id).getTask().getSDS().toStream().map(PGraphOrResult::getContent).toList();

        nodes.clear();
        edges.clear();

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
                .forEach(nodes::add);

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
                .forEach(edges::add);

        snapshotGraph.getEdgesDataProvider().refreshAll();
        snapshotGraph.getNodesDataProvider().refreshAll();

    }

    public static InputGraph fromJson2(String filename, String s, String labels) {
        return fromJson2(fromJson(filename), s, labels);
    }

    public static InputGraph fromJson2(PGraph pGraph, String s, String labels) {
        Physics physics = new Physics();
        physics.setEnabled(true);
        physics.setSolver(Physics.Solver.repulsion);
        Repulsion repulsion = new Repulsion();
        repulsion.setNodeDistance(1000);
        physics.setRepulsion(repulsion);


        final InputGraph event =
                new InputGraph(Options.builder().withWidth(s).withHeight(s)
                        .withAutoResize(true)
//                        .withLayout(layout)
                        .withPhysics(physics)
                        .withInteraction(Interaction.builder().withMultiselect(true).build()).build(), pGraph.timestamp());

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

        return event;


    }

    public static PGraph fromJson(FileReader fileReader) {
        return PGraphImpl.fromJson(fileReader);
    }

    public String register(String seraphQL, String stream) {
        SeraphQuery<PGraph, PGraph, PGraphOrResult, Result> q = parse(seraphQL, stream);
        DataStream<PGraph> inputStreamColors = q.instream();
        streams.put(stream, inputStreamColors);
        DataStream<Result> outStream = q.outstream();
//        streams.put(q.id(), outStream);
        queries.put(q.id(), q);
        cp.buildTask(q.getTask(), Collections.singletonList(inputStreamColors), Collections.singletonList(outStream));
        return q.id();
    }

    public List<SeraphQuery<PGraph, PGraph, PGraphOrResult, Result>> listQueries() {
        return queries.values().stream().toList();
    }

    public void unregisterQuery(String s) {
//        seraph.unregisterQuery(s);
    }

    public InputGraph nextEvent2(String event, String stream, String s, String labels) {
        PGraph pGraph = nextEvent(event, stream);
        return fromJson2(pGraph, s, labels);
    }

    public PGraph nextEvent(String event, String stream) {
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


    public void send(String stream, PGraph e) {
        streams.computeIfPresent(stream, (s, pGraphDataStream) -> {
            pGraphDataStream.put(e, System.currentTimeMillis());
            return pGraphDataStream;
        });
    }

    public PGStream register(PGStream s) {
//        return seraph.register(s);
        return s;
    }


    public SeraphQuery<PGraph, PGraph, PGraphOrResult, Result> parse(String value, String stream) {
        try {
            return QueryFactory.parse(value, stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public List<String> getResultVars(String cqe) {
        return queries.get(cqe).getResultVars();
    }

    public DataStream<Result> outstream(String id) {
        return queries.get(id).outstream();
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

}
