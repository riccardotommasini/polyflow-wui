package org.streamreasoning.gsp.views.rows;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.streamreasoning.gsp.views.PGS;
import org.streamreasoning.gsp.views.graphics.Icon;

public class InputRow extends HorizontalLayout {


    public InputRow(PGS pgs) {

        // Creating the first Icon for the left side
        Icon icon1 = new Icon();

        // Adding the icon to the row
        this.add(icon1);

        // Adding the streamView in the middle of the row
        this.add(pgs.getStreamView());

        // Creating the icon for the right side of the row
        Icon icon2 = new Icon();

        // Adding the icon to the row
        this.add(icon2);




    }





}
