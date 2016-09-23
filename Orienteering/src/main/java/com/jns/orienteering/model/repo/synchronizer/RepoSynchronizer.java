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
package com.jns.orienteering.model.repo.synchronizer;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.down.common.PlatformFactory;
import com.gluonhq.charm.down.common.SettingService;
import com.gluonhq.connect.ConnectState;
import com.jns.orienteering.model.dynamic.CountProperty;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

public class RepoSynchronizer {

    private static final Logger                       LOGGER               = LoggerFactory.getLogger(RepoSynchronizer.class);

    private static final String                       LAST_SYNCED_PROPERTY = "lastSync";

    private SettingService                            settingService       = PlatformFactory.getPlatform().getSettingService();

    private Map<String, BaseSynchronizer<?, ?, ?>>    synchronizers        = new HashMap<>();
    private SyncMetaData                              syncMetaData;

    private CountProperty                             pendingSynchronizers = new CountProperty();
    private ObjectProperty<ConnectState>              syncState            = new SimpleObjectProperty<>(ConnectState.READY);
    private Map<String, ChangeListener<ConnectState>> syncStateListeners   = new HashMap<>();

    private int                                       activeSynchronizers  = 0;

    public RepoSynchronizer() {
        pendingSynchronizers.addListener(l ->
        {
            LOGGER.debug("pendingSynchronizers: {}", pendingSynchronizers.get());

            if (pendingSynchronizers.get() > 0) {
                syncState.set(ConnectState.RUNNING);

            } else if (pendingSynchronizers.get() == 0) {
                if (activeSynchronizers > 0) {
                    return;
                }
                if (pendingSynchronizers.succeeded()) {
                    syncState.set(ConnectState.SUCCEEDED);
                    storeLastSyncedTimeStamp(syncMetaData.getCurrentTimeStamp());
                } else {
                    syncState.set(ConnectState.FAILED);
                }

            } else {
                throw new IndexOutOfBoundsException("pendingSynchronizers == -1");
            }
        });
    }

    public CountProperty pendingSynchronizersProperty() {
        return pendingSynchronizers;
    }

    public ReadOnlyObjectProperty<ConnectState> syncStateProperty() {
        return syncState;
    }

    public void addSynchronizer(BaseSynchronizer<?, ?, ?> synchronizer) {
        synchronizers.put(synchronizer.getName(), synchronizer);
    }

    public void syncNow(String synchronizerName, SyncMetaData syncMetaData) {
        this.syncMetaData = syncMetaData;
        setTimeStamps(syncMetaData);

        BaseSynchronizer<?, ?, ?> synchronizer = synchronizers.get(synchronizerName);
        synchronizer.syncStateProperty().addListener(getStateListener(synchronizerName));
        synchronizer.syncNow(syncMetaData);
    }

    public void syncNow(SyncMetaData syncMetaData) {
        this.syncMetaData = syncMetaData;
        setTimeStamps(syncMetaData);

        LOGGER.debug("lastSyncTimeStamp: {}", syncMetaData.getLastSynced());

        for (BaseSynchronizer<?, ?, ?> synchronizer : synchronizers.values()) {
            activeSynchronizers++;
            synchronizer.syncStateProperty().addListener(getStateListener(synchronizer.getName()));
            synchronizer.syncNow(syncMetaData);
        }
    }

    private ChangeListener<ConnectState> getStateListener(String synchronizerName) {
        if (!syncStateListeners.containsKey(synchronizerName)) {
            syncStateListeners.put(synchronizerName, createStateListener(synchronizerName));
        }
        return syncStateListeners.get(synchronizerName);
    }

    private ChangeListener<ConnectState> createStateListener(String synchronizerName) {
        ChangeListener<ConnectState> listener = (obsVal, st, st1) ->
        {
            switch (st1) {
                case RUNNING:
                    LOGGER.debug("increment pendingSynchronizers: {}", synchronizerName);
                    pendingSynchronizers.increment();
                    break;

                case SUCCEEDED:
                    LOGGER.debug("decrement pendingSynchronizers: {}", synchronizerName);
                    activeSynchronizers--;
                    pendingSynchronizers.decrement();
                    removeStateListener(synchronizerName);
                    break;

                case FAILED:
                    LOGGER.debug("decrement exceptionally pendingSynchronizers: {}", synchronizerName);
                    activeSynchronizers--;
                    pendingSynchronizers.decrementExceptionally();
                    removeStateListener(synchronizerName);
                    break;

                default:
                    break;
            }
        };
        syncStateListeners.put(synchronizerName, listener);
        return listener;
    }

    private void removeStateListener(String synchronizerName) {
        BaseSynchronizer<?, ?, ?> synchronizer = synchronizers.get(synchronizerName);
        ChangeListener<ConnectState> listener = syncStateListeners.remove(synchronizerName);
        synchronizer.syncStateProperty().removeListener(listener);
        LOGGER.debug("removed stateListener for synchronizer: {}", synchronizerName);
    }

    private void setTimeStamps(SyncMetaData syncMetaData) {
        syncMetaData.setLastSynced(getLastSyncedTimeStamp());
        syncMetaData.setCurrentTimeStamp();
    }

    private long getLastSyncedTimeStamp() {
        String property = settingService.retrieve(LAST_SYNCED_PROPERTY);
        return property == null ? 0 : Long.valueOf(property);
    }

    private void storeLastSyncedTimeStamp(long timeStamp) {
        settingService.store(LAST_SYNCED_PROPERTY, Long.toString(timeStamp));
    }

}
