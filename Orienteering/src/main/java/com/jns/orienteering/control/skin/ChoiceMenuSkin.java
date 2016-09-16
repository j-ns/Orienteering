/**
 *
 *  Copyright (c) 2016, Jens Stroh
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL JENS STROH BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jns.orienteering.control.skin;

import com.jns.orienteering.control.ChoiceMenu;
import com.jns.orienteering.util.Calculations;

import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SkinBase;
import javafx.scene.text.Font;

public class ChoiceMenuSkin<T> extends SkinBase<ChoiceMenu<T>> {

    public static final double  WINDOW_PADDING     = 8 * 2;

    private static final int    MINIMUM_CELL_COUNT = 3;
    private static final double LIST_CELL_HEIGHT   = 48;
    private static final double CELL_PADDING       = 16 * 2;

    private static final double MENU_UNIT_WIDTH    = 56;
    public static final double  MIN_WIDTH          = MENU_UNIT_WIDTH * 1.5;

    private static final Font   FONT               = new Font("Roboto", 16);

    protected ListView<T>       lview;

    private double              prefWidthCache;
    private double              maxWidthCache;

    private double              minHeightCache;
    private double              prefHeightCache;
    private double              maxHeightCache;

    protected boolean           dirty              = true;

    public ChoiceMenuSkin(ChoiceMenu<T> control) {
        super(control);
        initListView(control);

        control.itemsProperty().addListener((ov, t, t1) -> onItemsChanged());
    }

    private void initListView(ChoiceMenu<T> control) {
        lview = new ListView<>();
        lview.setCellFactory(control.getCellFactory());
        lview.setFixedCellSize(LIST_CELL_HEIGHT);
        lview.setItems(control.getItems());
        getChildren().add(lview);
    }

    protected void onItemsChanged() {
        if (!getSkinnable().isEmpty()) {
            dirty = true;
        }
        lview.setItems(getSkinnable().getItems());
    }

    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        if (!getSkinnable().isVisible() || !dirty) {
            return;
        }
        super.layoutChildren(x, y, width, height);

        dirty = false;
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return MIN_WIDTH;
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (dirty) {
            ObservableList<T> items = getSkinnable().getItems();
            double textWidth = Calculations.greatestTextWidth(items, getSkinnable().getStringConverter(), FONT);
            double prefWidth = leftInset + (textWidth + CELL_PADDING) + rightInset;

            int countOfItems = items == null ? 0 : items.size();
            prefWidth += calculateScrollBarWidth(countOfItems);

            prefWidthCache = prefWidth;
        }
        return prefWidthCache;
    }

    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (dirty) {
            Scene scene = getParentScene();
            if (scene == null) {
                return 0;
            }
            maxWidthCache = scene.getWidth() - WINDOW_PADDING;
        }

        return maxWidthCache;
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (dirty) {
            minHeightCache = topInset + getListViewHeightForCells(MINIMUM_CELL_COUNT) + bottomInset;
        }
        return minHeightCache;
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (dirty) {
            int countOfItems = getSkinnable().getItems().size();
            prefHeightCache = topInset + getListViewHeightForCells(countOfItems) + bottomInset;
        }

        return prefHeightCache;
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (dirty) {
            Scene scene = getParentScene();
            if (scene == null) {
                return 0;
            }
            maxHeightCache = scene.getHeight() - WINDOW_PADDING;
        }
        return maxHeightCache;
    }

    private Scene getParentScene() {
        return getSkinnable().getParentNode().getScene();
    }

    private double getListViewHeightForCells(int countOfCells) {
        return lview.getInsets().getTop() + getListCellSize() * countOfCells + lview.getInsets().getBottom();
    }

    protected double getListCellSize() {
        return lview.getFixedCellSize();
    }

    private double calculateScrollBarWidth(int countOfItems) {
        double listViewHeight = getListViewHeightForCells(countOfItems);

        if (listViewHeight > getSkinnable().maxHeight(-1)) {
            return getVerticalScrollbarWidth();
        }
        return 0;
    }

    private double getVerticalScrollbarWidth() {
        for (Node n : lview.lookupAll(".scroll-bar")) {
            if (n instanceof ScrollBar) {
                ScrollBar bar = (ScrollBar) n;
                if (bar.getOrientation().equals(Orientation.VERTICAL)) {
                    return bar.prefWidth(-1);
                }
            }
        }
        return 0;
    }

}
