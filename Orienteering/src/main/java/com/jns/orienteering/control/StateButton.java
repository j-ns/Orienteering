package com.jns.orienteering.control;

import java.util.Optional;

import com.jns.orienteering.util.Trigger;

import javafx.scene.Node;
import javafx.scene.control.ToggleButton;

public class StateButton<T> extends ToggleButton {

    private Node                     unselectedIcon;
    private Node                     selectedIcon;

    private Optional<SelectState<T>> selectState;

    public StateButton(Node unselectedIcon, Node selectedIcon) {
        this(unselectedIcon, selectedIcon, null);
    }

    public StateButton(Node unselectedIcon, Node selectedIcon, SelectState<T> selectState) {
        super();
        this.unselectedIcon = unselectedIcon;
        this.selectedIcon = selectedIcon;
        this.selectState = Optional.ofNullable(selectState);
        this.selectState.ifPresent(st -> st.setSelected(false));
        setGraphic(unselectedIcon);
    }

    public SelectState<T> getSelectState() {
        return selectState.orElse(null);
    }

    public void setSelectState(SelectState<T> selectState) {
        this.selectState = Optional.ofNullable(selectState);
        this.selectState.ifPresent(st -> st.setSelected(isSelected()));
        setGraphic(isSelected() ? selectedIcon : unselectedIcon);
    }

    public void setOnAction(Trigger onAction) {
        selectedProperty().addListener((obsValue, b, b1) ->
        {
            setGraphic(b1 ? selectedIcon : unselectedIcon);
            selectState.ifPresent(state -> state.setSelected(b1));
            onAction.start();
        });
    }

    public void setOnAction(Trigger onSelected, Trigger onUnselected) {
        selectedProperty().addListener((obsValue, b, b1) ->
        {
            selectState.ifPresent(state -> state.setSelected(b1));
            if (b1) {
                setGraphic(selectedIcon);
                onSelected.start();
            } else {
                setGraphic(unselectedIcon);
                onUnselected.start();
            }
        });
    }

}
