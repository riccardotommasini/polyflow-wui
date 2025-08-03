package org.streamreasoning.gsp.views.modular;

import com.vaadin.componentfactory.Popup;
import com.vaadin.componentfactory.PopupAlignment;
import com.vaadin.componentfactory.PopupPosition;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.ValueProvider;
import org.streamreasoning.gsp.services.SeraphService;
import org.vaadin.addons.visjs.network.main.NetworkDiagram;

import java.util.ArrayList;
import java.util.List;

public class RegisteredQueries extends Grid<QueryRow> {

    List<QueryRow> items;
    ListDataProvider<QueryRow> mapDP;

    public RegisteredQueries() {

        this.items = new ArrayList<>();
        this.mapDP = new SeraphService.MyDataProvider<>(items);
        this.setDataProvider(mapDP);

        this.addColumn(map -> map.id).setHeader("QID");
        this.addColumn(map -> map.projectionVar).setHeader("Projections");

        this.addComponentColumn((ValueProvider<QueryRow, Component>) seraphQuery -> {

            Button viewPlan = new Button("P");
            viewPlan.addClassName("special");
            viewPlan.setHeight("90%");
            viewPlan.setWidth("min-content");
            viewPlan.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ICON);

            NetworkDiagram queryPlan = seraphQuery.plan;
            Popup popup = new Popup();
            popup.setFor("id-of-target-element");
            VerticalLayout popupContent = new VerticalLayout();
            popupContent.add(new H2("Query Plan"));
            popupContent.add(queryPlan);
            popupContent.add(new HorizontalLayout(new Button("Action 1"), new Button("Action 2")));
            popupContent.setWidth("40rem");
            popupContent.setHeight("40rem");
            popupContent.setAlignItems(FlexComponent.Alignment.CENTER);

            popup.add(popupContent);
            popup.setPosition(PopupPosition.END);
            popup.setAlignment(PopupAlignment.CENTER);
            viewPlan.addClickListener((ComponentEventListener<ClickEvent<Button>>) click -> {
                Notification.show("cliccked");
                popup.show();
            });

            //TODO very unsafe assumes that the outer component is there
            ((HasComponents) getParent().get().getParent().get()).add(popup);

            return viewPlan;
        }).setHeader("Plan");


        this.addComponentColumn((ValueProvider<QueryRow, Component>) seraphQuery -> {
            Button removeQuery = new Button("X");
            removeQuery.addClassName("special");
            removeQuery.setHeight("90%");
            removeQuery.setWidth("min-content");
            removeQuery.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

            removeQuery.addClickListener((ComponentEventListener<ClickEvent<Button>>) click -> {
                String id = seraphQuery.id;
                Notification.show(id);
                items.remove(seraphQuery);
                mapDP.refreshAll();
                fireEvent(new QueryDeletionEvent(this, false, id));
            });

            return removeQuery;
        }).setHeader("");
    }

    public void refresh(List<QueryRow> qs) {
        items.clear();
        items.addAll(qs);
        mapDP.refreshAll();
    }

}
