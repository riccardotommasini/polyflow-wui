package org.streamreasoning.gsp.views;

import com.vaadin.componentfactory.Popup;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import de.f0rce.ace.AceEditor;
import de.f0rce.ace.enums.AceMode;
import de.f0rce.ace.enums.AceTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.streamreasoning.gsp.data.GraphDataComponent;
import org.streamreasoning.gsp.services.SeraphService;
import org.streamreasoning.gsp.views.rows.ControlRow;
import org.streamreasoning.gsp.views.rows.InputRow;
import org.streamreasoning.gsp.views.rows.QueryRow;
import org.vaadin.addons.visjs.network.main.Edge;
import org.vaadin.addons.visjs.network.main.NetworkDiagram;
import org.vaadin.addons.visjs.network.main.Node;
import org.vaadin.addons.visjs.network.options.HierarchicalLayout;
import org.vaadin.addons.visjs.network.options.Interaction;
import org.vaadin.addons.visjs.network.options.Options;
import org.vaadin.addons.visjs.network.options.edges.ArrowHead;
import org.vaadin.addons.visjs.network.options.edges.Arrows;
import org.vaadin.addons.visjs.network.options.edges.Layout;
import org.vaadin.addons.visjs.network.options.physics.Physics;
import org.vaadin.addons.visjs.network.options.physics.Repulsion;
import org.vaadin.addons.visjs.network.options.physics.Stabilization;
import org.vaadin.addons.visjs.network.util.Shape;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@PageTitle("Seraph")
@Route(value = "/pgs", layout = MainLayout.class)
@Uses(Icon.class)
public class PGS extends Composite<VerticalLayout> {

    static AtomicInteger eventCounter = new AtomicInteger();
    static boolean paused = true;
    static String inputStream = "http://stream1";
    private String labels = "Bike;Station";
    private GraphDataComponent snapshotGraphFunction;
    private GraphDataComponent snapshotGraphSolo;
    private HorizontalLayout streamView;
    private HorizontalLayout nextEventWindow;
    private VerticalLayout outputRowContainer;


    @Autowired
    private SeraphService seraphService;

    public PGS() {

        // Setting up look of StreamView
        initializeStreamView();

        InputRow inputRow = new InputRow(this);



        this.nextEventWindow = new HorizontalLayout();
        nextEventWindow.setHeight("90%");
        nextEventWindow.setWidth("90%");


        // This is tab for Next Event window
        VerticalLayout outerNextEventWindow = new VerticalLayout();
        outerNextEventWindow.setWidth("100%");
        outerNextEventWindow.setHeight("100%");


        // This seems to be the tab sheet
        TabSheet processingTabSheet = new TabSheet();
        processingTabSheet.setWidth("80%");
        processingTabSheet.setHeight("100%");

        //Next Event

        // This is the Dropdown menu to choose what stream you want
        ComboBox<String> select = new ComboBox<>();
        select.setLabel("From Stream");
        select.setItems("Bike Sharing", "Cyber Security", "Network Monitoring", "Basic", "New Stream");
        select.setValue("Basic");


        // This popup window only happens when the "New Stream" option is chosen in the
        // dropdown menu
        Popup popup = new Popup();
        popup.setFor("id-of-target-element");
        VerticalLayout popupContent = new VerticalLayout();

        popupContent.add(new Span("Add you own text to the window."));
        popupContent.add(new HorizontalLayout(new Button("Action 1"), new Button("Action 2")));
        popupContent.add(new Span("Buttons are also customizable"));
        popupContent.setMaxWidth("25rem");
        popupContent.setMaxHeight("20rem");
        popup.add(popupContent);

        // Adding the popup to the inputRow
        inputRow.add(popup);

        outerNextEventWindow.add(select);
        outerNextEventWindow.add(nextEventWindow);

        // Variable never used, out it goes
        //Component eventGraph = loadEvent(nextEventWindow);

        processingTabSheet.add(new Tab("Next Event"), outerNextEventWindow);

        //Snapshot Graph

        List<Node> nodes = new LinkedList<>();
        List<Edge> edges = new LinkedList<>();


        Physics physics = new Physics();
        physics.setEnabled(true);
        Stabilization stabilization = new Stabilization();
        stabilization.setIterations(200);
        physics.setStabilization(stabilization);
        physics.setSolver(Physics.Solver.repulsion);
        Repulsion repulsion = new Repulsion();
        repulsion.setNodeDistance(100);
        physics.setRepulsion(repulsion);


        final var dataProvider = new ListDataProvider<Node>(nodes);
        final var edgeProvider = new ListDataProvider<Edge>(edges);


        // Think this is the internal one
        this.snapshotGraphFunction = new GraphDataComponent(Options.builder().withWidth("100%").withHeight("100%").withInteraction(Interaction.builder().withMultiselect(true).build()).build(), dataProvider, edgeProvider);
        this.snapshotGraphSolo = new GraphDataComponent(Options.builder().withWidth("100%").withHeight("100%").withPhysics(physics).withInteraction(Interaction.builder().withMultiselect(true).build()).build(), dataProvider, edgeProvider);


        // Time Varying Table
        //Container for time varying table and datepicker

        Tab now = new Tab("Time-Varying Table");


        HorizontalLayout tvttab = new HorizontalLayout();
        tvttab.setWidth("100%");
        tvttab.setHeight("100%");
        TimePicker timePicker1 = new TimePicker();
        timePicker1.setLabel("");
        timePicker1.setValue(LocalTime.NOON);
        timePicker1.setWidth("100%");

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.add(new Paragraph("Insert Timestamp"));
        verticalLayout.add(timePicker1);
        verticalLayout.setWidth("20%");

        verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        verticalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        TabSheet queryingTab = new TabSheet();
        queryingTab.addSelectedChangeListener((ComponentEventListener<TabSheet.SelectedChangeEvent>) event -> {
            snapshotGraphSolo.diagamRedraw();
            snapshotGraphFunction.diagamRedraw();
        });

        queryingTab.setWidth("60%");
        queryingTab.setHeight("100%");

        // Next Temporal Table
        //Container for time varying table and datepicker


        Grid<?> nowgrid = new Grid<>();
        nowgrid.setId("nowgrid");
        nowgrid.setWidth("100%");
        nowgrid.setHeight("100%");
        nowgrid.setPageSize(10);
        nowgrid.getStyle().setFontSize("12px");


        processingTabSheet.add(now, tvttab);
        processingTabSheet.add(new Tab("Snapshot Graph"), snapshotGraphSolo);

        Component sdsViz = snapshotGraphFunction;

        tvttab.add(sdsViz, verticalLayout, nowgrid);


        Tab editorTab = new Tab("Query Editor");
        AceEditor editor = new AceEditor();
        editor.setFontSize(20);
        editor.setWidth("100%");
        editor.setHeight("100%");
        editor.setTheme(AceTheme.sqlserver);
        setUpAce(editor);
        editor.setValue("REGISTER QUERY simple_query STARTING AT NOW {\n" + "MATCH (b:Bike)-[r]->(s:Station)\n" + "WITHIN PT10S\n" + "EMIT b.bike_id as source, type(r) as edge, s.station_id as dest\n" + "ON ENTERING\n" + "EVERY PT5S\n" + "}");

        HorizontalLayout trash = new HorizontalLayout();

        select.addValueChangeListener(event -> {
            Notification.show(event.getValue());
            Component ig;
            moveEvent(nextEventWindow, trash, 0, "#f0f0f0", "120px");
            trash.removeAll();
            //TODO here we need to make it load from a folder of use-cases, consider moving the switch case on the service side
            switch (event.getValue()) {
                case "Bike Sharing":
                    labels = "Bike;Station";
                    inputStream = "http://stream1";
                    editor.setValue("REGISTER QUERY student_trick STARTING AT NOW {\n" + "MATCH (:Bike)-[r:rentedAt]->(s:Station),\n" + "q = (b)-[:returnedAt|rentedAt*3..]-(o:Station)\n" + "WITHIN PT1H\n" + "WITH r, s, q, relationships(q) AS rels,\n" + "[n IN nodes(q) WHERE 'Station' IN labels(n) | n.id] AS hs\n" + "WHERE ALL(e IN rels WHERE e.user_id = r.user_id AND e.\n" + "val_time > r.val_time AND e.duration < 20 )\n" + "EMIT r.user_id, s.id, r.val_time, hs\n" + "ON ENTERING EVERY PT5M }");
                    ig = seraphService.sendEvent("testGraph", inputStream);
                    loadEvent(nextEventWindow);
                    break;
                case "Cyber Security":
                    labels = "Router;Switch";
                    inputStream = "http://stream2";
                    editor.setValue("REGISTER QUERY watch_for_suspects STARTING AT NOW {\n" + "MATCH (c:Event)-[:OCCURRED_AT]->(l:Location)\n" + "WITHIN PT15M\n" + "WITH c, point(l) AS crime_scene\n" + "MATCH (crime:Event)<-[:PARTY_TO]-(p:Suspect)-[:NEAR_TO]->(curr:Location)\n" + "WITHIN PT15M\n" + "WITH c, crime, p, curr,\n" + "distance(point(curr), crime_scene) AS distance\n" + "WHERE distance < 3000 AND c.type=crime.type\n" + "EMIT person, curr, c.description\n" + "SNAPSHOT EVERY PT5M " + "}");
                    ig = seraphService.sendEvent("cyberTest", inputStream);
                    loadEvent(nextEventWindow);
                    break;
                case "Network Monitoring":
                    labels = "Event:Person";
                    inputStream = "http://stream3";
                    editor.setValue("" + "REGISTER QUERY anomalous_routes STARTING AT NOW {\n" + "MATCH path = allShortestPaths(\n" + "(rack:Rack)-[:HOLDS|ROUTES|CONNECTS*]-(r:Router:Egress))\n" + "WITHIN PT10M\n" + "WITH rack, avg(length(path)) as 10minAvg, path\n" + "WHERE (10minAvg - 5 / 0.5) >= 3\n" + "EMIT path\n" + "SNAPSHOT EVERY PT1M " + "}" + "");
                    ig = seraphService.sendEvent("cyberTest1", inputStream);
                    loadEvent(nextEventWindow);
                    break;
                case "Basic":
                    labels = "Bike;Station";
                    inputStream = "http://stream1";
                    editor.setValue("REGISTER QUERY <student_trick> STARTING AT NOW {\n" + "MATCH (b:Bike)-[r]->(s:Station)\n" + "WITHIN PT10S\n" + "EMIT b.bike_id as source, type(r) as edge, s.station_id as dest\n" + "ON ENTERING\n" + "EVERY PT5S\n" + "}");
                    ig = seraphService.sendEvent("testGraph", inputStream);
                    loadEvent(nextEventWindow);
                    break;
                case "New Stream":
                    if (popup.isOpened()) {
                        Notification.show("already broken");
                    } else {
                        popup.show();
                    }
            }
        });

        queryingTab.add(editorTab, editor);

        addQueryPlan(queryingTab);

        this.outputRowContainer = new VerticalLayout();

        addRegisteredQueries(queryingTab, outputRowContainer);


        //the result table should be generated based on binding plus the two validity columns

        //Control Row with all buttons
        HorizontalLayout controlRow = new ControlRow(this, tvttab, timePicker1, processingTabSheet, editor, outputRowContainer);

        /// Sizing Components

        getContent().setHeight("100%");
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        inputRow.setWidthFull();
        getContent().setFlexGrow(1.0, inputRow);
        inputRow.addClassName(Gap.MEDIUM);
        inputRow.setWidth("100%");
        inputRow.setHeight("10%");

        // Is query row also a potential one to move?
        HorizontalLayout queryRow = new QueryRow(this);


        queryRow.add(processingTabSheet);
        queryRow.add(queryingTab);

        //getContent().setFlexGrow(1.0, controlRow);

        outputRowContainer.setWidthFull();
        getContent().setFlexGrow(1.0, outputRowContainer);
        outputRowContainer.addClassName(Gap.MEDIUM);
        outputRowContainer.setWidth("100%");
        outputRowContainer.setHeight("30%");

        //Page composition
        getContent().add(inputRow);
        getContent().add(new Hr());
        getContent().add(queryRow);

        // I wanna move these, query show be created else where


        getContent().add(new Hr());
        getContent().add(controlRow);
        getContent().add(new Hr());
        getContent().add(outputRowContainer);
    }

    private static Button stopRuntimeIngestion(Button realTimeButton) {
        Button stopButton = new Button();

        stopButton.addClickListener(e -> {
            if (paused) {
                stopButton.setText("Pause Computation");
            } else {
                realTimeButton.setText("Resume");
            }
            paused = !paused;
            Notification.show("Processing Paused");
        });
        return stopButton;
    }

    public static void moveEvent(HorizontalLayout from, HorizontalLayout to, int j, String color, String size) {
        if ((from.getChildren().toList().size() < j + 1)) {
            return;
        }
        Component componentAt = from.getComponentAt(j);
        componentAt.getStyle().set("background-color", color);
        componentAt.getStyle().setWidth(size);
        componentAt.getStyle().setHeight(size);
        from.remove(componentAt);
        to.addComponentAsFirst(componentAt);
    }

    private static void addQueryPlan(TabSheet inputRow) {
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

        inputRow.add(new Tab("Query Plan"), plan);

    }

    public static void setUpAce(AceEditor ace) {
        ace.setMode(AceMode.sql);
    }

    private void addRegisteredQueries(TabSheet queryingTab, VerticalLayout outputRow) {

        Grid<String> g = new Grid();
        List<String> items = new ArrayList<>();

        g.setSelectionMode(Grid.SelectionMode.SINGLE);

        ListDataProvider<String> mapDP = new SeraphService.MyDataProvider<>(items);
        g.setDataProvider(mapDP);
        g.setId("Registered Queries");

        g.addColumn(map -> map).setHeader("QID");
        g.addColumn(map -> seraphService.getResultVars(map)).setHeader("Projections");
        g.addComponentColumn((ValueProvider<String, Component>) seraphQuery -> {
            Button removeQuery = new Button();
            removeQuery.setText("X");
            removeQuery.addClassName("special");
            removeQuery.setHeight("90%");
            removeQuery.setWidth("min-content");
            removeQuery.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

            Registration r = removeQuery.addClickListener((ComponentEventListener<ClickEvent<Button>>) click -> {
                String id = seraphQuery;
                Notification.show(id);
                seraphService.unregisterQuery(id);
                items.remove(seraphQuery);
                mapDP.refreshAll();
                outputRow.getChildren().filter(c -> id.equals(c.getId().get())).findFirst().ifPresent(Component::removeFromParent);
            });

            return removeQuery;
        }).setHeader("");

        g.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        g.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        g.setWidth("100%");
        g.setHeight("100%");
        g.setPageSize(10);

        Tab queries = new Tab("Queries");
        queryingTab.add(queries, g);

        queryingTab.addSelectedChangeListener((ComponentEventListener<TabSheet.SelectedChangeEvent>) event -> {
            if (event.getSelectedTab().equals(queries)) {
                if (seraphService != null) {
                    items.clear();
                    items.addAll(seraphService.listQueries());
                    mapDP.refreshAll();
                }
            }
        });
    }





    public Component loadEvent(HorizontalLayout eventView, Component event) {
        Physics physics = new Physics();
        physics.setEnabled(true);
        physics.setSolver(Physics.Solver.repulsion);
        Repulsion repulsion = new Repulsion();
        repulsion.setNodeDistance(1000);
        physics.setRepulsion(repulsion);
        eventView.add(event);
        return event;

    }

    private Component loadEvent(HorizontalLayout eventView) {
        Component event = SeraphService.loadEvent("testGraph1.json");
        event.getStyle().setWidth("90%").setHeight("90%");
        return loadEvent(eventView, event);
    }

    public void initializeStreamView(){
        // StreamView customization
        this.streamView = new HorizontalLayout();

        streamView.setHeight("100%");
        streamView.setWidth("100%");
        streamView.getStyle().setBorder("dotted");
        streamView.getStyle().set("border-color", "red");
        streamView.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        streamView.getStyle().set("background-color", "#f0f0f0"); // Use your desired color code

    }




    public SeraphService getSeraphService() {
        return seraphService;
    }
    public String getInputStream(){
        return inputStream;
    }

    public GraphDataComponent getsnapshotgraphfunction() {
        return snapshotGraphFunction;
    }

    public GraphDataComponent getSnapshotGraphSolo(){
        return snapshotGraphSolo;
    }

    public HorizontalLayout getStreamView() {
        return streamView;
    }
    public HorizontalLayout getNextEventWindow(){
        return nextEventWindow;
    }

    public AtomicInteger getEventCounter(){
        return eventCounter;
    }

    public VerticalLayout getOutputRowContainer() {
        return outputRowContainer;
    }

    public boolean getPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

}