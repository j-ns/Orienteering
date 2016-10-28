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

import static com.jns.orienteering.model.repo.BaseUrls.*;

import java.util.Iterator;

import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.GluonObservableObject;
import com.gluonhq.connect.provider.DataProvider;
import com.jns.orienteering.model.persisted.CityTaskLookup;
import com.jns.orienteering.model.persisted.ImageLogEntry;
import com.jns.orienteering.model.persisted.MissionStat;
import com.jns.orienteering.model.persisted.MissionsByTaskLookup;
import com.jns.orienteering.model.persisted.RepoAction;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.persisted.TaskNameLookup;
import com.jns.orienteering.model.persisted.TasksByMissionLookup;
import com.jns.orienteering.model.repo.readerwriter.RestMapReader;

public class TaskFBRepo extends FireBaseRepo<Task> {

    private NameLookupFBRepo<TaskNameLookup>           namelookupRepo     = new NameLookupFBRepo<>(TaskNameLookup.class, TASK_NAMES);

    private CityLookupFBRepo<CityTaskLookup, Task>     cityLookupRepo     =
            new CityLookupFBRepo<>(CityTaskLookup.class, Task.class, TASKS_BY_CITY, TASKS);

    private MultiValueLookupRepo<TasksByMissionLookup> tasksLookupRepo    =
            new MultiValueLookupRepo<>(TasksByMissionLookup.class, TASKS_BY_MISSION);

    private MissionsByTaskRepo                         missionsByTaskRepo = RepoService.INSTANCE.getCloudRepo(MissionsByTaskLookup.class);
    private MissionStatFBRepo                          missionStatRepo    = RepoService.INSTANCE.getCloudRepo(MissionStat.class);

    public TaskFBRepo() {
        super(Task.class, TASKS);
    }

    public GluonObservableList<Task> getPrivateTasksAsync(String cityId, String userId) {
        return cityLookupRepo.getPrivateListAsync(cityId, userId);
    }

    public GluonObservableList<Task> getPublicTasksAsync(String cityId) {
        return cityLookupRepo.getPublicListAsync(cityId);
    }

    public GluonObservableList<Task> retrieveTasksAsync(String missionId) {
        String sourceUrl = buildPath(TASKS_BY_MISSION, missionId);
        return DataProvider.retrieveList(new RestMapReader<>(createRestClient(), TasksByMissionLookup.class, sourceUrl, Task.class, TASKS));
    }

    public boolean checkIfTaskNameExists(String name) {
        return namelookupRepo.checkIfNameExists(name);
    }

    public GluonObservableObject<Task> createTaskAsync(Task task) {
        return executeAsync(task, () ->
        {
            task.setTimeStamp(createTimeStamp());
            Task result = addToList(task);

            if (result != null) {
                writeLogEntry(result, RepoAction.ADD);

                namelookupRepo.createOrUpdate(result.createNameLookup());
                cityLookupRepo.createOrUpdate(result.createCityLookup());
            }
        });
    }

    public GluonObservableObject<Task> updateTaskAsync(Task task, String previousImageId) {
        return executeAsync(task, () ->
        {
            task.setTimeStamp(createTimeStamp());
            createOrUpdate(task, task.getId());
            writeLogEntry(task, RepoAction.UPDATE);

            if (task.nameChanged()) {
                namelookupRepo.recreateLookup(task.createNameLookup(), task.getPreviousTask().getTaskName());
            }
            if (task.cityChanged() || task.accessTypeChanged()) {
                cityLookupRepo.recreateCityLookup(new CityTaskLookup(task));
            }
            // todo: update mission, when task changed

            // if (task.locationChanged) {
            // MissionsByTaskLookup missionsLookup = missionsLookupRepo.retrieveObject(task.getId());
            // if (missionsLookup != null) {
            // Iterator<String> missionIds = missionsLookup.getValues().keySet().iterator();
            // while (missionIds.hasNext()) {
            // String missionId = missionIds.next();
            // missionStatCloudRepo.deleteAsync(missionId);
            // }
            // }
            // }

            if (previousImageId != null) {
                getChangeLogRepo().writeImageLogAsync(new ImageLogEntry(previousImageId, task.getTimeStamp()));
            }
        });
    }

    public GluonObservableObject<Task> deleteTaskAsync(Task task) {
        return executeAsync(task, () ->
        {
            task.setTimeStamp(createTimeStamp());
            delete(task.getId());
            writeLogEntry(task, RepoAction.DELETE);

            namelookupRepo.deleteLookup(task.createNameLookup());
            cityLookupRepo.deleteLookup(task.createCityLookup());

            MissionsByTaskLookup missionsLookup = missionsByTaskRepo.retrieveObject(task.getId());
            if (missionsLookup != null) {
                Iterator<String> missionIds = missionsLookup.getValues().keySet().iterator();
                while (missionIds.hasNext()) {
                    String missionId = missionIds.next();

                    TasksByMissionLookup tasksLookup = tasksLookupRepo.retrieveObject(missionId);
                    if (tasksLookup != null) {
                        tasksLookup.setId(missionId);
                        tasksLookup.removeValue(task.getId());
                        tasksLookupRepo.createOrUpdateLookup(tasksLookup);
                    }

                    missionStatRepo.deleteAsync(missionId);
                }
                missionsByTaskRepo.delete(task.getId());
            }

            if (task.getImageId() != null) {
                getChangeLogRepo().writeImageLogAsync(new ImageLogEntry(task));
            }
        });
    }

    private void writeLogEntry(Task task, RepoAction action) {
        getChangeLogRepo().writeLogAsync(task, action, TASKS);
    }

}
