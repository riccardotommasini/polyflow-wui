package org.streamreasoning.gsp.views.buttons;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import org.streamreasoning.gsp.views.PGS;

public class StopButton extends Button {
    public StopButton(PGS pgs, RealTimeButton realTimeButton) {
        this.setText("Pause Computation");
        this.setWidth("min-content");
        this.setHeight("90%");
        this.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        this.addClickListener(e->stopRuntimeIngestion(e, pgs, realTimeButton));

    }

    public void stopRuntimeIngestion(ClickEvent event, PGS pgs, RealTimeButton realTimeButton) {
        if (pgs.getPaused()) {
            this.setText("Pause Computation");
        } else {
            realTimeButton.setText("Resume");
        }
        pgs.setPaused(!pgs.getPaused());
        Notification.show("Processing Paused");

    }
}
