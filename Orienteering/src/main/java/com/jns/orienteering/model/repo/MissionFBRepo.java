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

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.provider.DataProvider;
import com.jns.orienteering.model.common.RepoAction;
import com.jns.orienteering.model.persisted.Mission;
import com.jns.orienteering.model.persisted.MissionNameLookup;
import com.jns.orienteering.model.persisted.MissionStat;
import com.jns.orienteering.model.persisted.MissionsByCityLookup;
import com.jns.orienteering.model.persisted.MissionsByTaskLookup;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.persisted.TasksByMissionLookup;
import com.jns.orienteering.model.repo.readerwriter.RestMapReader;

public class MissionFBRepo extends FireBaseRepo<Mission> {

    private static final Logger                             LOGGER                   = LoggerFactory.getLogger(MissionFBRepo.class);

    private static final String                             MISSIONS                 = "missions";
    private static final String                             MISSION_NAMES            = "mission_names";
    private static final String                             MISSIONS_BY_CITY         = "missions_by_city";

    private static final String                             TASKS                    = "tasks";
    private static final String                             TASKS_BY_MISSION         = "tasks_by_mission";

    private NameLookupFBRepo<MissionNameLookup>             nameLookupRepo           = new NameLookupFBRepo<>(MissionNameLookup.class, MISSION_NAMES);

    private CityLookupFBRepo<MissionsByCityLookup, Mission> cityLookupRepo           =
            new CityLookupFBRepo<>(MissionsByCityLookup.class, Mission.class, MISSIONS_BY_CITY, MISSIONS);

    private MultiValueLookupRepo<TasksByMissionLookup>      tasksLookupRepo          =
            new MultiValueLookupRepo<>(TasksByMissionLookup.class, TASKS_BY_MISSION);

    private MissionsByTaskRepo                              missionsByTaskLookupRepo = RepoService.INSTANCE.getCloudRepo(MissionsByTaskLookup.class);
    private MissionStatFBRepo                               missionStatCloudRepo     = RepoService.INSTANCE.getCloudRepo(MissionStat.class);

    public MissionFBRepo() {
        super(Mission.class, MISSIONS);
    }

    public GluonObservableList<Mission> getPrivateMissions(String cityId, String userId) {
        return cityLookupRepo.getPrivateListAsync(cityId, userId);
    }

    public GluonObservableList<Mission> getPublicMissions(String cityId) {
        return cityLookupRepo.getPublicListAsync(cityId);
    }

    public boolean checkIfMissionNameDoesntExist(String name) {
        return !nameLookupRepo.checkIfNameExists(name);
    }

    public boolean missionContainsTasks(String missionId) {
        return tasksLookupRepo.checkIfUrlExists(TASKS_BY_MISSION, missionId);
    }

    public GluonObservableList<Task> retrieveTasksAsync(String missionId) {
        String sourceUrl = buildPath(TASKS_BY_MISSION, missionId);
        return DataProvider.retrieveList(new RestMapReader<>(createRestClient(), TasksByMissionLookup.class, sourceUrl, Task.class, TASKS));
    }

    public void createMission(Mission mission) throws IOException {
        mission.setTimeStamp(createTimeStamp());

        Mission result = addToList(mission);
        if (result != null) {
            try {
                nameLookupRepo.createOrUpdate(mission.createNameLookup());
                cityLookupRepo.createOrUpdate(mission.createCityLookup());
                tasksLookupRepo.createOrUpdateLookup(mission.createTasksLookup());
                missionsByTaskLookupRepo.createOrUpdateLookup(mission);

            } catch (IOException e) {
                LOGGER.error("Failed to store mission:. '{}'", mission.getMissionName(), e);
                throw e;
            }
        }
    }

    public void updateMission(Mission mission, Mission previousMission, List<Task> tasks, List<Task> tasksBuffer) throws IOException {
        mission.setTimeStamp(createTimeStamp());

        createOrUpdate(mission, mission.getId());

        boolean tasksChanged = !tasksBuffer.containsAll(tasks) || !tasks.containsAll(tasksBuffer);
        if (tasksChanged) {
            TasksByMissionLookup previousTasksByMission = tasksLookupRepo.retrieveObject(mission.getId());

            tasksLookupRepo.createOrUpdateLookup(new TasksByMissionLookup(mission.getId(), mission.getTasksMap()));
            missionsByTaskLookupRepo.updateLookup(previousTasksByMission, mission);
            missionStatCloudRepo.deleteAsync(mission.getId());
        }

        if (mission.hasMissionNameChanged()) {
            nameLookupRepo.recreateLookup(previousMission.getMissionName(), mission.createNameLookup());
        }
        if (mission.hasCityChanged() || mission.hasAccessTypeChanged()) {
            cityLookupRepo.recreateCityLookup(new MissionsByCityLookup(mission));
        }

        writeLogEntry(mission, RepoAction.UPDATE);
    }

    public void deleteMission(Mission mission) throws IOException {
        mission.setTimeStamp(createTimeStamp());

        delete(mission.getId());

        nameLookupRepo.deleteLookup(mission.createNameLookup());
        cityLookupRepo.deleteLookup(mission.createCityLookup());
        tasksLookupRepo.deleteLookup(mission.createTasksByMissionLookup());
        missionsByTaskLookupRepo.deleteLookup(mission);
        missionStatCloudRepo.deleteAsync(mission.getId());

        writeLogEntry(mission, RepoAction.DELETE);
    }

    private void writeLogEntry(Mission mission, RepoAction action) {
        mission.setRepoAction(action);
        writeLogEntry(mission, ChangeLogRepo::writeMissionLogAsync);
    }

}
