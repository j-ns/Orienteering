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
package com.jns.orienteering.model.persisted;

import static com.jns.orienteering.util.Validations.isNullOrEmpty;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

public class Mission extends BasePostableSynchronizable implements CityAssignable, LookupSupplier, Comparable<Mission> {

    private String               missionName;
    private String               cityId;
    private String               ownerId;
    private AccessType           accessType = AccessType.PRIVATE;

    private Map<String, Integer> tasksMap;
    private Set<String>          taskIds;

    private double               distance;
    private int                  maxPoints;

    private Mission              previousMission;

    @JsonDefaultConstructor
    public Mission() {
    }

    public Mission(String missionName, String cityId, String ownerId, double distance, AccessType accessType) {
        this.missionName = missionName;
        this.cityId = cityId;
        this.ownerId = ownerId;
        this.distance = distance;
        this.accessType = accessType;
        tasksMap = new LinkedHashMap<>();
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

    @XmlTransient
    public String getPreviousName() {
        return previousMission.getMissionName();
    }

    @Override
    public boolean nameChanged() {
        Objects.requireNonNull(previousMission, "previousMission must not be null");
        return !missionName.equals(previousMission.missionName);
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
    public boolean cityChanged() {
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
    public boolean accessTypeChanged() {
        return previousMission != null && previousMission.accessType != accessType;
    }

    @XmlTransient
    public Map<String, Integer> getTasksMap() {
        if (tasksMap == null) {
            tasksMap = new LinkedHashMap<>();
        }
        return tasksMap;
    }

    public void updateTasksMap(List<Task> tasks) {
        tasksMap = new LinkedHashMap<>();

        if (!isNullOrEmpty(tasks)) {
            int idx = 0;
            for (Task task : tasks) {
                tasksMap.put(task.getId(), idx++);
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
            result = missionName.compareTo(other.missionName);
        }
        if (result == 0) {
            result = cityId.compareTo(other.cityId);
        }
        if (result == 0) {
            result = Double.compare(distance, other.distance);
        }
        return result == 0;
    }

    @Override
    public int compareTo(Mission other) {
        return missionName.compareTo(other.missionName);
    }

}
