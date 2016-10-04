package com.jns.orienteering.model.dynamic;

import static com.jns.orienteering.util.Validators.isNullOrEmpty;

import java.util.function.BiConsumer;

import com.gluonhq.connect.GluonObservableList;
import com.jns.orienteering.model.common.AccessType;
import com.jns.orienteering.model.common.CityAssignable;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.util.GluonObservableHelper;

import javafx.collections.ObservableList;

public abstract class LocalCache<E extends CityAssignable> {

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
            clearPrivateItems();
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
            clearPublicItems();
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
                privateItems = GluonObservableHelper.newGluonObservableListInitialized();
            }
        } else {
            if (publicItems == null) {
                publicItems = GluonObservableHelper.newGluonObservableListInitialized();
            }
        }
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
