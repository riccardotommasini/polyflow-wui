package org.streamreasoning.gsp.views.graphics;

import com.vaadin.flow.data.provider.ListDataProvider;
import org.vaadin.addons.visjs.network.main.Edge;
import org.vaadin.addons.visjs.network.main.NetworkDiagram;
import org.vaadin.addons.visjs.network.main.Node;
import org.vaadin.addons.visjs.network.options.Interaction;
import org.vaadin.addons.visjs.network.options.Options;
import org.vaadin.addons.visjs.network.options.edges.Layout;

import java.util.LinkedList;
import java.util.List;



// This is not used for anything functional it's just some pretty images
public class Icon extends NetworkDiagram {


    public Icon(){

        // Needs an instantiation due to its inheritance from NetworkDiagram
        super(Options.builder().withWidth("50px").withHeight("100px").withLayout(new Layout()).withInteraction(Interaction.builder().withMultiselect(true).build()).build());


        final var edgeProvider1 = new ListDataProvider<Edge>(createEdges());
        final var dataProvider1 = new ListDataProvider<Node>(createNodes());
        this.setEdgesDataProvider(edgeProvider1);
        this.setNodesDataProvider(dataProvider1);

        // Isn't really documentation for this call, but it was in legacy code
        // so continuing to call it
        this.diagramFit();


    }


    // Creating a list with to edge points
    // basically just to points used for graphic
    public List<Edge> createEdges(){

        // Creating the two points
        List<Node> placehodlerNodes = createNodes();

        Edge ee = new Edge(placehodlerNodes.get(0), placehodlerNodes.get(1));
        ee.setColor("black");

        List<Edge> placehodlerEdges = new LinkedList<>();
        placehodlerEdges.add(ee);

        return placehodlerEdges;

    }

    public List<Node> createNodes(){
        List<Node> placehodlerNodes = new LinkedList<>();
        Node n1 = new Node("A");
        n1.setColor("#f0f0f0");
        Node n2 = new Node("B");
        n2.setColor("#f0f0f0");

        placehodlerNodes.add(n1);
        placehodlerNodes.add(n2);
        return placehodlerNodes;

    }




}
