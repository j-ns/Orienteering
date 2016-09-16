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

import javafx.beans.property.ListProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.SingleSelectionModel;

public class SelectionModelBase<T> extends SingleSelectionModel<T> {

    protected ListProperty<T> itemsProperty;

    public SelectionModelBase(ListProperty<T> itemsProperty) {
        this.itemsProperty = itemsProperty;
        addItemsListener();
    }

    protected void addItemsListener() {
        itemsProperty.addListener((ov, t, t1) -> clearSelection());
    }

    @Override
    protected T getModelItem(int index) {
        if (index < 0 || index >= itemsProperty.size()) {
            return null;
        }
        return itemsProperty.get(index);
    }

    @Override
    protected int getItemCount() {
        final ObservableList<T> items = itemsProperty.get();
        return items == null ? 0 : items.size();
    }

    public boolean isSingleSelection() {
        return true;
    }

    @Override
    public void select(T obj) {
        if (obj == null) {
            setSelectedIndex(-1);
            setSelectedItem(null);
            return;
        }

        final int itemCount = getItemCount();

        for (int i = 0; i < itemCount; i++) {
            final T value = getModelItem(i);
            if (value != null && value.equals(obj)) {
                select(i);
                return;
            }
        }
    }

}
