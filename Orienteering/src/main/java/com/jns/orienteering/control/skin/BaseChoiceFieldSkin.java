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

import com.gluonhq.charm.glisten.layout.layer.PopupView;
import com.jns.orienteering.control.BaseChoiceField;
import com.jns.orienteering.control.ChoiceMenu;
import com.jns.orienteering.control.FloatingTextField;
import com.jns.orienteering.control.SelectionModelBase;
import com.jns.orienteering.util.Icon;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class BaseChoiceFieldSkin<T, C extends BaseChoiceField<T>> extends SkinBase<C> {

    protected Node                displayNode;
    private TextField             textField;

    protected StackPane           pneOpenButton;
    private Label                 openButton;
    protected ChoiceMenu<T>       choiceMenu;
    private PopupView             popup;

    private SelectionModelBase<T> selectionModel;

    public BaseChoiceFieldSkin(C control) {
        super(control);
        initialize();

        displayNode.disableProperty().bind(control.disableProperty());

        choiceMenu.minWidthProperty().bind(Bindings.max(ChoiceMenuSkin.MIN_WIDTH, control.widthProperty()));
        choiceMenu.itemsProperty().bindBidirectional(control.itemsProperty());

        selectionModel = control.getSelectionModel();
        if (selectionModel.isSingleSelection()) {
            selectionModel.selectedItemProperty().addListener(selectedItemListener());
            updateDisplay(selectionModel.getSelectedItem());
        }

        control.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (!control.isDisabled()) {
                choiceMenu.show();
            }
        });

        choiceMenu.addEventHandler(ChoiceMenu.ON_SHOWING, e -> showPopup());
        choiceMenu.addEventHandler(ChoiceMenu.ON_HIDING, e -> popup.hide());
    }

    private void initialize() {
        displayNode = getSkinnable().getDisplayNode();
        getChildren().add(displayNode);

        if (getSkinnable().isShowOpenButton()) {
            initOpenButton();
        }
        choiceMenu = getSkinnable().getChoiceMenu();

        popup = new PopupView(getSkinnable(), choiceMenu);
    }

    private void initOpenButton() {
        openButton = getSkinnable().getOpenButton();
        if (openButton == null) {
            openButton = Icon.TRIANGLE_DOWN.label();
        }
        pneOpenButton = new StackPane(openButton);
        getChildren().add(pneOpenButton);
    }

    private void showPopup() {
        if (getSkinnable().isEmpty()) {
            getSkinnable().showMissingDataMessage();
            return;
        }
        popup.show();
        popup.requestFocus();
    }

    private ChangeListener<T> selectedItemListener() {
        return (ov, t, t1) -> {
            updateDisplay(t1);
            choiceMenu.hide();
        };
    }

    private void updateDisplay(T item) {
        getSkinnable().updateDisplay(item);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        if (!getSkinnable().isShowOpenButton()) {
            displayNode.resize(w, h);
            displayNode.relocate(x, y);

        } else {
            double buttonWidth = pneOpenButton.prefWidth(-1);

            displayNode.resize(w - buttonWidth, h);
            displayNode.relocate(x, y);

            double textFieldHeight = getTextField() == displayNode ? h : getTextField().prefHeight(-1);
            double yOffset = h - textFieldHeight;

            pneOpenButton.resize(buttonWidth, textFieldHeight);
            pneOpenButton.relocate(x + w - buttonWidth, yOffset);
        }
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return leftInset + rightInset;
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        double displayNodeWidth = displayNode.prefWidth(height);
        double openButtonWidth = !getSkinnable().isShowOpenButton() ? 0 : pneOpenButton.prefWidth(-1);
        return leftInset + displayNodeWidth + openButtonWidth + rightInset;
    }

    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().prefWidth(-1);
    }

    private TextField getTextField() {
        if (textField == null) {
            if (displayNode instanceof FloatingTextField) {
                textField = ((FloatingTextField) displayNode).getTextField();
            } else if (displayNode instanceof TextField) {
                textField = (TextField) displayNode;
            }
        }
        return textField;
    }
}
