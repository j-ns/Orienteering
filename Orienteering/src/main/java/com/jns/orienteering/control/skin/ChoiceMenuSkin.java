/*
 * Copyright 2016, Jens Stroh
 */
package com.jns.orienteering.control.skin;


import static com.jns.orienteering.util.Validations.isNullOrEmpty;

import com.jns.orienteering.control.ChoiceMenu;
import com.jns.orienteering.control.Icon;
import com.jns.orienteering.control.RefocusableTextField;
import com.jns.orienteering.control.ScrollListener;
import com.jns.orienteering.locale.Localization;
import com.jns.orienteering.model.common.Search;
import com.jns.orienteering.util.Calculations;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.When;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

public class ChoiceMenuSkin<T> extends SkinBase<ChoiceMenu<T>> {

    public static final double  SCENE_PADDING      = 8 * 2;

    private static final double LIST_CELL_HEIGHT   = 48;
    private static final double CELL_PADDING       = 16 * 2;
    private static final int    MINIMUM_CELL_COUNT = 3;

    private static final double MENU_UNIT_WIDTH    = 56;
    public static final double  MIN_WIDTH          = MENU_UNIT_WIDTH * 2;

    private static final Font   FONT               = new Font("Roboto", 16);

    protected ListView<T>       lview;
    private ScrollListener      scrollListener;

    private HBox                boxSearch;
    private TextField           txtSearch;
    private Node                iconSearch;
    private Node                iconClear;
    private Label               lblSearchIcon;

    private double              _prefWidth;
    private double              _maxWidth;

    private double              _minHeight;
    private double              _prefHeight;
    private double              _maxHeight;

    protected boolean           dirty              = true;

    private Search<T, String>   search;
    private boolean             searchActive;

    public ChoiceMenuSkin(ChoiceMenu<T> control) {
        super(control);

        if (control.isShowSearchBox()) {
            initSearchBox();
            initSearch();
        }
        initListView(control);

        control.itemsProperty().addListener((InvalidationListener) l -> onItemsChanged());

        if (control.getSelectionModel().isSingleSelection()) {
            control.getSelectionModel().selectedItemProperty().addListener(l -> onSelectedIndexChanged(control.getSelectionModel()
                                                                                                              .getSelectedIndex()));
            selectAndScrollTo(control.getSelectionModel().getSelectedIndex());
        }
    }

    private void initListView(ChoiceMenu<T> control) {
        lview = new ListView<>();
        lview.setCellFactory(control.getCellFactory());
        lview.setFixedCellSize(LIST_CELL_HEIGHT);
        lview.setItems(control.getItems());

        getChildren().add(lview);

        scrollListener = new ScrollListener(lview);
        scrollListener.activate();
    }

    private void initSearchBox() {
        txtSearch = new RefocusableTextField();
        txtSearch.setPromptText(Localization.localize("label.search"));
        txtSearch.textProperty().bindBidirectional(getSkinnable().filterTextProperty());

        iconSearch = Icon.SEARCH.label("1.5em");
        iconSearch.setId("searchIcon");
        iconClear = Icon.CLOSE.label("1.5em");
        iconClear.setOnMouseClicked(e -> txtSearch.clear());

        lblSearchIcon = new Label();
        lblSearchIcon.setMinWidth(Region.USE_PREF_SIZE);
        lblSearchIcon.graphicProperty().bind(new When(txtSearch.textProperty().isEmpty()).then(iconSearch).otherwise(iconClear));
        lblSearchIcon.prefHeightProperty().bind(txtSearch.heightProperty());

        boxSearch = new HBox(txtSearch, lblSearchIcon);
        boxSearch.getStyleClass().add("search-box");
        boxSearch.setAlignment(Pos.BOTTOM_LEFT);
        HBox.setHgrow(txtSearch, Priority.ALWAYS);

        getChildren().add(boxSearch);
    }

    private void initSearch() {
        search = Search.textInstance(getSkinnable().getStringConverter(), result -> Platform.runLater(() -> lview.setItems(result)));

        txtSearch.textProperty().addListener((obsValue, t, t1) ->
        {
            searchActive = !isNullOrEmpty(t1);
            if (searchActive) {
                search.find(t1, getSkinnable().getItems());

            } else {
                search.reset();
                lview.setItems(getSkinnable().getItems());
                selectAndScrollTo(getSkinnable().getSelectionModel().getSelectedIndex());
            }
        });
    }

    protected void onItemsChanged() {
        lview.setItems(getSkinnable().getItems());
        dirty = true;
        getSkinnable().selected();
    }

    private void onSelectedIndexChanged(int idx) {
        if (searchActive) {
            txtSearch.clear();
        } else {
            selectAndScrollTo(idx);
        }
        getSkinnable().selected();
    }

    protected void selectAndScrollTo(int idx) {
        lview.getSelectionModel().select(idx);
        lview.scrollTo(idx);
    }

    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        if (dirty && !searchActive) {
            double offset = 0;

            if (getSkinnable().isShowSearchBox() && _prefHeight > getSkinnable().maxHeight(-1) + LIST_CELL_HEIGHT * 10) {
                if (!getChildren().contains(boxSearch)) {
                    getChildren().add(boxSearch);
                }
                boxSearch.resizeRelocate(x, y, +width, LIST_CELL_HEIGHT);
                offset = LIST_CELL_HEIGHT;

            } else {
                getChildren().remove(boxSearch);
            }
            lview.resizeRelocate(x, y + offset, width, height - offset);
            dirty = false;
        }
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return leftInset + MIN_WIDTH + rightInset;
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (dirty && !searchActive) {
            double maxTextWidth = Calculations.maxTextWidth(getSkinnable().getItems(), getSkinnable().getStringConverter(), FONT);
            double prefWidth = leftInset + maxTextWidth + CELL_PADDING + rightInset;
            double delta = prefWidth % MENU_UNIT_WIDTH;
            prefWidth = prefWidth + (delta == 0 ? 0 : MENU_UNIT_WIDTH - delta);

            _prefWidth = prefWidth;
        }
        return _prefWidth;
    }

    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (dirty && !searchActive) {
            double sceneWidth = getSkinnable().getScene().getWidth();
            _maxWidth = sceneWidth - SCENE_PADDING;
        }
        return _maxWidth;
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (dirty) {
            _minHeight = topInset + getListViewHeightForCells(MINIMUM_CELL_COUNT) + bottomInset;
        }
        return _minHeight;
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (dirty && !searchActive) {
            int countOfItems = getSkinnable().getItems() == null ? MINIMUM_CELL_COUNT : getSkinnable().getItems().size();
            _prefHeight = topInset + getListViewHeightForCells(countOfItems) + bottomInset; // 2 um ScrollBar zu vermeiden
        }
        return _prefHeight;
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (dirty && !searchActive) {
            double sceneHeight = getSkinnable().getScene().getHeight();
            double topOffset = LIST_CELL_HEIGHT;
            double height = sceneHeight - SCENE_PADDING - topOffset;
            int countOfCells = (int) (height / LIST_CELL_HEIGHT);

            _maxHeight = getListViewHeightForCells(countOfCells);
        }
        return _maxHeight;
    }

    private double getListViewHeightForCells(int countOfCells) {
        return lview.getInsets().getTop() + getListCellSize() * countOfCells + lview.getInsets().getBottom();
    }

    protected double getListCellSize() {
        return lview.getFixedCellSize();
    }

}
