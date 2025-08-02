package org.streamreasoning.gsp.views.modular;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;

public class ModularTabSheet extends TabSheet {


    public SmartTab[] initialise(int tabs) {

        SmartTab[] ts = new SmartTab[tabs];

        for (int i = 0; i < tabs; i++) {
            ts[i] = new SmartTab("Tab " + i);
        }

        return ts;
    }

    public SmartTab[] initialise(String[] tabs) {

        SmartTab[] ts = new SmartTab[tabs.length];

        for (int i = 0; i < tabs.length; i++) {
            ts[i] = new SmartTab(tabs[i]);
        }

        return ts;
    }

    public class SmartTab extends Tab {

        public SmartTab(String caption) {
            super(caption);
        }

        public void add(Component components) {
            ModularTabSheet.this.add(this, components);
        }

        public void add(String name, Component components) {
            this.setLabel(name);
            ModularTabSheet.this.add(this, components);
        }

    }

}
