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
import com.jns.orienteering.model.persisted.ChangeLogEntry;
import com.jns.orienteering.model.persisted.Synchronizable;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.ChangeLogRepo;
import com.jns.orienteering.model.repo.FireBaseRepo;
import com.jns.orienteering.model.repo.LocalRepo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

/**
 * @param <S>
 *            the type of the synchronizable object
 * @param <L>
 *            the type of the locally stored object
 */
public abstract class BaseSynchronizer<S extends Synchronizable, L> {

    private static final Logger            LOGGER          = LoggerFactory.getLogger(BaseSynchronizer.class);

    private static final ChangeLogRepo     CHANGE_LOG_REPO = ChangeLogRepo.getInstance();

    protected FireBaseRepo<S>              cloudRepo;
    protected LocalRepo<S, L>              localRepo;
    protected String                       listIdentifier;

    protected BiFunction<List<S>, Long, L> cloudToLocalMapper;

    private SyncMetaData                   syncMetaData;
    private ObjectProperty<ConnectState>   syncState       = new SimpleObjectProperty<>(ConnectState.READY);
    private Consumer<ObservableList<S>>    onSynced        = e ->
                                                           {
                                                           };

    public BaseSynchronizer(String listIdentifier) {
        this.listIdentifier = listIdentifier;
    }

    public BaseSynchronizer(FireBaseRepo<S> cloudRepo, LocalRepo<S, L> localRepo) {
        this(cloudRepo, localRepo, null, null);
    }

    public BaseSynchronizer(FireBaseRepo<S> cloudRepo, LocalRepo<S, L> localRepo, BiFunction<List<S>, Long, L> cloudToLocalMapper,
                            String listIdentifier) {
        this.cloudRepo = cloudRepo;
        this.localRepo = localRepo;
        this.cloudToLocalMapper = cloudToLocalMapper;
        this.listIdentifier = listIdentifier;
    }

    public abstract String getName();

    public final ReadOnlyObjectProperty<ConnectState> syncStateProperty() {
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

    public Consumer<ObservableList<S>> getOnSynced() {
        return onSynced;
    }

    public void setOnSynced(Consumer<ObservableList<S>> onSynced) {
        this.onSynced = onSynced;
    }

    protected SyncMetaData getSyncMetaData() {
        return syncMetaData;
    }

    protected void setSyncMetaData(SyncMetaData syncMetaData) {
        this.syncMetaData = syncMetaData;
    }

    public abstract void syncNow(SyncMetaData syncMetaData);

    protected void retrieveCloudDataAndStoreLocally() {
        AsyncResultReceiver.create(cloudRepo.retrieveListAsync())
                           .onSuccess(this::storeLocally)
                           .onException(this::setFailed)
                           .start();
    }

    protected void storeLocally(ObservableList<S> cloudData) {
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
        AsyncResultReceiver.create(retrieveChangeLogAsync(syncMetaData.getLastSynced(), listIdentifier))
                           .onSuccess(this::syncLocalData)
                           .onException(ex ->
                           {
                               setFailed();
                               LOGGER.error("failed to sync local data: {}", listIdentifier, ex);
                           })
                           .start();
    }

    protected GluonObservableList<ChangeLogEntry> retrieveChangeLogAsync(long startAtTimeStamp, String listIdentifier) {
        return CHANGE_LOG_REPO.readListAsync(startAtTimeStamp, listIdentifier);
    }

    protected ChangeLogEntry retrieveChangeLogEntry(String listIdentifier, String id) throws IOException {
        return CHANGE_LOG_REPO.retrieveObject(listIdentifier, id);
    }

    protected GluonObservableObject<ChangeLogEntry> retrieveChangeLogEntryAsync(String listIdentifier, String id) {
        return CHANGE_LOG_REPO.retrieveObjectAsync(listIdentifier, id);
    }

    protected abstract void syncLocalData(ObservableList<ChangeLogEntry> log);

}