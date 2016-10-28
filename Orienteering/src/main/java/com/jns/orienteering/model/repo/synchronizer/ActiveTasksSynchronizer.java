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

import static com.jns.orienteering.control.Dialogs.showError;
import static com.jns.orienteering.locale.Localization.localize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.connect.GluonObservableList;
import com.jns.orienteering.model.dynamic.MissionCache;
import com.jns.orienteering.model.persisted.ActiveTaskList;
import com.jns.orienteering.model.persisted.ChangeLogEntry;
import com.jns.orienteering.model.persisted.Mission;
import com.jns.orienteering.model.persisted.RepoAction;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.LocalRepo;
import com.jns.orienteering.model.repo.MissionFBRepo;
import com.jns.orienteering.model.repo.RepoService;
import com.jns.orienteering.model.repo.TaskFBRepo;
import com.jns.orienteering.util.GluonObservables;
import com.jns.orienteering.util.MapUtils;

import javafx.collections.ObservableList;

public class ActiveTasksSynchronizer extends BaseSynchronizer<Task, ActiveTaskList> {

    private static final Logger                              LOGGER               = LoggerFactory.getLogger(ActiveTasksSynchronizer.class);

    public static final String                               NAME                 = "active_tasks_synchronizer";
    private static final String                              TASK_LIST_IDENTIFIER = "tasks";

    private static final GluonObservableList<ChangeLogEntry> NO_LOG               = null;

    public ActiveTasksSynchronizer(TaskFBRepo cloudRepo, LocalRepo<Task, ActiveTaskList> localRepo) {
        super(cloudRepo, localRepo, ActiveTaskList::new, TASK_LIST_IDENTIFIER);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void syncNow(SyncMetaData syncMetaData) {
        setRunning();
        setSyncMetaData(syncMetaData);

        if (getSyncMetaData().isCompleteRefreshNeeded()) {
            retrieveCloudDataAndStoreLocally();
            return;
        }

        boolean fileExists = localRepo.fileExists();
        if (!fileExists) {
            if (getSyncMetaData().getActiveMission() == null) {
                setSucceeded();
            } else {
                retrieveCloudDataAndStoreLocally();
            }
        } else {
            if (getSyncMetaData().getActiveMission() == null) {
                localRepo.deleteAsync();
                setSucceeded();
            } else {
                syncLocalData(NO_LOG);
            }
        }
    }

    @Override
    protected void retrieveCloudDataAndStoreLocally() {
        MissionFBRepo missionCloudRepo = RepoService.INSTANCE.getCloudRepo(Mission.class);
        String missionId = getSyncMetaData().getActiveMission().getId();

        GluonObservableList<Task> obsTasks = missionCloudRepo.retrieveTasksOrderedAsync(missionId);
        AsyncResultReceiver.create(obsTasks)
                           .onSuccess(result ->
                           {
                               MissionCache.INSTANCE.setActiveMissionTasks(result, missionId);
                               storeLocally(result);
                               setSucceeded();
                           })
                           .onException(this::setFailed)
                           .start();
    }

    @Override
    protected void syncLocalData(ObservableList<ChangeLogEntry> log) {
        GluonObservableList<Task> obsLocalTasks = localRepo.retrieveListAsync(TASK_LIST_IDENTIFIER);
        AsyncResultReceiver.create(obsLocalTasks)
                           .onSuccess(this::startSyncResultReceiver)
                           .onException(this::setFailed)
                           .start();
    }

    private void startSyncResultReceiver(GluonObservableList<Task> localTasks) {
        AsyncResultReceiver.create(getObsSyncResult(localTasks))
                           .onSuccess(syncResult ->
                           {
                               if (!syncResult.isEmpty()) {
                                   MissionCache.INSTANCE.setActiveMissionTasks(syncResult, getSyncMetaData().getActiveMission().getId());
                                   localRepo.createOrUpdateListAsync(new ActiveTaskList(syncResult, getSyncMetaData().getCurrentTimeStamp()));
                               }
                               setSucceeded();
                           })
                           .onException(this::setFailed)
                           .start();
    }

    // todo: use executor
    private GluonObservableList<Task> getObsSyncResult(GluonObservableList<Task> localTasks) {
        GluonObservableList<Task> obsTasksResult = new GluonObservableList<>();
        boolean localDataNeedsUpdate = false;

        Map<String, Task> localTasksMap = MapUtils.createMap(LinkedHashMap::new, localTasks, Task::getId, task -> task);

        for (Task localTask : localTasks) {
            try {
                String taskId = localTask.getId();
                ChangeLogEntry logEntry = retrieveChangeLogEntry(TASK_LIST_IDENTIFIER, taskId);

                if (logEntry == null) {
                    continue;
                }

                if (logEntry.getAction() == RepoAction.DELETE) {
                    localTasksMap.remove(taskId);
                    localDataNeedsUpdate = true;

                } else {
                    if (localTask.getTimeStamp() < logEntry.getTimeStamp()) {
                        Task cloudTask = cloudRepo.retrieveObject(taskId);
                        localTasksMap.put(taskId, cloudTask);
                        localDataNeedsUpdate = true;

                        LOGGER.debug("task added/updated: {}, lastSynced: {}", cloudTask.getTaskName(), cloudTask
                                                                                                                 .getTimeStamp());
                    }
                }

            } catch (IOException ex) {
                setFailed();
                showError(localize("changeLog.error.readLog"));
                GluonObservables.setException(obsTasksResult, ex);
                LOGGER.error("Failed to read changeLog for active tasks", ex);
                break;
            }
        }

        if (localDataNeedsUpdate) {
            List<Task> updatedTasks = new ArrayList<>(localTasksMap.values());

            MissionCache.INSTANCE.setActiveMissionTasks(updatedTasks, getSyncMetaData().getActiveMission().getId());
            localRepo.createOrUpdateListAsync(new ActiveTaskList(updatedTasks, getSyncMetaData().getCurrentTimeStamp()));

            GluonObservables.setInitialized(obsTasksResult, updatedTasks, true);

        } else {
            GluonObservables.setInitialized(obsTasksResult, true);
        }

        return obsTasksResult;
    }

}
