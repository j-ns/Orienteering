package com.jns.orienteering.control;

public class SelectState<T> {

    private T unselected;
    private T selected;

    private T state;

    public SelectState(T unselected, T selected) {
        this.unselected = unselected;
        this.selected = selected;
    }

    public T get() {
        return state;
    }

    public void setSelected(boolean selected) {
        state = selected ? this.selected : this.unselected;
    }

    public T getSelected() {
        return selected;
    }

    public T getUnselected() {
        return unselected;
    }

}