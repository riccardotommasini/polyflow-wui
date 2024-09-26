package org.streamreasoning.gsp.data;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;
import org.streamreasoning.gsp.services.DataComponent;

public class TableDataComponent<T> extends Grid<T> implements DataComponent {

    final ListDataProvider<T> dataProvider;

    public TableDataComponent(ListDataProvider<T> dataProvider) {
        this.dataProvider = dataProvider;
        this.setDataProvider(dataProvider);
    }

    @Override
    public void clear() {
        dataProvider.getItems().clear();
    }

    @Override
    public void add(Object o) {
        try {
            T t = (T) o;
            dataProvider.getItems().add(t);
        } catch (ClassCastException e) {
            return;
        }
    }

    @Override
    public void refreshAll() {
        dataProvider.refreshAll();
    }
}
