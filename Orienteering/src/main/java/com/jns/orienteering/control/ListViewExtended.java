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

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;

import com.jns.orienteering.model.common.AccessType;
import com.jns.orienteering.model.common.ListUpdater;
import com.jns.orienteering.model.common.SelectedObjectProperty;

import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class ListViewExtended<T> extends ListView<T> implements ActivatableDeactivatable {

    private ScrollListener            scrollListener;
    private boolean                   listenersEnabled;

    private Comparator<T>             comparator;
    private ObservableList<T>         backing;
    private SelectedObjectProperty<T> selectedItem;

    private ListUpdater<T>            listUpdater;

    public ListViewExtended() {
        selectedItem = new SelectedObjectProperty<>();
        scrollListener = new ScrollListener(this);
    }

    public void setSelectableCellFactory(Function<SelectedObjectProperty<T>, ListCell<T>> cellSupplier) {
        super.setCellFactory(listView -> cellSupplier.apply(selectedItem));
    }

    public void setOnSelection(Consumer<T> consumer) {
        selectedItem.setConsumer(consumer);
    }

    public SelectedObjectProperty<T> selectedItemProperty() {
        return selectedItem;
    }

    public void setComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public void setSortableItems(ObservableList<T> items) {
        backing = items;

        SortedList<T> sorted = new SortedList<>(backing, comparator);
        setItems(sorted);

        if (listUpdater != null) {
            listUpdater.setItems(backing);
        }
    }

    public ListUpdater<T> getListUpdater(AccessType accessType) {
        ListUpdater<T> _listUpdater = getListUpdater();
        _listUpdater.setAccessType(accessType);
        return _listUpdater;
    }

    public ListUpdater<T> getListUpdater() {
        if (listUpdater == null) {
            listUpdater = new ListUpdater<>(comparator);
        }
        listUpdater.setItems(backing != null ? backing : getItems());
        return listUpdater;
    }

    @Override
    public void activate() {
        addListeners();
    }

    public void addListeners() {
        if (!listenersEnabled) {
            selectedItem.addListener();
            scrollListener.activate();
            listenersEnabled = true;
        }
    }

    @Override
    public void deactivate() {
        removeListeners();
    }

    public void removeListeners() {
        if (listenersEnabled) {
            selectedItem.removeListener();
            scrollListener.deactivate();
            listenersEnabled = false;
        }
    }

    public void reorder(T sourceItem, T targetItem) {
        getListUpdater().reorder(sourceItem, targetItem);
        clearSelection();
    }

    public void clearSelection() {
        selectedItem.set(null);
        getSelectionModel().clearSelection();
    }

}
