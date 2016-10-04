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

import java.io.IOException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.connect.ConnectState;
import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.GluonObservableObject;
import com.jns.orienteering.model.common.Synchronizable;
import com.jns.orienteering.model.persisted.ChangeLogEntry;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.ChangeLogRepo;
import com.jns.orienteering.model.repo.FireBaseRepo;
import com.jns.orienteering.model.repo.LocalRepo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

/**
 * @param <T>
 *            type of the synchronizable data
 * @param <L>
 *            type of the locally stored data
 */
public abstract class BaseSynchronizer<T extends Synchronizable, L> {

    private static final Logger            LOGGER    = LoggerFactory.getLogger(BaseSynchronizer.class);

    private static ChangeLogRepo           logRepo   = new ChangeLogRepo();

    protected FireBaseRepo<T>              cloudRepo;
    protected LocalRepo<T, L>              localRepo;
    protected String                       listIdentifier;

    protected BiFunction<List<T>, Long, L> cloudToLocalMapper;

    private SyncMetaData                   syncMetaData;
    private ObjectProperty<ConnectState>   syncState = new SimpleObjectProperty<>(ConnectState.READY);
    private Consumer<ObservableList<T>>              onSynced;

    public BaseSynchronizer(String listIdentifier) {
        this.listIdentifier = listIdentifier;
    }

    public BaseSynchronizer(FireBaseRepo<T> cloudRepo, LocalRepo<T, L> localRepo) {
        this(cloudRepo, localRepo, null, null);
    }

    public BaseSynchronizer(FireBaseRepo<T> cloudRepo, LocalRepo<T, L> localRepo, BiFunction<List<T>, Long, L> cloudToLocalMapper,
                            String listIdentifier) {
        this.cloudRepo = cloudRepo;
        this.localRepo = localRepo;
        this.cloudToLocalMapper = cloudToLocalMapper;
        this.listIdentifier = listIdentifier;
    }

    public abstract String getName();

    public ReadOnlyObjectProperty<ConnectState> syncStateProperty() {
        return syncState;
    }

    protected void setRunning() {
        syncState.set(ConnectState.RUNNING);
    }

    protected void setSucceeded() {
        syncState.set(ConnectState.SUCCEEDED);
    }

    protected void setFailed() {
        syncState.set(ConnectState.FAILED);
    }

    public Consumer<ObservableList<T>> getOnSynced() {
        return onSynced;
    }

    public void setOnSynced(Consumer<ObservableList<T>> onSynced) {
        this.onSynced = onSynced;
    }

    public SyncMetaData getSyncMetaData() {
        return syncMetaData;
    }

    public void setSyncMetaData(SyncMetaData syncMetaData) {
        this.syncMetaData = syncMetaData;
    }

    public abstract void syncNow(SyncMetaData syncMetaData);

    protected void retrieveCloudDataAndStoreLocally() {
        AsyncResultReceiver.create(cloudRepo.retrieveListAsync())
                           .onSuccess(this::storeLocally)
                           .onException(this::setFailed)
                           .start();
    }

    protected void storeLocally(GluonObservableList<T> cloudData) {
        L localData = cloudToLocalMapper.apply(cloudData, syncMetaData.getCurrentTimeStamp());

        GluonObservableObject<L> obsLocalData = localRepo.createOrUpdateListAsync(localData);
        AsyncResultReceiver.create(obsLocalData)
                           .onSuccess(result ->
                           {
                               onSynced.accept(cloudData);
                               setSucceeded();
                               LOGGER.debug("stored local data {}: {}", listIdentifier, result);
                           })
                           .onException(ex ->
                           {
                               setFailed();
                               LOGGER.error("failed to store local data: {}", listIdentifier, ex);
                           })
                           .start();
    }

    protected void readChangeLogAndSyncLocalData() {
        AsyncResultReceiver.create(retrieveChangeLog(listIdentifier))
                           .onSuccess(this::syncLocalData)
                           .onException(ex ->
                           {
                               setFailed();
                               LOGGER.error("failed to sync local data: {}", listIdentifier, ex);
                           })
                           .start();
    }

    protected GluonObservableList<ChangeLogEntry> retrieveChangeLog(String listIdentifier) {
        return retrieveChangeLog(syncMetaData.getLastSynced(), listIdentifier);
    }

    protected GluonObservableList<ChangeLogEntry> retrieveChangeLog(long startAtTimeStamp, String listIdentifier) {
        return logRepo.readListAsync(startAtTimeStamp, listIdentifier);
    }

    protected ChangeLogEntry retrieveChangeLogEntry(String listIdentifier, String id) throws IOException {
        return logRepo.retrieveObject(listIdentifier, id);
    }

    protected GluonObservableObject<ChangeLogEntry> retrieveChangeLogEntryAsync(String listIdentifier, String id) {
        return logRepo.retrieveObjectAsync(listIdentifier, id);
    }

    protected abstract void syncLocalData(GluonObservableList<ChangeLogEntry> log);

}