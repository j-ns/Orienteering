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
package com.jns.orienteering.model.dynamic;

import static com.jns.orienteering.util.Validations.isNullOrEmpty;

import java.util.List;

import com.gluonhq.connect.GluonObservableList;
import com.jns.orienteering.model.common.GluonObservables;
import com.jns.orienteering.model.persisted.Mission;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.repo.MissionFBRepo;
import com.jns.orienteering.model.repo.RepoService;

public class MissionCache extends ModelCache<Mission> {

    public static final MissionCache  INSTANCE = new MissionCache();

    private MissionFBRepo             cloudRepo;

    private String                    activeMissionId;
    private GluonObservableList<Task> activeMissionTasks;

    private String                    selectedMissionId;
    private GluonObservableList<Task> missionTasks;
    private GluonObservableList<Task> missionTasksTemp;

    private MissionCache() {
        cloudRepo = RepoService.INSTANCE.getCloudRepo(Mission.class);
    }

    @Override
    protected GluonObservableList<Mission> retrievePrivateItems(String cityId, String userId) {
        return cloudRepo.getPrivateMissions(cityId, userId);
    }

    @Override
    protected GluonObservableList<Mission> retrievePublicItems(String cityId) {
        return cloudRepo.getPublicMissions(cityId);
    }

    public GluonObservableList<Task> retrieveActiveMissionTasksOrdered(String missionId) {
        if (!missionId.equals(activeMissionId)) {
            activeMissionTasks = null;
        }
        if (!isNullOrEmpty(activeMissionTasks)) {
            return activeMissionTasks;
        }
        activeMissionTasks = cloudRepo.retrieveTasksOrderedAsync(missionId);
        activeMissionId = missionId;
        return activeMissionTasks;
    }

    public GluonObservableList<Task> retrieveMissionTasksOrdered(String missionId) {
        if (missionId.equals(activeMissionId)) {
            missionTasks = GluonObservables.newListInitialized(activeMissionTasks);
            selectedMissionId = missionId;

        } else if (!missionId.equals(selectedMissionId)) {
            clearTasks();
        }

        if (!isNullOrEmpty(missionTasks)) {
            missionTasksTemp = GluonObservables.newListInitialized(missionTasks);
            return missionTasks;
        }

        missionTasksTemp = null;
        missionTasks = cloudRepo.retrieveTasksOrderedAsync(missionId);
        selectedMissionId = missionId;
        return missionTasks;
    }

    public GluonObservableList<Task> getActiveMissionTasks() {
        return activeMissionTasks;
    }

    public void setActiveMissionTasks(List<Task> tasks, String activeMissionId) {
        for (int idx = 0; idx < tasks.size(); idx++) {
            tasks.get(idx).setOrderNumber(idx);
        }

        activeMissionTasks = GluonObservables.newListInitialized(tasks);
        this.activeMissionId = activeMissionId;
    }

    public GluonObservableList<Task> getMissionTasks() {
        return missionTasks;
    }

    public GluonObservableList<Task> getMissionTasksTemp() {
        if (missionTasksTemp == null) {
            missionTasksTemp = GluonObservables.newListInitialized(missionTasks);
        }
        return missionTasksTemp;
    }

    public void removeMissionAndTasks(Mission mission) {
        removeItem(mission);

        if (mission.getId().equals(selectedMissionId)) {
            selectedMissionId = null;
            clearTasks();
        }

        if (mission.getId().equals(activeMissionId)) {
            activeMissionId = null;
            clearActiveMissionTasks();
        }
    }

    public void udpateMissionTask(Task newTask, Task previousTask) {
        int idx = missionTasks.indexOf(previousTask);
        if (idx > -1) {
            missionTasks.set(idx, newTask);
        }

        int idxTemp = missionTasksTemp.indexOf(previousTask);
        if (idxTemp > -1) {
            missionTasksTemp.set(idxTemp, newTask);
        }

        if (!isNullOrEmpty(activeMissionTasks)) {
            int idxActiveTasks = activeMissionTasks.indexOf(previousTask);
            if (idxActiveTasks > -1) {
                activeMissionTasks.set(idxActiveTasks, newTask);
            }
        }
    }

    public void updateTasksWithBuffer() {
        missionTasks.setAll(missionTasksTemp);
        if (selectedMissionId.equals(activeMissionId)) {
            activeMissionTasks.setAll(missionTasksTemp);
        }
    }

    public void removeTask(Task task) {
        if (missionTasks != null) {
            missionTasks.remove(task);
        }
        if (missionTasksTemp != null) {
            missionTasksTemp.remove(task);
        }
        if (activeMissionTasks != null) {
            activeMissionTasks.remove(task);
        }
    }

    public boolean containsActiveTask(Task task) {
        if (isNullOrEmpty(activeMissionTasks)) {
            return false;
        }
        return activeMissionTasks.contains(task);
    }

    @Override
    public void clearItems() {
        super.clearPrivateItems();
        super.clearPublicItems();
        clearTasks();
    }

    @Override
    protected void clearPrivateItems() {
        super.clearPrivateItems();
        clearTasks();
    }

    @Override
    protected void clearPublicItems() {
        super.clearPublicItems();
        clearTasks();
    }

    public void clearTasks() {
        missionTasks = GluonObservables.newListInitialized();
        missionTasksTemp = GluonObservables.newListInitialized();
    }

    public void clearMissionTasksTemp() {
        missionTasksTemp = GluonObservables.newListInitialized();
    }

    public void clearActiveMissionTasks() {
        activeMissionTasks = GluonObservables.newListInitialized();
    }

    public boolean isInitialized() {
        return activeMissionTasks != null;
    }
}
