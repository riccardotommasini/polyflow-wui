package org.streamreasoning.gsp.views.modular;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

public class QueryDeletionEvent extends ComponentEvent<Component> {

    public String id;

    public QueryDeletionEvent(Component registeredQueries, boolean b, String id) {
        super(registeredQueries, b);
        this.id = id;
    }
}
