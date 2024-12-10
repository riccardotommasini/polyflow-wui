package org.streamreasoning.gsp.views.buttons;



// This is to try and separate components

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import org.streamreasoning.gsp.services.SeraphService;
import org.streamreasoning.gsp.views.PGS;

// The button should just register a query
public class QueryButton extends Button {


    public QueryButton(PGS pgs) {

        // The properties of the sendQueryButton
        this.setText("Register Query");
        this.addClassName("special");
        this.setHeight("90%");
        this.setWidth("min-content");
        this.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        this.addClickListener(this::registerQuery);

    }

    public void registerQuery(ClickEvent click) {

        //TODO add the register query method here (missing the necessary variables at the moment)

        //seraphService.registerNewQuery(inputStream, snapshotGraphFunction, snapshotGraphSolo, tvttab, timePicker1, processingTabSheet, editor, outputRowContainer));

    }








}
