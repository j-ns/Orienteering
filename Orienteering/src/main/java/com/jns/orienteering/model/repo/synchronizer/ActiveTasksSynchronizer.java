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

import static com.jns.orienteering.locale.Localization.localize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.connect.GluonObservableList;
import com.jns.orienteering.model.common.RepoAction;
import com.jns.orienteering.model.persisted.ActiveTaskList;
import com.jns.orienteering.model.persisted.ChangeLogEntry;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.LocalRepo;
import com.jns.orienteering.model.repo.TaskFBRepo;
import com.jns.orienteering.util.Dialogs;

import javafx.collections.FXCollections;

public class ActiveTasksSynchronizer extends BaseSynchronizer<Task, Task, ActiveTaskList> {

    private static final Logger LOGGER               = LoggerFactory.getLogger(ActiveTasksSynchronizer.class);

    public static final String  NAME                 = "active_tasks_synchronizer";
    private static final String TASK_LIST_IDENTIFIER = "tasks";

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
        syncActiveTasks();
    }

    private void syncActiveTasks() {
        boolean fileExists = localRepo.fileExists();

        if (!fileExists) {
            if (getSyncMetaData().getActiveMission() == null) {
                getOnSynced().accept(FXCollections.observableArrayList());
                setSucceeded();
            } else {
                retrieveCloudDataAndStoreLocally();
            }
        } else {
            readChangeLogAndSyncLocalData();
        }
    }

    @Override
    protected void retrieveCloudDataAndStoreLocally() {
        GluonObservableList<Task> obsTasks = ((TaskFBRepo) cloudRepo).retrieveTasksAsync(getSyncMetaData().getActiveMission().getId());

        AsyncResultReceiver.create(obsTasks)
                           .onSuccess(this::storeLocally)
                           .onException(this::setFailed)
                           .start();
    }

    @Override
    protected void readChangeLogAndSyncLocalData() {
        GluonObservableList<Task> obsLocalTasks = localRepo.retrieveListAsync(TASK_LIST_IDENTIFIER);
        AsyncResultReceiver.create(obsLocalTasks)
                           .onSuccess(result ->
                           {
                               // async?
                               List<Task> localTasksCopy = new ArrayList<>(result);

                               boolean localDataNeedsUpdate = false;
                               for (Task localTask : result) {
                                   try {
                                       String taskId = localTask.getId();
                                       ChangeLogEntry logEntry = retrieveChangeLogEntry(TASK_LIST_IDENTIFIER, taskId);

                                       if (logEntry == null) {
                                           continue;
                                       }
                                       if (logEntry.getId() != null) {
                                           if (logEntry.getAction() == RepoAction.DELETE) {
                                               if (removeTask(localTasksCopy, taskId)) {
                                                   localDataNeedsUpdate = true;
                                               }
                                           } else {
                                               if (localTask.getTimeStamp() < logEntry.getTimeStamp()) {
                                                   if (logEntry.getAction() == RepoAction.UPDATE) {
                                                       removeTask(localTasksCopy, taskId);
                                                   }
                                                   Task taskFromCloud = cloudRepo.retrieveObject(taskId);
                                                   localTasksCopy.add(taskFromCloud);
                                                   localDataNeedsUpdate = true;
                                                   LOGGER.debug("task added/updated: {}, lastSynced: {}", taskFromCloud.getTaskName(), taskFromCloud
                                                                                                                                                    .getTimeStamp());
                                               }
                                           }
                                       }

                                   } catch (IOException e) {
                                       LOGGER.error("Failed to read changeLog for active tasks", e);
                                       Dialogs.ok(localize("changeLog.error.readLog")).showAndWait();
                                       setFailed();
                                       return;
                                   }
                               }
                               if (localDataNeedsUpdate) {
                                   localRepo.createOrUpdateListAsync(new ActiveTaskList(localTasksCopy, getSyncMetaData().getCurrentTimeStamp()));
                               }
                               getOnSynced().accept(localTasksCopy);
                               setSucceeded();
                           })
                           .onException(this::setFailed)
                           .start();
    }

    private boolean removeTask(List<Task> localTasksCopy, String taskId) {
        Iterator<Task> it = localTasksCopy.iterator();
        boolean removed = false;

        while (it.hasNext()) {
            Task task = it.next();
            if (task.getId() == taskId) {
                it.remove();
                LOGGER.debug("removed activeTask: {}, lastSynced: {}", task.getTaskName(), task.getTimeStamp());
                removed = true;
            }
        }
        return removed;
    }

    @Override
    protected void syncLocalData(GluonObservableList<ChangeLogEntry> log) {
        // no op
    }

}
