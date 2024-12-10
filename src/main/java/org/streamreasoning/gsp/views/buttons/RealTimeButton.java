package org.streamreasoning.gsp.views.buttons;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.Command;
import org.streamreasoning.gsp.views.PGS;

public class RealTimeButton extends Button {
    public RealTimeButton(PGS pgs) {
        this.setText("Real-Time");
        this.setWidth("min-content");
        this.setHeight("90%");
        this.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        this.addClickListener(e-> startRuntimeIngestion(e,pgs));
    }

    public void startRuntimeIngestion(ClickEvent e, PGS pgs){
        //Button realTimeButton = new RealTimeButton();

        //paused = !paused;
        pgs.setPaused(!pgs.getPaused());

        getUI().ifPresent(ui -> {
            new Thread(() -> {
                while (!pgs.getPaused()) {
                    try {
                        ui.access((Command) () -> {
                            String s = "100%";
                            Component pGraph3 = pgs.getSeraphService().sendEvent("testGraph", pgs.getInputStream());
                            pGraph3.getStyle().setWidth(s).setHeight(s);
                            pgs.moveEvent(pgs.getNextEventWindow(), pgs.getStreamView(), 0, "#f0f0f0", "120px");
                            pgs.loadEvent(pgs.getNextEventWindow(), pGraph3);
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







    }

}
