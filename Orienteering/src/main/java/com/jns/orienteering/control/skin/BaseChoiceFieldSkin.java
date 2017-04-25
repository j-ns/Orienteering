/*
 *
 *  Copyright 2016 - 2017, Jens Stroh
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.animation.NoTransition;
import com.gluonhq.charm.glisten.layout.layer.PopupView;
import com.jns.orienteering.control.BaseChoiceField;
import com.jns.orienteering.control.ChoiceMenu;
import com.jns.orienteering.control.FloatingTextField;
import com.jns.orienteering.control.Icon;
import com.jns.orienteering.control.SelectionModelBase;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class BaseChoiceFieldSkin<T, C extends BaseChoiceField<T>> extends SkinBase<C> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseChoiceFieldSkin.class);

    private Node                displayNode;
    private TextField           textField;

    private StackPane           pneOpenButton;
    private PopupView           popup;
    private ChoiceMenu<T>       choiceMenu;

    public BaseChoiceFieldSkin(C control) {
        super(control);

        displayNode = control.getDisplayNode();
        displayNode.disableProperty().bind(control.disableProperty());
        getChildren().add(displayNode);

        textField = getTextField();

        if (control.isShowOpenButton()) {
            initOpenButton();
        }

        initChoiceMenu(control);
        initSelectionModel();
    }

    private void initOpenButton() {
        Label openButton = getSkinnable().getOpenButton();

        if (openButton == null) {
            openButton = Icon.TRIANGLE_DOWN.label("14");
        }

        if (displayNode instanceof FloatingTextField) {
            ((FloatingTextField) displayNode).setGraphic(openButton);

        } else {
            pneOpenButton = new StackPane(openButton);
            getChildren().add(pneOpenButton);
        }
    }

    private void initChoiceMenu(C control) {
        choiceMenu = control.getChoiceMenu();
        choiceMenu.itemsProperty().bindBidirectional(control.itemsProperty());

        choiceMenu.addEventHandler(ChoiceMenu.ON_SHOWING, e -> showPopup());
        choiceMenu.addEventHandler(ChoiceMenu.ON_HIDING, e -> hidePopup());

        control.addEventHandler(MouseEvent.MOUSE_CLICKED, e ->
            {
                if (!control.isDisabled()) {
                    choiceMenu.show();
                }
            });
    }

    private void initSelectionModel() {
        SelectionModelBase<T> selectionModel = getSkinnable().getSelectionModel();

        if (selectionModel.isSingleSelection()) {
            selectionModel.selectedItemProperty().addListener((obs, t, t1) -> updateDisplay(t1));
            updateDisplay(selectionModel.getSelectedItem());
        }
    }

    private boolean                              pauseBackButtonFilter;

    private final EventHandler<? super KeyEvent> backButtonFilter     = evt ->
                                                                          {
                                                                              if (!pauseBackButtonFilter && popup.isShowing() && KeyCode.ESCAPE
                                                                                                                                               .equals(evt
                                                                                                                                                          .getCode())) {
                                                                                  evt.consume();
                                                                                  choiceMenu.hide();
                                                                              }
                                                                          };

    private final ChangeListener<Boolean>        popupShowingListener = (obsValue1, b2, b1) ->
                                                                          {
                                                                              if (b1) {
                                                                                  pauseBackButtonFilter = false;

                                                                              } else {
                                                                                  pauseBackButtonFilter = true;
                                                                                  choiceMenu.hide();
                                                                              }
                                                                          };

    private void createPopup() {
        popup = new PopupView(getSkinnable().getPopupOwnerNode(), choiceMenu);
        popup.setShowTransitionFactory(e -> new NoTransition());
        popup.addEventFilter(KeyEvent.KEY_RELEASED, backButtonFilter);
        popup.showingProperty().addListener(popupShowingListener);
    }

    private void showPopup() {
        if (!getSkinnable().isEmpty()) {
            if (popup == null) {
                createPopup();
            }
            popup.show();
            popup.requestFocus();

        } else {
            getSkinnable().showMissingDataMessage();
        }
    }

    private void hidePopup() {
        popup.hide();
    }

    private void updateDisplay(T item) {
        LOGGER.debug("selectedItem: {}", item);
        getSkinnable().updateDisplay(item);
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

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        if (!getSkinnable().isShowOpenButton() || getSkinnable().getDisplayNode() instanceof FloatingTextField) {
            displayNode.resize(w, h);
            displayNode.relocate(x, y);

        } else {
            double textFieldHeight = textField == displayNode ? h : snapSize(textField.prefHeight(-1));
            double yOffset = h - textFieldHeight;
            double openButtonWidth = snapSize(pneOpenButton.prefWidth(-1));

            displayNode.resize(w - openButtonWidth, h);
            displayNode.relocate(x, y);

            pneOpenButton.resize(openButtonWidth, textFieldHeight);
            pneOpenButton.relocate(w - openButtonWidth, yOffset);
        }
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return leftInset + rightInset;
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        double displayNodeWidth = snapSize(displayNode.prefWidth(height));
        double openButtonWidth = pneOpenButton == null ? 0d : snapSize(pneOpenButton.prefWidth(-1));
        return leftInset + displayNodeWidth + openButtonWidth + rightInset;
    }

}
