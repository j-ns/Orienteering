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
package com.jns.orienteering.control;

import static com.jns.orienteering.util.Validators.isNullOrEmpty;

import java.util.function.Function;

import com.jns.orienteering.control.skin.BaseChoiceFieldSkin;
import com.jns.orienteering.util.Dialogs;
import com.jns.orienteering.util.StringUtils;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;

public abstract class BaseChoiceField<T> extends Control {

    private ChoiceMenu<T>         choiceMenu;

    private Function<T, String>   stringConverter;

    private String                missingDataTitle;
    private String                missingDataMessage;

    private boolean               showOpenButton;
    private Label                 openButton;

    private SelectionModelBase<T> selectionModel;
    private ListProperty<T>       items;

    public BaseChoiceField(ChoiceMenu<T> choiceMenu) {
        this();
        this.choiceMenu = choiceMenu;
    }

    public BaseChoiceField() {
        getStyleClass().add("choice-field");
        setShowOpenButton(true);
    }

    public ChoiceMenu<T> getChoiceMenu() {
        if (choiceMenu == null) {
            choiceMenu = new ChoiceMenu<>();
            choiceMenu.setParentNode(this);
            choiceMenu.setSelectionModel(getSelectionModel());
            choiceMenu.setStringConverter(getStringConverter());
        }
        return choiceMenu;
    }

    public void setChoiceMenu(ChoiceMenu<T> choiceBox) {
        this.choiceMenu = choiceBox;
    }

    public SelectionModelBase<T> getSelectionModel() {
        if (selectionModel == null) {
            selectionModel = new SelectionModelBase<>(itemsProperty());
        }
        return selectionModel;
    }

    public void setSelectionModel(SelectionModelBase<T> selectionModel) {
        this.selectionModel = selectionModel;
    }

    public T getSelectedItem() {
        return getSelectionModel().getSelectedItem();
    }

    public abstract String getHint();

    public abstract void setHint(String hint);

    public Function<T, String> getStringConverter() {
        if (stringConverter == null) {
            stringConverter = StringUtils.getDefaultStringConverter();
        }
        return stringConverter;
    }

    public void setStringConverter(Function<T, String> stringConverter) {
        this.stringConverter = stringConverter;
    }

    public String getMissingDataTitle() {
        return missingDataTitle;
    }

    public void setMissingDataTitle(String text) {
        this.missingDataTitle = text;
    }

    public String getMissingDataMessage() {
        return missingDataMessage;
    }

    public void setMissingDataInfo(String title, String message) {
        missingDataTitle = title;
        missingDataMessage = message;
    }

    public void setShowOpenButton(boolean value) {
        showOpenButton = value;
    }

    public boolean isShowOpenButton() {
        return showOpenButton;
    }

    public Label getOpenButton() {
        return openButton;
    }

    public void setOpenButton(Label label) {
        this.openButton = label;
    }

    public abstract Node getDisplayNode();

    public abstract void setEditable(boolean value);

    public ObservableList<T> getItems() {
        return items == null ? null : items.get();
    }

    public void setItems(ObservableList<T> items) {
        itemsProperty().set(items);
    }

    public ListProperty<T> itemsProperty() {
        if (items == null) {
            items = new SimpleListProperty<T>(this, "items", FXCollections.observableArrayList());
        }
        return items;
    }

    public void showMissingDataMessage() {
        Dialogs.ok(missingDataTitle, missingDataMessage).showAndWait();
    }

    public abstract void updateDisplay(T item);

    public boolean isEmpty() {
        return isNullOrEmpty(getItems());
    }

    public void clear() {
        if (items != null) {
            items.set(FXCollections.emptyObservableList());
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new BaseChoiceFieldSkin<T, BaseChoiceField<T>>(this);
    }
}
