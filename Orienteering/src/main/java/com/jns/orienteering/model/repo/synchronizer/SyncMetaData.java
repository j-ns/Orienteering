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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.jns.orienteering.model.persisted.Mission;
import com.jns.orienteering.model.persisted.User;

public class SyncMetaData {

    private long    lastSynced;
    private long    currentTimeStamp;
    private String  listIdentifier;
    private String  userId;
    private User    user;
    private Mission activeMission;

    public SyncMetaData() {
    }

    public SyncMetaData(long lastSynced, long currentTimeStamp) {
        this.lastSynced = lastSynced;
        this.currentTimeStamp = currentTimeStamp;
    }

    public long getLastSynced() {
        return lastSynced;
    }

    public void setLastSynced(long lastSynced) {
        this.lastSynced = lastSynced;
    }

    public long getCurrentTimeStamp() {
        return currentTimeStamp;
    }

    public void setCurrentTimeStamp(long currentTimeStamp) {
        this.currentTimeStamp = currentTimeStamp;
    }

    public void setCurrentTimeStamp() {
        currentTimeStamp = createTimeStamp();
    }

    public boolean isLastSyncedBefore(LocalDate date) {
        LocalDate lastSyncedDate = Instant.ofEpochSecond(lastSynced).atZone(ZoneId.systemDefault()).toLocalDate();

        int result = lastSyncedDate.getYear() - date.getYear();
        if (result == 0) {
            result = lastSyncedDate.getMonthValue() - date.getMonthValue();
            if (result == 0) {
                result = lastSyncedDate.getDayOfMonth() - date.getDayOfMonth();
            }
        }
        return result < 0;
    }

    /**
     * @return
     *         {@link LocalDate} of today converted to epochSeconds
     */
    private long createTimeStamp() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        long epochDay = now.toLocalDate().toEpochDay();
        long secs = epochDay * 86400 + now.toLocalTime().toSecondOfDay();
        secs -= now.getOffset().getTotalSeconds();
        return secs;
    }

    public String getListIdentifier() {
        return listIdentifier;
    }

    public void setListIdentifier(String listIdentifier) {
        this.listIdentifier = listIdentifier;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public SyncMetaData userId(String userId) {
        this.userId = userId;
        return this;
    }

    public Mission getActiveMission() {
        return activeMission;
    }

    public void setActiveMission(Mission activeMission) {
        this.activeMission = activeMission;
    }

    public SyncMetaData activeMission(Mission activeMission) {
        this.activeMission = activeMission;
        return this;
    }

}
