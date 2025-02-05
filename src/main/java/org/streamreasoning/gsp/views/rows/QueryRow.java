package org.streamreasoning.gsp.views.rows;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.streamreasoning.gsp.views.PGS;

public class QueryRow extends HorizontalLayout {

    public QueryRow(PGS pgs) {

        this.setWidthFull();
        pgs.getContent().setFlexGrow(1, this);
        this.addClassName(LumoUtility.Gap.MEDIUM);
        this.setWidth("100%");
        this.setHeight("40%");






    }



}
