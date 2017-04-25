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
package com.jns.orienteering.control;

import java.util.function.Function;

import com.jns.orienteering.control.cell.ChoiceCell;
import com.jns.orienteering.control.skin.ChoiceMenuSkin;
import com.jns.orienteering.util.Validations;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.util.Callback;

public class ChoiceMenu<T> extends Control {

    public static final EventType<Event>                       ON_SHOWING   = new EventType<>(Event.ANY, "CHOICE_MENU_SHOWING");
    public static final EventType<Event>                       ON_HIDING    = new EventType<>(Event.ANY, "CHOICE_MENU_HIDING");
    public static final EventType<Event>                       ON_SELECTION = new EventType<>(Event.ANY, "CHOICE_MENU_SELECTION");

    private ObjectProperty<Callback<ListView<T>, ListCell<T>>> cellFactory;
    private Function<T, String>                                stringConverter;

    private boolean                                            showSearchBox;
    private StringProperty                                     filterText;

    private ObjectProperty<ObservableList<T>>                  items;
    private SelectionModelBase<T>                              selectionModel;

    private boolean                                            showing;
    private boolean                                            hiding;

    public ChoiceMenu() {
        getStyleClass().add("choice-menu");
    }

    public final ObjectProperty<Callback<ListView<T>, ListCell<T>>> cellFactoryProperty() {
        if (cellFactory == null) {
            cellFactory = new SimpleObjectProperty<>(this, "cellFactory");
        }
        return cellFactory;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final Callback<ListView<T>, ListCell<T>> getCellFactory() {
        if (cellFactory != null) {
            return cellFactory.get();
        }
        return lv -> new ChoiceCell(this);
    }

    public final void setCellFactory(Callback<ListView<T>, ListCell<T>> value) {
        cellFactoryProperty().set(value);
    }

    public Function<T, String> getStringConverter() {
        return stringConverter;
    }

    public void setStringConverter(Function<T, String> stringConverter) {
        this.stringConverter = stringConverter;
    }

    public boolean isShowSearchBox() {
        return showSearchBox;
    }

    public void setShowSearchBox(boolean showSearchBox) {
        this.showSearchBox = showSearchBox;
    }

    public final StringProperty filterTextProperty() {
        if (filterText == null) {
            filterText = new SimpleStringProperty(this, "filterText");
        }
        return filterText;
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

    public final ObjectProperty<ObservableList<T>> itemsProperty() {
        if (items == null) {
            items = new SimpleObjectProperty<>(this, "items");
        }
        return items;
    }

    public final ObservableList<T> getItems() {
        return items == null ? null : items.get();
    }

    public final void setItems(ObservableList<T> value) {
        itemsProperty().set(value);
    }

    public boolean isEmpty() {
        return Validations.isNullOrEmpty(getItems());
    }

    private ObjectProperty<EventHandler<Event>> onSelection = new ObjectPropertyBase<EventHandler<Event>>() {

        @Override
        protected void invalidated() {
            setEventHandler(ON_SELECTION, get());
        }

        @Override
        public Object getBean() {
            return ChoiceMenu.this;
        }

        @Override
        public String getName() {
            return "onSelection";
        }
    };

    public final ObjectProperty<EventHandler<Event>> onSelectionProperty() {
        return onSelection;
    }

    public void setOnSelection(EventHandler<Event> handler) {
        onSelection.set(handler);
    }

    private ObjectProperty<EventHandler<Event>> onShowing = new ObjectPropertyBase<EventHandler<Event>>() {

        @Override
        protected void invalidated() {
            setEventHandler(ON_SHOWING, get());
        }

        @Override
        public Object getBean() {
            return ChoiceMenu.this;
        }

        @Override
        public String getName() {
            return "onShowing";
        }
    };

    public final ObjectProperty<EventHandler<Event>> onShowingProperty() {
        return onShowing;
    }

    public final void setOnShowing(EventHandler<Event> handler) {
        onShowing.set(handler);
    }

    private ObjectProperty<EventHandler<Event>> onHiding = new ObjectPropertyBase<EventHandler<Event>>() {

        @Override
        protected void invalidated() {
            setEventHandler(ON_HIDING, get());
        }

        @Override
        public Object getBean() {
            return ChoiceMenu.this;
        }

        @Override
        public String getName() {
            return "onHiding";
        }
    };

    public final ObjectProperty<EventHandler<Event>> onHidingProperty() {
        return onHiding;
    }

    public final void setOnHiding(EventHandler<Event> handler) {
        onHiding.set(handler);
    }

    public void show() {
        if (!isShowing()) {

            showing = true;
            fireEvent(new Event(ChoiceMenu.ON_SHOWING));
        }
    }

    public void hide() {
        if (isShowing() && !hiding) {

            hiding = true;
            fireEvent(new Event(ChoiceMenu.ON_HIDING));

            showing = false;
            hiding = false;

            if (showSearchBox) {
                filterTextProperty().set(null);
            }
        }
    }

    public void selected() {
        hide();
        fireEvent(new Event(ChoiceMenu.ON_SELECTION));
    }

    public boolean isShowing() {
        return showing;
    }

    public void setShowing(boolean value) {
        showing = value;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ChoiceMenuSkin<>(this);
    }
}