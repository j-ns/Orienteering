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

public class TaskStat extends Stat {

    private Task    task;
    private String  taskName;
    private boolean missionFinished;

    @JsonDefaultConstructor
    public TaskStat() {
    }

    public TaskStat(Task task, TrackData trackData) {
        super(trackData, task.getAchievedPoints());
        taskName = task.getTaskName();
        this.task = task;
    }

    public static TaskStat summaryInstance(MissionStat missionStat) {
        TaskStat task = summaryInstance(Task.finishedInstance(missionStat.isCompleted()), missionStat);
        task.missionFinished = true;
        return task;
    }

    public static TaskStat summaryInstance(Task task, MissionStat missionStat) {
        TaskStat taskStat = new TaskStat(task, missionStat.getTrackData());
        taskStat.setStart(missionStat.getStart());
        taskStat.setEnd(missionStat.getEnd());
        return taskStat;
    }

    public Task getTask() {
        return task;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public boolean isMissionFinished() {
        return missionFinished;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TaskStat)) {
            return false;
        }
        TaskStat other = (TaskStat) obj;
        return compareTo(other) == 0;
    }

    @Override
    public int compareTo(Stat other) {
        int result = taskName.compareTo(((TaskStat) other).getTaskName());
        if (result == 0) {
            result = super.compareTo(other);
        }
        return result;
    }

}
