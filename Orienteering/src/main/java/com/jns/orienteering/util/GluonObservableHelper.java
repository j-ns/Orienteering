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
package com.jns.orienteering.util;

import java.util.Collection;

import com.gluonhq.connect.ConnectState;
import com.gluonhq.connect.GluonObservable;
import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.GluonObservableObject;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class GluonObservableHelper {

    private GluonObservableHelper() {
    }

    public static <T> GluonObservableList<T> newGluonObservableListInitialized(Collection<T> items) {
        GluonObservableList<T> gluonObservableList = new GluonObservableList<>();
        gluonObservableList.setAll(items);
        setInitialized(gluonObservableList);
        return gluonObservableList;
    }

    public static <T> GluonObservableList<T> newGluonObservableListInitialized() {
        GluonObservableList<T> gluonObservableList = new GluonObservableList<>();
        setInitialized(gluonObservableList);
        return gluonObservableList;
    }

    public static <T> GluonObservableObject<T> newGluonObservableInitialized() {
        GluonObservableObject<T> gluonObservableObject = new GluonObservableObject<>();
        setInitialized(gluonObservableObject);
        return gluonObservableObject;
    }

    public static <T> GluonObservableObject<T> newGluonObservable(T obj) {
        GluonObservableObject<T> gluonObservableObject = new GluonObservableObject<>();
        gluonObservableObject.set(obj);
        return gluonObservableObject;
    }

    public static <T> void setInitialized(GluonObservableObject<T> observable, T value, boolean initialized) {
        Platform.runLater(() ->
        {
            observable.set(value);
            ((SimpleBooleanProperty) observable.initializedProperty()).set(initialized);
            ((SimpleObjectProperty<ConnectState>) observable.stateProperty()).set(ConnectState.SUCCEEDED);
        });
    }

    public static void setInitialized(GluonObservable observable) {
        setInitialized(observable, true);
    }

    public static void setInitialized(GluonObservable observable, boolean initialized) {
        if (!observable.isInitialized()) {
            Platform.runLater(() ->
            {
                ((SimpleBooleanProperty) observable.initializedProperty()).set(initialized);
                ((SimpleObjectProperty<ConnectState>) observable.stateProperty()).set(ConnectState.SUCCEEDED);
            });
        }
    }

    public static void setException(GluonObservable observable, Throwable ex) {
        Platform.runLater(() ->
        {
            ((SimpleObjectProperty<Throwable>) observable.exceptionProperty()).set(ex);
            ((SimpleObjectProperty<ConnectState>) observable.stateProperty()).set(ConnectState.FAILED);
        });
    }

}
