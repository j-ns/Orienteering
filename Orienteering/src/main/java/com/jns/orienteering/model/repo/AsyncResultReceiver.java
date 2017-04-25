package com.jns.orienteering.model.repo;

import static com.jns.orienteering.control.Dialogs.showError;
import static com.jns.orienteering.locale.Localization.localize;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.connect.ConnectState;
import com.gluonhq.connect.GluonObservable;
import com.jns.orienteering.control.Dialogs;
import com.jns.orienteering.control.Message;
import com.jns.orienteering.control.ProgressLayer;
import com.jns.orienteering.model.common.CountProperty;
import com.jns.orienteering.model.common.GluonObservables;
import com.jns.orienteering.util.Trigger;

import javafx.beans.value.ChangeListener;

public class AsyncResultReceiver<T extends GluonObservable> {

    private static final Logger                            LOGGER                      = LoggerFactory.getLogger(AsyncResultReceiver.class);

    private static final MobileApplication                 APPLICATION                 = MobileApplication.getInstance();
    private static final String                            DEFAULT_PROGRESS_LAYER_NAME = ProgressLayer.DEFAULT_LAYER_NAME;

    private static final CountProperty                     runningInstances            = new CountProperty();

    private Callable<T>                                    observableSupplier;
    private T                                              observableResult;
    private Consumer<T>                                    consumer;

    private String                                         progressLayerName;
    private Consumer<Throwable>                            onException;
    private Message                                        exceptionMessage;
    private Consumer<T>                                    finalizer;

    private AsyncResultReceiver<? extends GluonObservable> next;

    private ChangeListener<? super ConnectState>           stateListener               = (obsValue, st, st1) ->
                                                                                           {
                                                                                               switch (st1) {

                                                                                                   case REMOVED:
                                                                                                       ifPresentConsume(consumer, observableResult);
                                                                                                       startFinalizer();
                                                                                                       break;

                                                                                                   case FAILED:
                                                                                                       showError(localize("dialog.error.connectionFailed"));
                                                                                                       startFinalizer();
                                                                                                       break;

                                                                                                   default:
                                                                                                       break;
                                                                                               }
                                                                                           };

    private ChangeListener<? super Boolean>                initializedListener         = (obsValue, b, b1) ->
                                                                                           {
                                                                                               if (b1) {
                                                                                                   ifPresentConsume(consumer, observableResult);
                                                                                                   startFinalizer();
                                                                                               }
                                                                                           };

    private ChangeListener<? super Throwable>              exceptionListener           = (obsValue, ex, ex1) ->
                                                                                           {
                                                                                               if (ex1 != null) {
                                                                                                   LOGGER.error("AsyncRestultReceiver exception:",
                                                                                                                ex1);

                                                                                                   ifPresentConsume(onException, ex1);
                                                                                                   ifPresent(exceptionMessage, Dialogs::showError);

                                                                                                   if (ex1 instanceof UnknownHostException ||
                                                                                                           ex1 instanceof ConnectException) {
                                                                                                       showError(localize("dialog.error.connectionFailed"));
                                                                                                   }

                                                                                                   startFinalizer();
                                                                                               }
                                                                                           };

    private GluonObservable                                initializeOnSuccess;

    private AsyncResultReceiver(T observable) {
        this.observableResult = observable;
    }

    public static <T extends GluonObservable> AsyncResultReceiver<T> create(T observable) {
        return new AsyncResultReceiver<T>(observable);
    }

    public AsyncResultReceiver<T> defaultProgressLayer() {
        progressLayerName = DEFAULT_PROGRESS_LAYER_NAME;
        return this;
    }

    public AsyncResultReceiver<T> progressLayer(ProgressLayer progressLayer) {
        progressLayerName = progressLayer.getLayerName();
        return this;
    }

    public AsyncResultReceiver<T> onSuccess(Consumer<T> consumer) {
        this.consumer = consumer;
        return this;
    }

    /**
     * Convenience method to set another GluonObservable initialized, if this AsyncResultReceiver succeeds.
     *
     * @param obsValue
     *            the GluonObservable which is listening for the result of this AsyncResultReceiver
     * @return
     */
    public AsyncResultReceiver<T> setInitializedOnSuccess(GluonObservable obsValue) {
        initializeOnSuccess = obsValue;
        return this;
    }

    public AsyncResultReceiver<T> onException(Trigger onException) {
        return onException(e -> onException.start());
    }

    public AsyncResultReceiver<T> onException(Consumer<Throwable> onException) {
        this.onException = onException;
        return this;
    }

    public AsyncResultReceiver<T> propagateException(GluonObservable obsValue) {
        onException = ex -> GluonObservables.setException(obsValue, ex);
        return this;
    }

    public AsyncResultReceiver<T> exceptionMessage(String title) {
        exceptionMessage = Message.create().title(title);
        return this;
    }

    public AsyncResultReceiver<T> exceptionMessage(Message message) {
        exceptionMessage = message;
        return this;
    }

    public AsyncResultReceiver<T> finalize(Trigger finalizer) {
        return finalize(e -> finalizer.start());
    }

    public AsyncResultReceiver<T> finalize(Consumer<T> finalizer) {
        this.finalizer = finalizer;
        return this;
    }

    public <U extends GluonObservable> AsyncResultReceiver<T> next(AsyncResultReceiver<U> receiver) {
        next = receiver;
        return this;
    }

    public void start() {
        runningInstances.increment();

        ensureObservable();

        if (observableResult.getException() != null) {
            ifPresentConsume(onException, observableResult.getException());
            ifPresent(exceptionMessage, Dialogs::showError);
            startFinalizer();

        } else if (observableResult.isInitialized()) {
            ifPresentConsume(consumer, observableResult);
            startFinalizer();

        } else {
            observableResult.stateProperty().addListener(stateListener);
            observableResult.initializedProperty().addListener(initializedListener);
            observableResult.exceptionProperty().addListener(exceptionListener);

            ifPresent(progressLayerName, APPLICATION::showLayer);
        }
    }

    private void ensureObservable() {
        if (observableResult == null && observableSupplier != null) {
            try {
                observableResult = observableSupplier.call();

            } catch (Exception ex) {
                LOGGER.error("Failed to initialize observable", ex);
            }
        }
    }

    private void startFinalizer() {
        removeListeners();

        ifPresentConsume(finalizer, observableResult);
        runningInstances.decrement();

        if (next == null) {
            if (runningInstances.get() == 0) {
                ifPresent(progressLayerName, APPLICATION::hideLayer);
            }
        } else {
            if (progressLayerName != null && !progressLayerName.equals(next.progressLayerName)) {
                APPLICATION.hideLayer(progressLayerName);
            }
            next.start();
        }

        ifPresent(initializeOnSuccess, GluonObservables::setInitialized);
    }

    private void removeListeners() {
        observableResult.stateProperty().removeListener(stateListener);
        observableResult.initializedProperty().removeListener(initializedListener);
        observableResult.exceptionProperty().removeListener(exceptionListener);
    }

    private <U> void ifPresentConsume(Consumer<U> consumer, U target) {
        if (consumer != null) {
            consumer.accept(target);
        }
    }

    private <U> void ifPresent(U value, Consumer<? super U> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }

}