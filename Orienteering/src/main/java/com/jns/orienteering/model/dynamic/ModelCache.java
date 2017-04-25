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
package com.jns.orienteering.model.dynamic;

import static com.jns.orienteering.util.Validations.isNullOrEmpty;

import java.util.function.BiConsumer;

import com.gluonhq.connect.GluonObservableList;
import com.jns.orienteering.model.common.GluonObservables;
import com.jns.orienteering.model.persisted.AccessType;
import com.jns.orienteering.model.persisted.CityAssignable;
import com.jns.orienteering.model.repo.AsyncResultReceiver;

import javafx.collections.ObservableList;

public abstract class ModelCache<E extends CityAssignable> {

    private String                 cityId;
    private String                 userId;

    private GluonObservableList<E> publicItems  = new GluonObservableList<>();
    private GluonObservableList<E> privateItems = new GluonObservableList<>();

    public GluonObservableList<E> refreshPrivateItems(String cityId, String userId) {
        privateItems = null;
        return getPrivateItems(cityId, userId);
    }

    public GluonObservableList<E> refreshPublicItems(String cityId) {
        publicItems = null;
        return getPublicItems(cityId);
    }

    public GluonObservableList<E> getPrivateItems(String cityId, String userId) {
        if (!cityId.equals(this.cityId) || !userId.equals(this.userId)) {
            clearItems();
        }

        if (isNullOrEmpty(privateItems)) {
            privateItems = retrievePrivateItems(cityId, userId);
            AsyncResultReceiver.create(privateItems)
                               .onSuccess(result ->
                               {
                                   this.cityId = cityId;
                                   this.userId = userId;
                               })
                               .start();
        }

        return privateItems;
    }

    public GluonObservableList<E> getPublicItems(String cityId) {
        if (!cityId.equals(this.cityId)) {
            clearItems();
        }

        if (isNullOrEmpty(publicItems)) {
            publicItems = retrievePublicItems(cityId);
            AsyncResultReceiver.create(publicItems)
                               .onSuccess(e -> this.cityId = cityId)
                               .start();
        }

        return publicItems;
    }

    protected abstract GluonObservableList<E> retrievePrivateItems(String cityId, String userId);

    protected abstract GluonObservableList<E> retrievePublicItems(String cityId);

    public void addItem(E item) {
        if (item.getCityId().equals(cityId)) {
            ensureItemsInitialized(item.getAccessType());
            updateItems(item, ObservableList<E>::add);
        }
    }

    public void updateItem(E newItem, E previousItem) {
        if (newItem.getCityId().equals(cityId)) {
            removeItem(previousItem);
            addItem(newItem);
        }
    }

    public void removeItem(E item) {
        if (!item.getCityId().equals(cityId)) {
            return;
        }
        if (item.getAccessType() == AccessType.PUBLIC) {
            if (publicItems.isEmpty()) {
                return;
            }
        } else {
            if (privateItems.isEmpty()) {
                return;
            }
        }
        updateItems(item, ObservableList<E>::remove);
    }

    private void updateItems(E item, BiConsumer<ObservableList<E>, E> action) {
        ensureItemsInitialized(item.getAccessType());

        if (item.getAccessType() == AccessType.PRIVATE) {
            action.accept(privateItems, item);
        } else {
            action.accept(publicItems, item);
        }
    }

    private void ensureItemsInitialized(AccessType accessType) {
        if (accessType == AccessType.PRIVATE) {
            if (privateItems == null) {
                privateItems = GluonObservables.newListInitialized();
            }
        } else {
            if (publicItems == null) {
                publicItems = GluonObservables.newListInitialized();
            }
        }
    }

    public void clearItems() {
        clearPrivateItems();
        clearPublicItems();
    }

    public void clearItems(AccessType accessType) {
        if (accessType == AccessType.PRIVATE) {
            clearPrivateItems();
        } else {
            clearPublicItems();
        }
    }

    protected void clearPrivateItems() {
        privateItems = new GluonObservableList<>();
    }

    protected void clearPublicItems() {
        publicItems = new GluonObservableList<>();
    }

}
