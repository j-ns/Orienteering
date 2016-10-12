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

import static com.jns.orienteering.locale.Localization.localize;
import static com.jns.orienteering.util.DateTimeFormatters.createTimeStamp;

import java.util.Comparator;
import java.util.Objects;

import javax.xml.bind.annotation.XmlTransient;

import com.gluonhq.charm.down.common.Position;
import com.gluonhq.maps.MapPoint;
import com.jns.orienteering.locale.Localization;

public class Task extends BasePostableSynchronizable implements CityAssignable, LookupSupplier, Comparable<Task> {

    private static final Comparator<Task> ORDER_NUMBER_COMPARATOR = (t, t1) -> Integer.compare(t.getOrderNumber(), t1.getOrderNumber());

    private String                        cityId;
    private String                        ownerId;
    private AccessType                    accessType              = AccessType.PRIVATE;

    private String                        taskName;
    private String                        description;
    private double                        longitude;
    private double                        latitude;
    private boolean                       createImageId;
    private String                        imageId;
    private String                        scancode;
    private int                           points;

    private int                           orderNumber;

    private boolean                       completed;

    private Task                          previousTask;

    @JsonDefaultConstructor
    public Task() {
    }

    public Task(String cityId, String name, String description, Position position, int points, AccessType accessType, String ownerId,
                boolean createImageId) {
        this.cityId = cityId;
        taskName = name;
        this.description = description;
        this.points = points;
        longitude = position.getLongitude();
        latitude = position.getLatitude();
        this.accessType = accessType;
        this.ownerId = ownerId;
        this.createImageId = createImageId;
    }

    public static Task finishedInstance(boolean completed) {
        Task task = new Task();
        task.taskName = localize("mission.finished");
        task.setCompleted(completed);
        return task;
    }

    @Override
    public void setId(String id) {
        super.setId(id);
        if (createImageId) {
            imageId = createImageId();
        }
    }

    private String createImageId() {
        return id + "_" + createTimeStamp() + ".jpg";
    }

    @XmlTransient
    public Task getPreviousTask() {
        return previousTask;
    }

    public void setPreviousTask(Task previousTask) {
        this.previousTask = previousTask;
    }

    @Override
    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String name) {
        taskName = name;
    }

    @Override
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public Position getPosition() {
        return new Position(latitude, longitude);
    }

    public String getPositionString() {
        return latitude == 0 && longitude == 0 ? null : latitude + "," + longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getImageUrl() {
        return imageId == null ? null : "tasks/" + imageId;
    }

    public String getScanCode() {
        return scancode;
    }

    public void setScancode(String barcode) {
        scancode = barcode;
    }

    public int getPoints() {
        return points;
    }

    public int getAchievedPoints() {
        return completed ? points : 0;
    }

    public String getPointsString() {
        return points == 0 ? " - " + localize("task.points") : Integer.valueOf(points) + " " + Localization.localize("task.points");
    }

    public void setPoints(int points) {
        this.points = points;
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
        return previousTask != null && previousTask.accessType != accessType;
    }

    @XmlTransient
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isCompleted() {
        return completed;
    }

    @XmlTransient
    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public static Comparator<Task> getOrderNumberComparator() {
        return ORDER_NUMBER_COMPARATOR;
    }

    public MapPoint getMapPoint() {
        return new MapPoint(latitude, longitude);
    }

    @Override
    public String getPreviousCityId() {
        return previousTask.getCityId();
    }

    @Override
    public boolean cityChanged() {
        return previousTask != null && !previousTask.cityId.equals(cityId);
    }

    @Override
    public boolean nameChanged() {
        Objects.requireNonNull(previousTask, "previousTask must not be null");
        return !previousTask.getTaskName().equals(taskName);
    }

    @Override
    public TaskNameLookup createNameLookup() {
        return new TaskNameLookup(taskName, id);
    }

    @Override
    public CityTaskLookup createCityLookup() {
        return new CityTaskLookup(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Task)) {
            return false;
        }
        Task other = (Task) obj;
        return compareTo(other) == 0;
    }

    @Override
    public int compareTo(Task other) {
        return taskName.compareTo(other.taskName);
    }

    @Override
    public String toString() {
        return new StringBuilder().append(taskName)
                                  .append(" cityId: ")
                                  .append(cityId)
                                  .append(" ownerId: ")
                                  .append(ownerId)
                                  .append(" ")
                                  .append(accessType)
                                  .append(" lat: ")
                                  .append(latitude)
                                  .append(" long: ")
                                  .append(longitude)
                                  .toString();
    }

}
