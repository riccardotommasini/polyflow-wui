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

        // It was in the legacy code to have a right control row
        // Removed it, but made the code to re-create it if necessary
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
        Button sendQuery = new QueryButton(pgs, tvttab, timePicker1, processingTabSheet, editor, outputRowContainer);

        // Fuck it needs to be here and I can't outsource this to a place with all values already
        // instantiated
        //sendQuery.addClickListener(click -> pgs.getSeraphService().registerNewQuery(pgs.getInputStream(), pgs.getsnapshotgraphfunction(), pgs.getSnapshotGraphSolo(), tvttab, timePicker1, processingTabSheet, editor, outputRowContainer));


        Button nextEventButton = new NextEventButton(pgs);

        this.realTimeButton = new RealTimeButton(pgs);

        Button stopButton = new StopButton(pgs, realTimeButton);


        //stopRuntimeIngestion(realTimeButton);

        leftControl.add(nextEventButton);
        leftControl.add(realTimeButton);
        leftControl.add(stopButton);
        leftControl.add(sendQuery);

    }



}







