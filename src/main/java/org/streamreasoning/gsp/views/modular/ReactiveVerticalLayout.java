package org.streamreasoning.gsp.views.modular;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;

public class ReactiveVerticalLayout extends VerticalLayout {

    public Registration addMyCustomEventListener(ComponentEventListener<QueryDeletionEvent> listener) {
        return addListener(QueryDeletionEvent.class, listener);
    }

}
