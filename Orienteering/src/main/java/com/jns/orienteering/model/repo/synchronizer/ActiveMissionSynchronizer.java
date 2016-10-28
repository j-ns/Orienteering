/*
* Copyright (c) 2016, Jens Stroh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL JENS STROH BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jns.orienteering.model.repo.synchronizer;

import static com.jns.orienteering.control.Dialogs.showInfo;
import static com.jns.orienteering.locale.Localization.localize;

import com.gluonhq.connect.ConnectState;
import com.gluonhq.connect.GluonObservable;
import com.gluonhq.connect.GluonObservableObject;
import com.jns.orienteering.common.BaseService;
import com.jns.orienteering.model.persisted.AccessType;
import com.jns.orienteering.model.persisted.ChangeLogEntry;
import com.jns.orienteering.model.persisted.Mission;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.persisted.User;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.FireBaseRepo;
import com.jns.orienteering.model.repo.RepoService;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ActiveMissionSynchronizer extends BaseSynchronizer<User, User> {

    public static final String      NAME                     = "mission_synchronizer";
    private static final String     MISSIONS_LIST_IDENTIFIER = "missions";

    private ActiveTasksSynchronizer activeTasksSynchronizer;

    private BaseService             service;

    public ActiveMissionSynchronizer(BaseService service) {
        super(service.getRepoService().getCloudRepo(User.class), service.getRepoService().getLocalRepo(User.class));
        this.service = service;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void syncNow(SyncMetaData syncMetaData) {
        setRunning();
        setSyncMetaData(syncMetaData);

        User user = service.getUser();
        if (user == null) {
            setSucceeded();
            return;
        }

        Mission activeMission = user.getActiveMission();
        if (activeMission == null) {
            setSucceeded();
            return;
        }
        initActiveTasksSynchronizer(service);

        if (syncMetaData.isCompleteRefreshNeeded() || !localRepo.fileExists()) {
            retrieveCloudDataAndStoreLocally();
            return;
        }

        readChangeLogAndSyncLocalData();
    }

    @Override
    protected void retrieveCloudDataAndStoreLocally() {
        Mission activeMission = getSyncMetaData().getActiveMission();
        GluonObservableObject<User> obsActiveMission = cloudRepo.retrieveObjectAsync(activeMission.getId());
        AsyncResultReceiver.create(obsActiveMission)
                           .onSuccess(result ->
                           {
                               Mission _activeMission = result.get() == null ? null : result.get().getActiveMission();
                               service.setActiveMission(_activeMission);
                               activeTasksSynchronizer.syncNow(getSyncMetaData());
                           })
                           .onException(this::setFailed)
                           .start();
    }

    @Override
    protected void readChangeLogAndSyncLocalData() {
        GluonObservableObject<ChangeLogEntry> obsMissionLogEntry = retrieveChangeLogEntryAsync(MISSIONS_LIST_IDENTIFIER, getSyncMetaData()
                                                                                                                                          .getActiveMission()
                                                                                                                                          .getId());
        AsyncResultReceiver.create(obsMissionLogEntry)
                           .onSuccess(result ->
                           {
                               if (result.get() != null) {
                                   syncLocalData(FXCollections.singletonObservableList(result.get()));
                               } else {
                                   setSucceeded();
                               }
                           })
                           .onException(this::setFailed)
                           .start();
    }

    @Override
    protected void syncLocalData(ObservableList<ChangeLogEntry> log) {
        ChangeLogEntry logEntry = log.get(0);
        if (logEntry.getTimeStamp() < getSyncMetaData().getLastSynced()) {
            activeTasksSynchronizer.syncNow(getSyncMetaData());
            return;
        }

        switch (logEntry.getAction()) {
            case DELETE:
                service.setActiveMission(null);
                service.updateActiveTasks(null);
                setSucceeded();

                showInfo(localize("missionSynchronizer.info.activeMissionDeletedByOwner"));
                break;

            case UPDATE:
                FireBaseRepo<Mission> missionCloudRepo = service.getRepoService().getCloudRepo(Mission.class);

                GluonObservableObject<Mission> obsActiveMission = missionCloudRepo.retrieveObjectAsync(logEntry.getTargetId());
                AsyncResultReceiver.create(obsActiveMission)
                                   .onSuccess(resultActiveMission ->
                                   {
                                       Mission activeMission = resultActiveMission.get();
                                       String userId = getSyncMetaData().getUserId();
                                       if (activeMission.getAccessType() == AccessType.PRIVATE && !activeMission.getOwnerId()
                                                                                                                .equals(userId)) {
                                           service.setActiveMission(null);
                                           service.updateActiveTasks(null);
                                           setSucceeded();

                                       } else {
                                           if (!service.getActiveMission().equals(activeMission)) {
                                               service.setActiveMission(activeMission);
                                           }
                                           // todo: if tasks changed -> clear localTasks
                                           GluonObservable obsDelete = RepoService.INSTANCE.getLocalRepo(Task.class).deleteAsync();
                                           AsyncResultReceiver.create(obsDelete)
                                                              .onSuccess(e -> activeTasksSynchronizer.syncNow(getSyncMetaData()))
                                                              .onException(this::setFailed)
                                                              .start();
                                       }
                                   })
                                   .onException(this::setFailed)
                                   .start();
                break;

            default:
                break;
        }

    }

    private void initActiveTasksSynchronizer(BaseService service) {
        activeTasksSynchronizer = new ActiveTasksSynchronizer(service.getRepoService().getCloudRepo(Task.class),
                                                              service.getRepoService().getLocalRepo(Task.class));

        activeTasksSynchronizer.syncStateProperty().addListener((obsValue, st, st1) ->
        {
            if (st1 != ConnectState.RUNNING) {
                ((SimpleObjectProperty<ConnectState>) syncStateProperty()).set(st1);
            }
        });
    }

}
