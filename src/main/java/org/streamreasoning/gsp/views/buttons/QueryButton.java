package org.streamreasoning.gsp.views.buttons;



// This is to try and separate components

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.timepicker.TimePicker;
import de.f0rce.ace.AceEditor;
import org.streamreasoning.gsp.services.SeraphService;
import org.streamreasoning.gsp.views.PGS;

// The button should just register a query
public class QueryButton extends Button {


    public QueryButton(PGS pgs, HorizontalLayout tvttab, TimePicker timePicker1, TabSheet processingTabSheet, AceEditor editor, VerticalLayout outputRowContainer) {

        // The properties of the sendQueryButton
        this.setText("Register Query");
        this.addClassName("special");
        this.setHeight("90%");
        this.setWidth("min-content");
        this.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        this.addClickListener(e-> registerQuery(e,pgs, tvttab,  timePicker1,  processingTabSheet,  editor, outputRowContainer ));

    }

    public void registerQuery(ClickEvent click,PGS pgs, HorizontalLayout tvttab, TimePicker timePicker1, TabSheet processingTabSheet, AceEditor editor,VerticalLayout outputRowContainer) {

        pgs.getSeraphService().registerNewQuery(pgs.getInputStream(), pgs.getsnapshotgraphfunction(), pgs.getSnapshotGraphSolo(), tvttab, timePicker1, processingTabSheet, editor, outputRowContainer);

    }








}
