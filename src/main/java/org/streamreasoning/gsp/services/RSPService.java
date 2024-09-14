package org.streamreasoning.gsp.services;

import com.vaadin.flow.data.provider.ListDataProvider;
import graph.ContinuousQuery;
import graph.jena.datatypes.JenaGraphOrBindings;
import graph.jena.syntax.RSPQLQueryFactory;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.engine.binding.Binding;
import org.springframework.stereotype.Service;
import org.streamreasoning.gsp.data.InputGraph;
import org.streamreasoning.polyflow.api.processing.ContinuousProgram;
import org.streamreasoning.polyflow.api.processing.Task;
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

import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;


@Service
public class RSPService {

    private final AtomicInteger eventCounter = new AtomicInteger(1);
    private final ContinuousProgram<Graph, Graph, JenaGraphOrBindings, Binding> cp;
    private final Map<String, ContinuousQuery<Graph, Graph, JenaGraphOrBindings, Binding>> queries = new HashMap<>();
    private final Map<String, DataStream<Graph>> streams = new HashMap<>();


    public RSPService() {
        this.cp = new ContinuousProgramImpl<>();
    }


    public void updateSnapshotGraphFromContent(NetworkDiagram snapshotGraph, List<Node> nodes, List<Edge> edges, String id) {
        List<Graph> elements = queries.get(id).getTask().getSDS().toStream().map(JenaGraphOrBindings::getContent).toList();

        nodes.clear();
        edges.clear();


        elements.stream().flatMap(Graph::stream).forEach(t -> {

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

        snapshotGraph.getEdgesDataProvider().refreshAll();
        snapshotGraph.getNodesDataProvider().refreshAll();

    }

    private static String getID(org.apache.jena.graph.Node n) {
        if (n.isBlank()) return n.getBlankNodeLabel();
        if (n.isLiteral()) return n.getLiteral().getLexicalForm();
        return n.getURI();
    }

    public static InputGraph fromFile(String filename, String s, String labels) {
        Graph graph = RDFDataMgr.loadGraph(filename);
        return fromGraph(graph, s, labels);
    }

    public static InputGraph fromGraph(Graph pGraph, String s, String labels) {
        Physics physics = new Physics();
        physics.setEnabled(true);
        physics.setSolver(Physics.Solver.repulsion);
        Repulsion repulsion = new Repulsion();
        repulsion.setNodeDistance(1000);
        physics.setRepulsion(repulsion);

        final InputGraph event = new InputGraph(Options.builder().withWidth(s).withHeight(s).withAutoResize(true)
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

    public String register(String seraphQL, String selectedStream) {
        ContinuousQuery<Graph, Graph, JenaGraphOrBindings, Binding> parse = RSPQLQueryFactory.parse(seraphQL);
        queries.put(parse.id(), parse);
        parse.instream().forEach(i -> streams.put(i.getName(), i));
        cp.buildTask(parse.getTask(), parse.instream(), List.of(parse.outstream()));
        return parse.id();
    }

    public List<ContinuousQuery<Graph, Graph, JenaGraphOrBindings, Binding>> listQueries() {
        return queries.values().stream().toList();
    }

    public void unregisterQuery(String s) {
//        seraph.unregisterQuery(s);
    }

    public InputGraph nextEvent2(String event, String stream, String s, String labels) {
        Graph pGraph = nextEvent(event, stream);
        return fromGraph(pGraph, s, labels);
    }

    public Graph nextEvent(String event, String stream) {
        eventCounter.compareAndSet(2, 1);
        URL url = RSPService.class.getClassLoader().getResource(event + (eventCounter.getAndIncrement()) + ".jsonld");
        Graph pGraph2 = RDFDataMgr.loadGraph(url.getPath());
        streams.computeIfPresent(stream, (s, pGraphDataStream) -> {
            pGraphDataStream.put(pGraph2, System.currentTimeMillis());
            return pGraphDataStream;
        });
        return pGraph2;
    }


    public Task<Graph, Graph, JenaGraphOrBindings, Binding> parse(String value, String stream) {
//        try {
//            return QueryFactory.parse(value, stream);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        return null;
    }


    public List<String> getResultVars(String s) {
        return queries.get(s).getResultVars();
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public DataStream<Binding> outstream(String id) {
        return queries.get(id).outstream();
    }

    /**
     * Find node in network diagram by ID
     *
     * @param id String
     * @return Node
     */
    public static Node findNodeById(String id, List<Node> nodes) {
        for (Node node : nodes) {
            if (node.getId().equals(id)) {
                return node;
            }
        }
        return null;
    }
}
