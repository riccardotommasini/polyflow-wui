package org.streamreasoning.gsp.views;

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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import de.f0rce.ace.enums.AceTheme;
import graph.seraph.events.PGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.streamreasoning.gsp.config.QueryConfig;
import org.streamreasoning.gsp.config.QueryConfigLoader;
import org.streamreasoning.gsp.data.GraphDataComponent;
import org.streamreasoning.gsp.services.DataComponent;
import org.streamreasoning.gsp.services.SeraphService;
import org.streamreasoning.gsp.views.modular.*;
import org.vaadin.addons.visjs.network.main.Edge;
import org.vaadin.addons.visjs.network.main.Node;
import org.vaadin.addons.visjs.network.options.Interaction;
import org.vaadin.addons.visjs.network.options.Options;
import org.vaadin.addons.visjs.network.options.physics.Physics;
import org.vaadin.addons.visjs.network.options.physics.Repulsion;
import org.vaadin.addons.visjs.network.options.physics.Stabilization;

import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@PageTitle("Seraph")
@Route(value = "/pgsmod", layout = MainLayout.class)
@Uses(Icon.class)
public class PGSModular extends Composite<VerticalLayout> {
    static AtomicInteger eventCounter = new AtomicInteger();
    static boolean paused = true;
    static String inputStream = "http://stream1";
    private final Options.Builder builder;
    private String labels = "Bike;Station";
    QueryConfig config = QueryConfigLoader.load("queries-config.yaml");

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
        this.builder = Options.builder().withWidth("100%").withHeight("100%").withPhysics(physics).withInteraction(Interaction.builder().withMultiselect(true).build());


        InputRow inputRow = new InputRow();
        ProgressiveStreamView streamView = inputRow.getStreamView();
        streamView.reload("testGraph", 35); //todo distinguish capacity of the view from the number of events available per stream.

        HorizontalLayout operationRow = new HorizontalLayout();

        HorizontalLayout nextEventWindow = new HorizontalLayout();

        //Next Event
        ComboBox<String> select = new ComboBox<>();
        select.setLabel("From Stream");
        select.setItems(config.getAllQueryNames());
        select.setValue(config.getDefaultQueryName());

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

        ModularTabSheet.SmartTab[] queryingTabs = queryingTabSheet.initialise(2);

        SmartEditor editor = new SmartEditor(config.getDefaultQuery().getQuery_template());
        RegisteredQueries registeredQueries = new RegisteredQueries();

        select.addValueChangeListener(event -> {
            QueryConfig.QueryDefinition queryByName = config.getQueryByName(event.getValue());
            streamView.reload("testGraph", 35); //todo distinguish capacity of the view from the number of events available per stream.
            if (queryByName != null) {
                editor.setValue(queryByName.getQuery_template());
            } else Notification.show("Selected Stream [" + event.getValue() + "] does not have a corresponding query");
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

        ReactiveVerticalLayout outputRowContainer = new ReactiveVerticalLayout();
        ModularTabSheet outputTabSheet = new ModularTabSheet();
        outputTabSheet.setWidthFull();
        outputTabSheet.setHeightFull();
        outputRowContainer.add(outputTabSheet);


//        Grid<String> registeredQueries = addRegisteredQueries(queryingTabSheet, outputRowContainer);

        outputRowContainer.addMyCustomEventListener((ComponentEventListener<QueryDeletionEvent>) event -> {
            seraphService.unregisterQuery(event.id);
            outputRowContainer.getChildren().filter(c -> event.id.equals(c.getId().get())).findFirst().ifPresent(Component::removeFromParent);
        });


        //Control Row with all buttons
        HorizontalLayout controlRow = new HorizontalLayout();
        HorizontalLayout leftControl = new HorizontalLayout();
        HorizontalLayout rightControl = new HorizontalLayout();
        controlRow.add(leftControl, rightControl);

        Button sendQuery = new Button("Register Query");
        Button nextEventButton = new Button("Next Event");
        Button realTimeButton = new Button("Real-Time");
        Button stopButton = new Button("Pause Computation");

        sendQuery.addClickListener(click -> seraphService.registerNewQuery2(inputStream, snapshotGraphFunction, snapshotGraphSolo, time_varying_table, timePicker, processingTabSheet, editor, outputTabSheet));

        ingestOneEvent(nextEventButton, streamView, nextEventWindow, snapshotGraphFunction, snapshotGraphSolo, outputRowContainer);
        startRuntimeIngestion(realTimeButton, streamView, nextEventWindow);
        stopRuntimeIngestion(stopButton, realTimeButton);

        /// Sizing Components

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
        getContent().setFlexGrow(0.2, inputRow);
        inputRow.setHeight("100px");
        inputRow.setSpacing(false);

        streamView.setHeight("100%");
        streamView.setWidthFull();
        streamView.getStyle().setBorder("dotted");
        streamView.getStyle().set("border-color", "red");
        streamView.getStyle().set("overflow-x", "auto");
        streamView.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        streamView.getStyle().set("margin-left", "20px");
        streamView.getStyle().set("margin-right", "20px");
        streamView.getStyle().set("background-color", "#f0f0f0"); // Use your desired color code

        operationRow.setWidthFull();
        getContent().setFlexGrow(0.4, operationRow);
        operationRow.addClassName(Gap.MEDIUM);
        operationRow.setWidth("100%");
        operationRow.setHeight("40%");

        controlRow.setWidthFull();
        getContent().setFlexGrow(0.1, controlRow);
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
        getContent().setFlexGrow(0.3, outputRowContainer);
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
        queryingTabs[1].add("Registered Queries", registeredQueries);

        queryingTabSheet.addSelectedChangeListener((ComponentEventListener<TabSheet.SelectedChangeEvent>) event -> {
            if (event.getSelectedTab().getLabel().equals("Registered Queries")) {
                if (seraphService != null) {
                    registeredQueries.refresh(seraphService.listQueries().stream().map(q -> {
                        QueryRow queryRow = new QueryRow();
                        queryRow.id = q;
                        queryRow.projectionVar = seraphService.getResultVars(q);
                        queryRow.plan = seraphService.getQueryPlan(q);
                        return queryRow;
                    }).toList());
                }
            }
        });


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


    private Button ingestOneEvent(Button nextEventButton, ProgressiveStreamView streamView, HorizontalLayout nextEventWindow, DataComponent snapshotGraphFunction, DataComponent snapshotGraphSolo, VerticalLayout outeroutputRow) {
        nextEventButton.addClickListener(e -> {

            List<String> seraphQueries = seraphService.listQueries();

            if (seraphQueries.isEmpty()) {
                Notification.show("Register a query first!", 1000, Notification.Position.MIDDLE);
                return;
            }

            PGraph currentEvent = (PGraph) streamView.getCurrentEvent();

            seraphService.sendEvent(currentEvent, inputStream);

//            eventCounter.compareAndSet(10, 0);
//
//            moveEvent(nextEventWindow, streamView, 0, "#f0f0f0", "120px");
//
//            Component pg = seraphService.sendEvent("testGraph", inputStream);
//            loadEvent(nextEventWindow, pg);
//
//            Notification.show("testGraph", 500, Notification.Position.BOTTOM_CENTER);

            //            seraphQueries.forEach(q -> {
            //                HorizontalLayout outputRow = (HorizontalLayout) outeroutputRow.getChildren().filter(c -> q.equals(c.getId().get())).findFirst().get();
            //                if (outputRow.getComponentCount() > 5) {
            //                    outputRow.remove(outputRow.getComponentAt(0));
            //                }
            //
            //            });

            // if (streamView.getComponentCount() > 15) {
            //     streamView.remove(streamView.getComponentAt(0));
            // }

            snapshotGraphFunction.refreshAll();
            snapshotGraphSolo.refreshAll();

        });
        return nextEventButton;
    }

    private Button startRuntimeIngestion(Button realTimeButton, ProgressiveStreamView streamView, HorizontalLayout nextEventWindow) {
        realTimeButton.addClickListener(e -> {
            paused = !paused;
            getUI().ifPresent(ui -> {
                new Thread(() -> {
                    while (!paused) {
                        try {
                            ui.access((Command) () -> {
                                PGraph currentEvent = (PGraph) streamView.getCurrentEvent();
                                seraphService.sendEvent(currentEvent, inputStream);
//                                String s = "100%";
//                                Component pGraph3 = seraphService.sendEvent("testGraph", inputStream);
//                                pGraph3.getStyle().setWidth(s).setHeight(s);
//                                moveEvent(nextEventWindow, streamView, 0, "#f0f0f0", "120px");
//                                loadEvent(nextEventWindow, pGraph3);
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