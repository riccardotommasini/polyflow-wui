package org.streamreasoning.gsp.views.rows;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.f0rce.ace.AceEditor;
import org.streamreasoning.gsp.views.PGS;
import org.streamreasoning.gsp.views.buttons.NextEventButton;
import org.streamreasoning.gsp.views.buttons.QueryButton;
import org.streamreasoning.gsp.views.buttons.RealTimeButton;
import org.streamreasoning.gsp.views.buttons.StopButton;


public class ControlRow extends HorizontalLayout {

    private HorizontalLayout leftControl = new HorizontalLayout();
    private HorizontalLayout rightControl = new HorizontalLayout();
    private RealTimeButton realTimeButton;


    public ControlRow(PGS pgs, HorizontalLayout tvttab, TimePicker timePicker1, TabSheet processingTabSheet, AceEditor editor,VerticalLayout outputRowContainer) {



        this.add(leftControl, rightControl);


        // Initializing layout of the controlRow
        this.setWidthFull();
        this.addClassName(LumoUtility.Gap.SMALL);
        this.setWidth("100%");
        this.setHeight("70px");



        // Setting up the layout of the right and left control row
        initializeLeftControlRow(pgs,  tvttab,  timePicker1,  processingTabSheet,  editor, outputRowContainer);
        //initializeRightControlRow();




    }

    public void initializeRightControlRow(){
        //Initialize rightControlRow
        rightControl.setWidthFull();
        rightControl.addClassName(LumoUtility.Gap.SMALL);
        rightControl.setWidth("60%");
        rightControl.setHeight("100%");
    }

    public void initializeLeftControlRow(PGS pgs, HorizontalLayout tvttab, TimePicker timePicker1, TabSheet processingTabSheet, AceEditor editor,VerticalLayout outputRowContainer){
        //Initialize lefControlRow
        leftControl.setWidthFull();
        leftControl.addClassName(LumoUtility.Gap.SMALL);
        leftControl.setWidth("70%");
        leftControl.setHeight("100%");

        // Works with my own button
        Button sendQuery = new QueryButton(pgs);

        // Fuck it needs to be here and I can't outsource this to a place with all values already
        // instantiated
        sendQuery.addClickListener(click -> pgs.getSeraphService().registerNewQuery(pgs.getInputStream(), pgs.getsnapshotgraphfunction(), pgs.getSnapshotGraphSolo(), tvttab, timePicker1, processingTabSheet, editor, outputRowContainer));


        Button nextEventButton = new NextEventButton(pgs);

        // All things to nextEventButton should be fixed now
        //ingestOneEvent(streamView, nextEventWindow, snapshotGraphFunction, snapshotGraphSolo, outputRowContainer);

        this.realTimeButton = new RealTimeButton(pgs);

        // All things to RealTimeButton should be fixed now
        //startRuntimeIngestion(streamView, nextEventWindow);

        Button stopButton = new StopButton(pgs, realTimeButton);


        //stopRuntimeIngestion(realTimeButton);

        leftControl.add(nextEventButton);
        leftControl.add(realTimeButton);
        leftControl.add(stopButton);
        leftControl.add(sendQuery);

    }



}




/*

//import static org.streamreasoning.gsp.views.PGS.paused;

public class ControlRow {


     private HorizontalLayout mainRow = new HorizontalLayout();



    public HorizontalLayout getMainRow() {
        return mainRow;
    }

    public void addButton(HorizontalLayout button) {
        mainRow.add(button);
    }



    public void setMainRow(HorizontalLayout mainRow) {
        this.mainRow = mainRow;
    }

    public void instantiateControlRow(SeraphService seraphService, AtomicInteger eventCounter, String inputStream, HorizontalLayout streamView, HorizontalLayout nextEventWindow, GraphDataComponent snapshotGraphFunction, GraphDataComponent snapshotGraphSolo, VerticalLayout outputRowContainer){

        // Instantiating the
        HorizontalLayout leftControl = new HorizontalLayout();
        addButton(leftControl);
        leftControl.setWidthFull();
        leftControl.addClassName(LumoUtility.Gap.SMALL);
        leftControl.setWidth("70%");
        leftControl.setHeight("100%");






        Button nextEventButton = ingestOneEvent( streamView, nextEventWindow, snapshotGraphFunction, snapshotGraphSolo, outputRowContainer);

        Button realTimeButton = startRuntimeIngestion( streamView, nextEventWindow);

        Button stopButton = stopRuntimeIngestion(realTimeButton);


        leftControl.add(nextEventButton);
        leftControl.add(realTimeButton);
        leftControl.add(stopButton);
        leftControl.add(sendQuery);


    }




    private Button ingestOneEvent( HorizontalLayout streamView, HorizontalLayout nextEventWindow, DataComponent snapshotGraphFunction, DataComponent snapshotGraphSolo, VerticalLayout outeroutputRow) {

        Button nextEventButton = new Button();
        nextEventButton.addClassName("special");
        nextEventButton.setText("Next Event");
        nextEventButton.setHeight("90%");
        nextEventButton.setWidth("min-content");
        nextEventButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

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

            if (streamView.getComponentCount() > 15) {
                streamView.remove(streamView.getComponentAt(0));
            }

            snapshotGraphFunction.refreshAll();
            snapshotGraphSolo.refreshAll();

        });
        return nextEventButton;
    }
    private static void moveEvent(HorizontalLayout from, HorizontalLayout to, int j, String color, String size) {
        if ((from.getChildren().toList().size() < j + 1)) {
            return;
        }
        Component componentAt = from.getComponentAt(j);
        componentAt.getStyle().set("background-color", color);
        componentAt.getStyle().setWidth(size);
        componentAt.getStyle().setHeight(size);
        from.remove(componentAt);
        to.addComponentAsFirst(componentAt);
//        componentAt.diagamRedraw();
//        componentAt.diagramFit();
    }



    // Think this is the one for the graphics, i.e. the display of the structure
    // Need to test with the physics of it
    private Component loadEvent(HorizontalLayout eventView, Component event) {
        Physics physics = new Physics();
        physics.setEnabled(true);
        physics.setSolver(Physics.Solver.repulsion);
        Repulsion repulsion = new Repulsion();
        repulsion.setNodeDistance(1000);
        physics.setRepulsion(repulsion);
        eventView.add(event);
//        event.diagramFit();
//        event.diagamRedraw();
        return event;

    }


    // How if I just make the button myself rather than using his method?
    // See if that helps anything
    private Button startRuntimeIngestion( HorizontalLayout streamView, HorizontalLayout nextEventWindow) {
        Button realTimeButton = new Button();
        realTimeButton.setText("Real-Time");
        realTimeButton.setWidth("min-content");
        realTimeButton.setHeight("90%");
        realTimeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // This button should be vaadin.flow.component.button.Button for it to work
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

    private static Button stopRuntimeIngestion(Button realTimeButton) {
        Button stopButton = new Button();
        stopButton.setText("Pause Computation");
        stopButton.setWidth("min-content");
        stopButton.setHeight("90%");
        stopButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

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




}

 */







