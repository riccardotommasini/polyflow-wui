package org.streamreasoning.gsp.views;



import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.Route;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;


@PageTitle("Vacation")
@Route(value = "/Fun", layout = MainLayout.class)
@Uses(Icon.class)
public class Fun extends Div {

    public Fun() {

        HorizontalLayout one = new HorizontalLayout();
        HorizontalLayout two = new HorizontalLayout();
        HorizontalLayout three = new HorizontalLayout();
        HorizontalLayout four = new HorizontalLayout();
        one.setHeight("100%");
        one.setWidth("100%");
        // Used to make a href
       // Anchor link = new Anchor();



        Text text = new Text("Wanna go to beautiful Geneva?? Well, look no further!");

        one.add(text);
        Image img = new Image();
        img.setSrc("https://switzerland-tour.com/storage/media/Geneva/Geneva-in-Switzerland.jpg");
        img.setWidth("50%");
        img.setHeight("50%");
        two.add(img);

        // Keeps crashing because of the following line
        //Desktop desk = Desktop.getDesktop();

        Random random = new Random();
        Button button = new Button("Click me for plan ");
        String url = "https://da.airbnb.com/rooms/1247343125656734144?adults=1&search_mode=regular_search&check_in=2024-10-19&check_out=2024-10-20&source_impression_id=p3_1729246315_P3tGEBeRyyneOInI&previous_page_section_name=1000&federated_search_id=965a3dc6-787b-4aad-853c-28aafba0afd8";
        button.addClickListener(event -> {

            int counter = random.nextInt(4);
            switch (counter) {
                case 0:
                    button.setText("Contact your travelling agency.");
                    break;
                case 1:
                    button.setText("Steal someone's plans and make 'em your own.");
                    break;
                case 2:
                    button.setText("Live in the LHC, most expensive place to live in Geneva.");
                    break;
                case 3:
                    button.setText("idk, why you asking me.");
                    break;

            }




        });


        four.add(button);

        add(one, two,three,four);

    }


}
