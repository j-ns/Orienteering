package com.jns.orienteering.model.dynamic;

import java.util.function.BiConsumer;

import com.gluonhq.connect.GluonObservableList;
import com.jns.orienteering.model.common.AccessType;
import com.jns.orienteering.model.common.CityAssignable;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.util.GluonObservableHelper;

import javafx.collections.ObservableList;

public abstract class LocalCache<T extends CityAssignable> {

    private String                 cityId;
    private String                 userId;
    private GluonObservableList<T> publicItems  = new GluonObservableList<>();
    private GluonObservableList<T> privateItems = new GluonObservableList<>();

    public GluonObservableList<T> refreshPrivateList(String cityId, String userId) {
        privateItems = new GluonObservableList<>();
        return getPrivateList(cityId, userId);
    }

    public GluonObservableList<T> refreshPublicList(String cityId) {
        publicItems = new GluonObservableList<>();
        return getPublicList(cityId);
    }

    public GluonObservableList<T> getPrivateList(String cityId, String userId) {
        if (!cityId.equals(this.cityId) || !userId.equals(this.userId)) {
            clearAll();
        }

        if (privateItems.isEmpty()) {
            GluonObservableList<T> obsPublicItems = retrievePrivateList(cityId, userId);
            AsyncResultReceiver.create(obsPublicItems)
                               .onSuccess(result ->
                               {
                                   privateItems.setAll(result);
                                   this.cityId = cityId;
                                   this.userId = userId;

                                   GluonObservableHelper.setInitialized(privateItems, true);
                               })
                               .propagateException(privateItems)
                               .start();
        }

        return privateItems;
    }

    public GluonObservableList<T> getPublicList(String cityId) {
        if (!cityId.equals(this.cityId)) {
            clearAll();
        }

        if (publicItems.isEmpty()) {
            GluonObservableList<T> obsPublicItems = retrievePublicList(cityId);
            AsyncResultReceiver.create(obsPublicItems)
                               .onSuccess(result ->
                               {
                                   publicItems.setAll(result);
                                   GluonObservableHelper.setInitialized(publicItems, true);
                               })
                               .propagateException(publicItems)
                               .start();
        }

        return publicItems;
    }

    protected abstract GluonObservableList<T> retrievePrivateList(String cityId, String userId);

    protected abstract GluonObservableList<T> retrievePublicList(String cityId);

    public GluonObservableList<T> getPublicItems() {
        return publicItems;
    }

    public GluonObservableList<T> getPrivateItems() {
        return privateItems;
    }

    public void addItem(T item) {
        if (!item.cityChanged()) {
            updateItems(item, ObservableList<T>::add);
        }
    }

    public void updateItem(T newItem, T previousItem) {
        removeItem(previousItem);
        if (!newItem.cityChanged()) {
            updateItems(newItem, ObservableList<T>::add);
        }
    }

    public void removeItem(T item) {
        if (item.getAccessType() == AccessType.PUBLIC) {
            if (publicItems.isEmpty()) {
                return;
            }
        } else {
            if (privateItems.isEmpty()) {
                return;
            }
        }
        updateItems(item, ObservableList<T>::remove);
    }

    private void updateItems(T item, BiConsumer<ObservableList<T>, T> action) {
        if (item.getAccessType() == AccessType.PRIVATE) {
            action.accept(privateItems, item);
        } else {
            action.accept(publicItems, item);
        }
    }

    public void clearAll() {
        privateItems = new GluonObservableList<>();
        publicItems = new GluonObservableList<>();
    }

}
