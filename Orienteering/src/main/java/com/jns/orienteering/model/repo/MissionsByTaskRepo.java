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

import java.io.IOException;
import java.util.Set;

import com.jns.orienteering.model.persisted.Mission;
import com.jns.orienteering.model.persisted.MissionsByTaskLookup;
import com.jns.orienteering.model.persisted.TasksByMissionLookup;

public class MissionsByTaskRepo extends FireBaseRepo<MissionsByTaskLookup> {

    public MissionsByTaskRepo() {
        super(MissionsByTaskLookup.class, MISSIONS_BY_TASK);
    }

    public void createOrUpdateLookup(Mission mission) throws IOException {
        String missionId = mission.getId();
        Set<String> taskIds = mission.getTaskIds();

        for (String taskId : taskIds) {
            MissionsByTaskLookup existingLookup = retrieveObject(taskId);

            if (existingLookup != null) {
                existingLookup.addValue(missionId);
                createOrUpdate(existingLookup, taskId);

            } else {
                MissionsByTaskLookup newLookup = new MissionsByTaskLookup();
                newLookup.addValue(missionId);
                createOrUpdate(newLookup, taskId);
            }
        }
    }

    public void updateLookup(TasksByMissionLookup previousTasksLookup, Mission mission) throws IOException {
        removeDeletedTasks(previousTasksLookup, mission);
        createOrUpdateLookup(mission);
    }

    public void removeDeletedTasks(TasksByMissionLookup previousTasksLookup, Mission mission) throws IOException {
        if (previousTasksLookup == null) {
            return;
        }

        String missionId = mission.getId();
        Set<String> taskIds = mission.getTaskIds();
        Set<String> previousTaskIds = previousTasksLookup.getValues().keySet();
        previousTaskIds.removeAll(taskIds);

        if (!previousTaskIds.isEmpty()) {
            for (String taskIdToDelete : previousTaskIds) {
                MissionsByTaskLookup existingLookup = retrieveObject(taskIdToDelete);
                existingLookup.removeValue(missionId);
                createOrUpdate(existingLookup, taskIdToDelete);
            }
        }
    }

    public void deleteLookup(Mission mission) throws IOException {
        String missionId = mission.getId();
        for (String taskId : mission.getTaskIds()) {
            delete(taskId, MISSIONS, missionId);
        }

    }
}
