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
package com.jns.orienteering.model.repo;

import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.connect.ConnectState;
import com.gluonhq.connect.GluonObservable;
import com.jns.orienteering.control.ProgressLayer;
import com.jns.orienteering.control.ProgressLayer.PauseFadeInHide;
import com.jns.orienteering.locale.Localization;
import com.jns.orienteering.util.Dialogs;
import com.jns.orienteering.util.Trigger;

import javafx.beans.value.ChangeListener;

public class AsyncResultReceiver<T extends GluonObservable> {

    private static final Logger        LOGGER                 = LoggerFactory.getLogger(AsyncResultReceiver.class);

    private static final ProgressLayer DEFAULT_PROGRESS_LAYER = new ProgressLayer(PauseFadeInHide::new);

    private T                          observable;
    private Optional<Consumer<T>>      consumer               = Optional.empty();

    private Optional<ProgressLayer>    progressLayer          = Optional.empty();
    private Optional<Trigger>          onException            = Optional.empty();
    private Optional<String>           exceptionMessage       = Optional.empty();
    private Optional<Trigger>          finalizer              = Optional.empty();

    private AsyncResultReceiver(T observable) {
        this.observable = observable;
    }

    public static <T extends GluonObservable> AsyncResultReceiver<T> create(T observable) {
        return new AsyncResultReceiver<T>(observable);
    }

    public AsyncResultReceiver<T> onSuccess(Consumer<T> consumer) {
        this.consumer = Optional.of(consumer);
        return this;
    }

    public AsyncResultReceiver<T> defaultProgressLayer() {
        this.progressLayer = Optional.of(DEFAULT_PROGRESS_LAYER);
        return this;
    }

    public AsyncResultReceiver<T> progressLayer(ProgressLayer progressLayer) {
        this.progressLayer = Optional.of(progressLayer);
        return this;
    }

    public AsyncResultReceiver<T> onException(Trigger onException) {
        this.onException = Optional.of(onException);
        return this;
    }

    public AsyncResultReceiver<T> exceptionMessage(String message) {
        exceptionMessage = Optional.of(message);
        return this;
    }

    public AsyncResultReceiver<T> finalize(Trigger finalizer) {
        this.finalizer = Optional.of(finalizer);
        return this;
    }

    public void start() {
        if (observable.isInitialized()) {
            consumer.ifPresent(c -> c.accept(observable));
            startFinalizer();
        } else {
            observable.stateProperty().addListener(stateListener);
            observable.initializedProperty().addListener(initializedListener);
            observable.exceptionProperty().addListener(exceptionListener);
            progressLayer.ifPresent(ProgressLayer::show);
        }
    }

    private ChangeListener<? super ConnectState> stateListener       = (obsValue, st, st1) ->
                                                                     {
                                                                         switch (st1) {
                                                                             case RUNNING:
                                                                                 break;

                                                                             case REMOVED:
                                                                                 startFinalizer();
                                                                                 break;

                                                                             case FAILED:
                                                                                 Dialogs.ok(Localization.localize("dialog.error.connectionFailed"))
                                                                                        .showAndWait();
                                                                                 startFinalizer();
                                                                                 break;

                                                                             default:
                                                                                 break;
                                                                         }
                                                                     };

    private ChangeListener<? super Boolean>      initializedListener = (obsValue, b, b1) ->
                                                                     {
                                                                         if (b1) {
                                                                             consumer.ifPresent(c -> c.accept(observable));
                                                                             startFinalizer();
                                                                         }
                                                                     };

    private ChangeListener<? super Throwable>    exceptionListener   = (obsValue, e, e1) ->
                                                                     {
                                                                         if (e1 != null) {
                                                                             onException.ifPresent(Trigger::start);
                                                                             exceptionMessage.ifPresent(msg -> Dialogs.ok(msg).showAndWait());
                                                                             startFinalizer();
                                                                         }
                                                                     };

    private void startFinalizer() {
        progressLayer.ifPresent(ProgressLayer::hide);
        finalizer.ifPresent(Trigger::start);
        removeListeners();
    }

    private void removeListeners() {
        observable.stateProperty().removeListener(stateListener);
        observable.initializedProperty().removeListener(initializedListener);
        observable.exceptionProperty().removeListener(exceptionListener);
    }

    // public AsyncResultReceiver<GluonObservable> next() {
    //
    // }
}