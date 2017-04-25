package com.jns.orienteering.control.cell;

import java.util.function.Function;

import com.jns.orienteering.control.ChoiceMenu;
import com.jns.orienteering.control.SelectionModelBase;

import javafx.scene.control.ListCell;
import javafx.scene.input.MouseEvent;

public class ChoiceCell<T> extends ListCell<T> {

    private SelectionModelBase<T> selectionModel;
    private Function<T, String>   stringConverter;

    public ChoiceCell(ChoiceMenu<T> choiceMenu) {
        selectionModel = choiceMenu.getSelectionModel();
        stringConverter = choiceMenu.getStringConverter();

        addEventHandler(MouseEvent.MOUSE_CLICKED, e -> selectItem());
    }

    private void selectItem() {
        T item = getItem();
        T previousItem = selectionModel.getSelectedItem();

        if (item != null && previousItem == item) {
            lviewSelectionModel().clearSelection();
            selectionModel.clearSelection();
            return;
        }

        selectionModel.select(item);
    }

    private javafx.scene.control.MultipleSelectionModel<T> lviewSelectionModel() {
        return getListView().getSelectionModel();
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null && !empty) {
            setText(stringConverter.apply(item));
        } else {
            setText(null);
        }
    }

}
