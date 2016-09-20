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
package com.jns.orienteering.model.persisted;

import static com.jns.orienteering.util.Validators.isNullOrEmpty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import com.jns.orienteering.model.common.AccessType;
import com.jns.orienteering.model.common.JsonDefaultConstructor;
import com.jns.orienteering.model.common.LookupSupplier;
import com.jns.orienteering.model.common.Postable;
import com.jns.orienteering.model.common.UpdatableListItem;

public class Mission extends BaseSynchronizable implements Postable, UpdatableListItem, LookupSupplier, Comparable<Mission> {

    private String               postId;

    private String               missionName;
    private String               cityId;
    private String               ownerId;
    private AccessType           accessType = AccessType.PRIVATE;

    private Map<String, Boolean> tasksMap;
    private Set<String>          taskIds;

    private double               distance;
    private int                  maxPoints;

    private Mission              previousMission;

    @JsonDefaultConstructor
    public Mission() {
    }

    public Mission(Mission mission) {
        this(mission.getMissionName(), mission.getCityId(), mission.getOwnerId(), mission.getDistance(), mission.getAccessType());
    }

    public Mission(String missionName, String cityId, String ownerId, double distance, AccessType accessType) {
        this(missionName, cityId, distance, accessType);
        this.ownerId = ownerId;
    }

    public Mission(String missionName, String cityId, double distance, AccessType accessType) {
        this.missionName = missionName;
        this.cityId = cityId;
        this.distance = distance;
        this.accessType = accessType;
        tasksMap = new HashMap<>();
    }

    public static Mission create(String missionName) {
        Mission mission = new Mission();
        mission.setMissionName(missionName);
        return mission;
    }

    public Mission cityId(String id) {
        cityId = id;
        return this;
    }

    public Mission ownerId(String id) {
        ownerId = id;
        return this;
    }

    public Mission distance(double distance) {
        this.distance = distance;
        return this;
    }

    public Mission accessType(AccessType access) {
        accessType = access;
        return this;
    }

    public void setPreviousMission(Mission mission) {
        previousMission = mission;
    }

    public String getMissionName() {
        return missionName;
    }

    public void setMissionName(String missionName) {
        this.missionName = missionName;
    }

    public boolean hasMissionNameChanged() {
        return previousMission != null && !missionName.equals(previousMission.missionName);
    }

    @XmlTransient
    public String getPreviousName() {
        return previousMission == null ? null : previousMission.getMissionName();
    }

    @Override
    public boolean hasNameChanged() {
        return !missionName.equals(previousMission.missionName);
    }

    @Override
    @XmlElement(name = "name")
    public String getPostId() {
        return postId;
    }

    @Override
    public void setPostId(String name) {
        postId = name;
    }

    @Override
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    @Override
    public String getPreviousCityId() {
        return previousMission == null ? null : previousMission.cityId;
    }

    @Override
    public boolean hasCityChanged() {
        return previousMission != null && !previousMission.cityId.equals(cityId);
    }

    @Override
    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    @Override
    public boolean hasAccessTypeChanged() {
        return previousMission != null && previousMission.accessType != accessType;
    }

    @XmlTransient
    public Map<String, Boolean> getTasksMap() {
        if (tasksMap == null) {
            tasksMap = new HashMap<>();
        }
        return tasksMap;
    }

    public void setTasksMap(Map<String, Boolean> tasksMap) {
        this.tasksMap = tasksMap;
    }

    public void updateTasksMap(List<Task> tasks) {
        tasksMap = new HashMap<>();
        if (!isNullOrEmpty(tasks)) {
            for (Task task : tasks) {
                tasksMap.put(task.getId(), true);
            }
        }
    }

    @XmlTransient
    public Set<String> getTaskIds() {
        if (taskIds == null) {
            taskIds = getTasksMap().keySet();
        }
        return taskIds;
    }

    public double getDistance() {
        return distance;
    }

    @XmlTransient
    public String getDistanceText() {
        return Double.toString(distance);
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(int points) {
        maxPoints = points;
    }

    @Override
    public MissionNameLookup createNameLookup() {
        return new MissionNameLookup(missionName, id);
    }

    @Override
    public MissionsByCityLookup createCityLookup() {
        return new MissionsByCityLookup(this);
    }

    public TasksByMissionLookup createTasksLookup() {
        return new TasksByMissionLookup(this);
    }

    public MissionsByTaskLookup createMissionsLookup() {
        return new MissionsByTaskLookup(this);
    }

    public TasksByMissionLookup createTasksByMissionLookup() {
        return new TasksByMissionLookup(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        Mission other = (Mission) obj;
        int result = id.compareTo(other.id);
        if (result == 0) {
            result = accessType.compareTo(other.accessType);
        }
        if (result == 0) {
            result = compareTo(other);
        }
        if (result == 0) {
            result = cityId.compareTo(other.cityId);
        }
        if (result == 0) {
            result = Integer.compare(maxPoints, other.maxPoints);
        }
        return false;
    }

    @Override
    public int compareTo(Mission other) {
        return missionName.compareTo(other.missionName);
    }

}
