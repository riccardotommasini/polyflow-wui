package org.streamreasoning.gsp.views;

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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import de.f0rce.ace.AceEditor;
import de.f0rce.ace.enums.AceMode;
import de.f0rce.ace.enums.AceTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.streamreasoning.gsp.data.GraphDataComponent;
import org.streamreasoning.gsp.services.DataComponent;
import org.streamreasoning.gsp.services.SeraphService;
import org.streamreasoning.gsp.views.modular.InputRow;
import org.streamreasoning.gsp.views.modular.ModularTabSheet;
import org.vaadin.addons.visjs.network.main.Edge;
import org.vaadin.addons.visjs.network.main.NetworkDiagram;
import org.vaadin.addons.visjs.network.main.Node;
import org.vaadin.addons.visjs.network.options.Interaction;
import org.vaadin.addons.visjs.network.options.Options;
import org.vaadin.addons.visjs.network.options.edges.ArrowHead;
import org.vaadin.addons.visjs.network.options.edges.Arrows;
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
@Route(value = "/pgsmod", layout = MainLayout.class)
@Uses(Icon.class)
public class PGSModular extends Composite<VerticalLayout> {

    static Random random = new Random();
    static AtomicInteger idCounter = new AtomicInteger();
    static AtomicInteger eventCounter = new AtomicInteger();
    static boolean paused = true;
    static String inputStream = "http://stream1";
    private String labels = "Bike;Station";

    @Autowired
    private SeraphService seraphService;

    public PGSModular() {
        //Read these option from YAML file
        Physics physics = new Physics();
        physics.setEnabled(true);
        Stabilization stabilization = new Stabilization();
        stabilization.setIterations(200);
        physics.setStabilization(stabilization);
        physics.setSolver(Physics.Solver.repulsion);
        Repulsion repulsion = new Repulsion();
        repulsion.setNodeDistance(100);
        physics.setRepulsion(repulsion);
        Options.Builder builder = Options.builder().withWidth("100%").withHeight("100%").withPhysics(physics).withInteraction(Interaction.builder().withMultiselect(true).build());


        InputRow inputRow = new InputRow();
        HorizontalLayout streamView = inputRow.getStreamView();

        HorizontalLayout operationRow = new HorizontalLayout();

        HorizontalLayout nextEventWindow = new HorizontalLayout();

        //Next Event
        ComboBox<String> select = new ComboBox<>();
        select.setLabel("From Stream");
        select.setItems("Bike Sharing", "Cyber Security", "Network Monitoring", "Basic", "New Stream");
        select.setValue("Basic");

        VerticalLayout outerNextEvent = new VerticalLayout();
        outerNextEvent.add(select);
        outerNextEvent.add(nextEventWindow);

        ModularTabSheet processingTabSheet = new ModularTabSheet();

        ModularTabSheet.SmartTab[] processingTabs = processingTabSheet.initialise(3);

        loadEvent(nextEventWindow);

        //Snapshot Graph

        List<Node> nodes = new LinkedList<>();
        List<Edge> edges = new LinkedList<>();

        final var dataProvider = new ListDataProvider<Node>(nodes);
        final var edgeProvider = new ListDataProvider<Edge>(edges);

        final GraphDataComponent snapshotGraphSolo = new GraphDataComponent(builder.build(), dataProvider, edgeProvider);

        // Time Varying Table

        HorizontalLayout time_varying_table = new HorizontalLayout();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.add(new Paragraph("Insert Timestamp"));

        TimePicker timePicker = new TimePicker();
        Grid<?> next_temporal_table = new Grid<>();

        final GraphDataComponent snapshotGraphFunction = new GraphDataComponent(builder.build(), dataProvider, edgeProvider);
        time_varying_table.add(snapshotGraphFunction, verticalLayout, next_temporal_table);

        ModularTabSheet queryingTabSheet = new ModularTabSheet();

        ModularTabSheet.SmartTab[] queryingTabs = queryingTabSheet.initialise(3);

        AceEditor editor = new AceEditor();
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
                default:
                    Notification.show("already broken");
            }
        });

//
//        queryingTabSheet.addSelectedChangeListener((ComponentEventListener<TabSheet.SelectedChangeEvent>) event -> {
//            snapshotGraphSolo.diagamRedraw();
//            snapshotGraphFunction.diagamRedraw();
////            eventGraph.diagamRedraw();
////            eventGraph.diagramFit();
//        });


        //Output Row

        //the result table should be generated based on binding plus the two validity columns

        VerticalLayout outputRowContainer = new VerticalLayout();

        Grid<String> registeredQueries = addRegisteredQueries(queryingTabSheet, outputRowContainer);

        //Control Row with all buttons
        HorizontalLayout controlRow = new HorizontalLayout();
        HorizontalLayout leftControl = new HorizontalLayout();
        HorizontalLayout rightControl = new HorizontalLayout();
        controlRow.add(leftControl, rightControl);

        Button sendQuery = new Button("Register Query");
        Button nextEventButton = new Button("Next Event");
        Button realTimeButton = new Button("Real-Time");
        Button stopButton = new Button("Pause Computation");

        sendQuery.addClickListener(click -> seraphService.registerNewQuery(inputStream, snapshotGraphFunction, snapshotGraphSolo, time_varying_table, timePicker, processingTabSheet, editor, outputRowContainer));

        ingestOneEvent(nextEventButton, streamView, nextEventWindow, snapshotGraphFunction, snapshotGraphSolo, outputRowContainer);
        startRuntimeIngestion(realTimeButton, streamView, nextEventWindow);
        stopRuntimeIngestion(stopButton, realTimeButton);

        /// Sizing Components
        setUpAce(editor);

        outerNextEvent.setWidth("100%");
        outerNextEvent.setHeight("100%");

        processingTabSheet.setHeight("100%");
        processingTabSheet.setWidth("80%");
        queryingTabSheet.setHeight("100%");
        queryingTabSheet.setWidth("60%");

        nextEventWindow.setHeight("90%");
        nextEventWindow.setWidth("90%");
        time_varying_table.setWidth("100%");
        time_varying_table.setHeight("100%");
        timePicker.setLabel("");
        timePicker.setValue(LocalTime.NOON);
        timePicker.setWidth("100%");
        verticalLayout.add(timePicker);
        verticalLayout.setWidth("20%");
        verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        verticalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        next_temporal_table.setId("next_temporal_table");
        next_temporal_table.setWidth("100%");
        next_temporal_table.setHeight("100%");
        next_temporal_table.setPageSize(10);
        next_temporal_table.getStyle().setFontSize("12px");
        editor.setFontSize(20);
        editor.setWidth("100%");
        editor.setHeight("100%");
        editor.setTheme(AceTheme.sqlserver);

        registeredQueries.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        registeredQueries.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        registeredQueries.setWidth("100%");
        registeredQueries.setHeight("100%");
        registeredQueries.setPageSize(10);

        sendQuery.addClassName("special");
        sendQuery.setHeight("90%");
        sendQuery.setWidth("min-content");
        sendQuery.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        nextEventButton.addClassName("special");
        nextEventButton.setHeight("90%");
        nextEventButton.setWidth("min-content");
        nextEventButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        realTimeButton.setWidth("min-content");
        realTimeButton.setHeight("90%");
        realTimeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        stopButton.setWidth("min-content");
        stopButton.setHeight("90%");
        stopButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        getContent().setHeight("100%");
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        inputRow.setWidthFull();
        getContent().setFlexGrow(1.0, inputRow);
        inputRow.setHeight("100px");
        inputRow.setSpacing(false);

        operationRow.setWidthFull();
        getContent().setFlexGrow(1.0, operationRow);
        operationRow.addClassName(Gap.MEDIUM);
        operationRow.setWidth("100%");
        operationRow.setHeight("40%");

        controlRow.setWidthFull();
        getContent().setFlexGrow(1.0, controlRow);
        controlRow.addClassName(Gap.SMALL);
        controlRow.setWidth("100%");
        controlRow.setHeight("70px");

        leftControl.setWidthFull();
        leftControl.addClassName(Gap.SMALL);
        leftControl.setWidth("70%");
        leftControl.setHeight("100%");

        rightControl.setWidthFull();
        rightControl.addClassName(Gap.SMALL);
        rightControl.setWidth("60%");
        rightControl.setHeight("100%");

        outputRowContainer.setWidthFull();
        getContent().setFlexGrow(1.0, outputRowContainer);
        outputRowContainer.addClassName(Gap.MEDIUM);
        outputRowContainer.setWidth("100%");
        outputRowContainer.setHeight("30%");

        //Page composition
        getContent().add(inputRow);
        getContent().add(new Hr());

        getContent().add(operationRow);
        operationRow.add(processingTabSheet);

        processingTabs[0].add("Next Event", outerNextEvent);
        processingTabs[1].add("Time-Varying Table", time_varying_table);
        processingTabs[2].add("Snapshot Graph", snapshotGraphSolo);

        operationRow.add(queryingTabSheet);
        queryingTabs[0].add("Query Editor", editor);
        queryingTabs[1].add("Query Plan", addQueryPlan(queryingTabSheet));
        queryingTabs[2].add("Queries", registeredQueries);

        getContent().add(new Hr());

        getContent().add(controlRow);

        leftControl.add(nextEventButton);
        leftControl.add(realTimeButton);
        leftControl.add(stopButton);
        leftControl.add(sendQuery);

        getContent().add(new Hr());
        getContent().add(outputRowContainer);
    }

    private static Button stopRuntimeIngestion(Button stopButton, Button realTimeButton) {
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

    private static void moveEvent(HorizontalLayout from, HorizontalLayout to, int j, String color, String size) {
        if ((from.getChildren().toList().size() < j + 1)) {
            return;
        }
        Component componentAt = from.getComponentAt(j);
        componentAt.getStyle().set("background-color", color);
        componentAt.getStyle().setWidth(size);
        componentAt.getStyle().setHeight("100%");
        componentAt.getStyle().set("flex-shrink", "0");
        from.remove(componentAt);
        to.addComponentAsFirst(componentAt);
//        componentAt.diagamRedraw();
//        componentAt.diagramFit();
    }

    private static NetworkDiagram addQueryPlan(TabSheet inputRow) {
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

    public static void setUpAce(AceEditor ace) {

        //
//        ArrayList<String> custom = new ArrayList<String>();
//        custom.add("REGISTER");
//        custom.add("QUERY");
//        custom.add("MATCH");
//        custom.add("WHERE");
//        custom.add("STARTING");
//        custom.add("WITH");
//        custom.add("WITHIN");
//        custom.add("AT");
//        custom.add("EMIT");
//        custom.add("SNAPSHOT");
//        custom.add("ON");
//        custom.add("ENTERING");
//        custom.add("EVERY");
//        custom.add("EMIT");
        //
//        AceCustomMode customMode = new AceCustomMode();

//        AceCustomModeRule keywords = new AceCustomModeRule();
//        keywords.setRegex("[a-zA-Z_$][a-zA-Z0-9_$]*\\b");
//        keywords.setKeywordMapper(
//                Map.of(
//                        AceCustomModeTokens.KEYWORD, String.join("|", custom)
//                ),
//                AceCustomModeTokens.IDENTIFIER,
//                true,
//                "|"
//        );


//        ArrayList<String> fs = new ArrayList<String>();
//        fs.add("allShortestPaths");

//        AceCustomModeRule functions = new AceCustomModeRule();
//        functions.setRegex("[a-z][a-zA-Z0-9]*\\b");
//        functions.setKeywordMapper(
//                Map.of(AceCustomModeTokens.VARIABLE, String.join("|", fs)),
//                AceCustomModeTokens.VARIABLE,
//                true,
//                "|"
//        );


//        AceCustomModeRule lineComment = new AceCustomModeRule();
//        lineComment.setRegex("--.*$");
//        lineComment.setToken(AceCustomModeTokens.COMMENT);
        //
//        AceCustomModeRule blockComment = new AceCustomModeRule();
//        blockComment.setStart("/\\*");
//        blockComment.setEnd("\\*/");
//        blockComment.setToken(AceCustomModeTokens.COMMENT);
        //
//        customMode.addState(
//                "start",
//                lineComment,
//                blockComment,
//                functions,
//                keywords
//        );

//        ace.addCustomMode("cypher", customMode);
//        ace.setCustomMode("cypher");
        ace.setMode(AceMode.sql);
    }

    private Grid<String> addRegisteredQueries(TabSheet queryingTab, VerticalLayout outputRow) {

        Grid<String> g = new Grid();
        List<String> items = new ArrayList<>();

        g.setSelectionMode(Grid.SelectionMode.SINGLE);

        ListDataProvider<String> mapDP = new SeraphService.MyDataProvider<>(items);
        g.setDataProvider(mapDP);
//                    g.addColumn(map -> ts).setHeader("Id");
        g.setId("Registered Queries");

        g.addColumn(map -> map).setHeader("QID");
        g.addColumn(map -> seraphService.getResultVars(map)).setHeader("Projections");
        g.addComponentColumn((ValueProvider<String, Component>) seraphQuery -> {
            Button removeQuery = new Button("X");
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


        queryingTab.addSelectedChangeListener((ComponentEventListener<TabSheet.SelectedChangeEvent>) event -> {
            if (event.getSelectedTab().getLabel().equals("Registered Queries")) {
                if (seraphService != null) {
                    items.clear();
                    items.addAll(seraphService.listQueries());
                    mapDP.refreshAll();
                }
            }
        });

        return g;
    }

    private Button ingestOneEvent(Button nextEventButton, HorizontalLayout streamView, HorizontalLayout nextEventWindow, DataComponent snapshotGraphFunction, DataComponent snapshotGraphSolo, VerticalLayout outeroutputRow) {
        nextEventButton.addClickListener(e -> {

            List<String> seraphQueries = seraphService.listQueries();

            if (seraphQueries.isEmpty()) {
                Notification.show("Register a query first!", 1000, Notification.Position.MIDDLE);
                return;
            }

            eventCounter.compareAndSet(10, 0);

            moveEvent(nextEventWindow, streamView, 0, "#f0f0f0", "120px");

            Component pg = seraphService.sendEvent("testGraph", inputStream);
            loadEvent(nextEventWindow, pg);

            Notification.show("testGraph", 500, Notification.Position.BOTTOM_CENTER);

            seraphQueries.forEach(q -> {
                HorizontalLayout outputRow = (HorizontalLayout) outeroutputRow.getChildren().filter(c -> q.equals(c.getId().get())).findFirst().get();
                if (outputRow.getComponentCount() > 5) {
                    outputRow.remove(outputRow.getComponentAt(0));
                }

            });

            // if (streamView.getComponentCount() > 15) {
            //     streamView.remove(streamView.getComponentAt(0));
            // }

            snapshotGraphFunction.refreshAll();
            snapshotGraphSolo.refreshAll();

        });
        return nextEventButton;
    }

    private Button startRuntimeIngestion(Button realTimeButton, HorizontalLayout streamView, HorizontalLayout nextEventWindow) {
        realTimeButton.addClickListener(e -> {
            paused = !paused;
            getUI().ifPresent(ui -> {
                new Thread(() -> {
                    while (!paused) {
                        try {
                            ui.access((Command) () -> {
                                String s = "100%";
                                Component pGraph3 = seraphService.sendEvent("testGraph", inputStream);
                                pGraph3.getStyle().setWidth(s).setHeight(s);
                                moveEvent(nextEventWindow, streamView, 0, "#f0f0f0", "120px");
                                loadEvent(nextEventWindow, pGraph3);
                            });

                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }).start();
                Notification.show("Real Time Processing Started");
            });

            Notification.show("Real Time Processing Coulnd't Start");

        });
        return realTimeButton;
    }

    private Component loadEvent(HorizontalLayout eventView, Component event) {
        eventView.add(event);
//        event.diagramFit();
//        event.diagamRedraw();
        return event;

    }

    private Component loadEvent(HorizontalLayout eventView) {
        Component event = SeraphService.loadEvent("testGraph1.json");
        event.getStyle().setWidth("90%").setHeight("90%");
        return loadEvent(eventView, event);
    }

}