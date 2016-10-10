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

import java.time.LocalDate;
import java.time.LocalTime;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import com.jns.orienteering.model.common.JsonDefaultConstructor;
import com.jns.orienteering.model.common.Postable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MissionStat extends Stat implements Postable {

    private String                   postId;

    private Mission                  mission;
    private String                   missionId;
    private String                   missionName;
    private boolean                  missionFinished;
    private long                     timeStamp;
    private String                   userId;
    private ObservableList<TaskStat> taskStats = FXCollections.observableArrayList();

    @JsonDefaultConstructor
    public MissionStat() {
    }

    public MissionStat(Mission mission, String userId, int start) {
        this.mission = mission;
        missionId = mission.getId();
        missionName = mission.getMissionName();
        this.userId = userId;
        timeStamp = LocalDate.now().toEpochDay();
        setStart(start);
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

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    public Mission getMission() {
        return mission;
    }

    public String getMissionName() {
        return missionName;
    }

    public void setMissionName(String missionName) {
        this.missionName = missionName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }


    public void addTaskStat(TaskStat stat) {
        taskStats.add(stat);
        addPoints(stat.getPoints());
    }

    public ObservableList<TaskStat> getTaskStats() {
        return taskStats;
    }

    public ObservableList<TaskStat> getTaskStatsWithSummary() {
        ObservableList<TaskStat> stats = FXCollections.observableArrayList(taskStats);
        stats.add(getSummary());
        return stats;
    }

    public void setTaskStats(ObservableList<TaskStat> taskStats) {
        this.taskStats = taskStats;
    }

    @Override
    public int getEnd() {
        if (taskStats.isEmpty()) {
            return -1;
        }
        return taskStats.get(taskStats.size() - 1).getEnd();
    }

    public int getMaxPoints() {
        int points = 0;
        for (TaskStat stats : taskStats) {
            points += stats.getTask().getPoints();
        }
        return points;
    }

    @Override
    @XmlTransient
    public TrackData getTrackData() {
        TrackData trackData = new TrackData();
        for (TaskStat stats : taskStats) {
            trackData.add(stats.getTrackData());
        }
        return trackData;
    }

    @Override
    public int getDuration() {
        if (getEnd() == -1) {
            return LocalTime.now().toSecondOfDay() - getStart();
        }
        return getEnd() - getStart();
    }

    @XmlTransient
    public boolean isFinished() {
        return missionFinished;
    }

    public void setFinished(boolean finished) {
        missionFinished = finished;
    }

    @Override
    public boolean isCompleted() {
        if (taskStats.isEmpty()) {
            return false;
        }
        for (TaskStat taskStat : taskStats) {
            if (!taskStat.isCompleted()) {
                return false;
            }
        }
        return true;
    }

    public TaskStat getSummary() {
        return TaskStat.summaryInstance(this);
    }

}
