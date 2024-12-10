package org.streamreasoning.gsp.views.buttons;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.streamreasoning.gsp.views.PGS;

import java.util.List;

import static org.streamreasoning.gsp.views.PGS.moveEvent;

public class NextEventButton extends Button {


    public NextEventButton(PGS pgs) {
        this.addClassName("special");
        this.setText("Next Event");
        this.setHeight("90%");
        this.setWidth("min-content");
        this.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        this.addClickListener(e-> ingestOneEvent(e,pgs));
    }

    public void ingestOneEvent(ClickEvent e, PGS pgs) {

        //TODO add the clickEvent here so as to keep it within the class

        List<String> seraphQueries = pgs.getSeraphService().listQueries();

        if (seraphQueries.isEmpty()) {
            Notification.show("Register a query first!", 1000, Notification.Position.MIDDLE);
            return;
        }

        pgs.getEventCounter().compareAndSet(10, 0);

        moveEvent(pgs.getNextEventWindow(), pgs.getStreamView(), 0, "#f0f0f0", "120px");

        Component pg = pgs.getSeraphService().sendEvent("testGraph", pgs.getInputStream());
        pgs.loadEvent(pgs.getNextEventWindow(), pg);

        Notification.show("testGraph", 500, Notification.Position.BOTTOM_CENTER);

        seraphQueries.forEach(q -> {
            HorizontalLayout outputRow = (HorizontalLayout) pgs.getOutputRowContainer().getChildren().filter(c -> q.equals(c.getId().get())).findFirst().get();
            if (outputRow.getComponentCount() > 5) {
                outputRow.remove(outputRow.getComponentAt(0));
            }

        });

        if (pgs.getStreamView().getComponentCount() > 15) {
            pgs.getStreamView().remove(pgs.getStreamView().getComponentAt(0));
        }

        pgs.getsnapshotgraphfunction().refreshAll();
        pgs.getSnapshotGraphSolo().refreshAll();

    }
}
