package org.streamreasoning.gsp.data;

import com.vaadin.componentfactory.Popup;
import com.vaadin.componentfactory.PopupAlignment;
import com.vaadin.componentfactory.PopupPosition;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import graph.seraph.events.PGraph;
import org.streamreasoning.gsp.services.SeraphService;
import org.vaadin.addons.visjs.network.main.NetworkDiagram;
import org.vaadin.addons.visjs.network.options.Options;

public class InputGraph extends NetworkDiagram {
    public final Long timestamp;
    public final Object event;

    public InputGraph(Options options, Long timestamp, Object e) {
        super(options);
        this.timestamp = timestamp;
        this.event = e;
    }

    public Component popup() {
        Popup popup = new Popup();
        VerticalLayout popupContent = new VerticalLayout();
        popupContent.add(new H2("" + timestamp));
        popupContent.add(SeraphService.loadEvent((PGraph) event)); //need to come up with something smarter.
        popupContent.add(this);
        popupContent.setWidth("300px");
        popupContent.setHeight("300px");
        popupContent.setAlignItems(FlexComponent.Alignment.CENTER);

        popup.add(popupContent);
        popup.setPosition(PopupPosition.END);
        popup.setAlignment(PopupAlignment.CENTER);
        this.addSelectListener(clickEvent -> {
            popup.show();
            this.diagamRedraw();
            this.diagramFit();
        });
        return popup;
    }

}
