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
package com.jns.orienteering.model.common;

import java.util.Comparator;
import java.util.Objects;

import com.jns.orienteering.model.persisted.AccessType;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ListUpdater<E> {

    private Comparator<E>     comparator;
    private ObservableList<E> items;
    private AccessType        accessType;

    public ListUpdater(Comparator<E> comparator) {
        Objects.requireNonNull(comparator, "comparator can not be null");
        this.comparator = comparator;
    }

    public void setAll(ObservableList<E> items) {
        this.items.setAll(items);
    }

    public ObservableList<E> getItems() {
        return items;
    }

    public void setItems(ObservableList<E> items) {
        this.items = items;
    }

    public ObservableList<E> createItemsCopy() {
        return FXCollections.observableArrayList(items);
    }

    public void add(E item) {
        items.add(item);
    }

    public void add(int idx, E item) {
        items.add(idx, item);
    }

    public void update(E item) {
        int idx = indexOf(item);
        if (idx > -1) {
            items.set(idx, item);
        }
    }

    public void remove(E item) {
        int idx = indexOf(item);
        if (idx > -1) {
            items.remove(idx);
        }
    }

    public void replace(E oldItem, E newItem) {
        int idx = indexOf(oldItem);
        if (idx > -1) {
            items.set(idx, newItem);
        }
    }

    public void reorder(E sourceItem, E targetItem) {
        int idxSource = indexOf(sourceItem);
        int idxTarget = indexOf(targetItem);
        if (idxSource != idxTarget) {
            items.remove(sourceItem);
            items.add(idxTarget, sourceItem);
        }
    }

    public void clear() {
        items.clear();
    }

    public boolean contains(E item) {
        return indexOf(item) > -1;
    }

    public int indexOf(E item) {
        for (int idx = 0; idx < items.size(); idx++) {
            if (comparator.compare(item, items.get(idx)) == 0) {
                return idx;
            }
        }
        return -1;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

}