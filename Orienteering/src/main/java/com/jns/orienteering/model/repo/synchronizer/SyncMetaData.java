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
package com.jns.orienteering.model.repo.synchronizer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import com.jns.orienteering.model.persisted.Mission;
import com.jns.orienteering.model.persisted.User;
import com.jns.orienteering.model.repo.TimeStampCreator;

public class SyncMetaData {

    private long    lastSynced;
    private long    currentTimeStamp;
    private User    user;
    private Mission activeMission;
    private boolean completeRefreshNeeded;

    public SyncMetaData(User user, Mission activeMission) {
        this.user = user;
        this.activeMission = activeMission;
        currentTimeStamp = TimeStampCreator.timeStamp();
    }

    public long getLastSynced() {
        return lastSynced;
    }

    public void setLastSynced(long lastSynced) {
        this.lastSynced = lastSynced;
    }

    public User getUser() {
        return user;
    }

    public String getUserId() {
        return user == null ? null : user.getId();
    }

    public Mission getActiveMission() {
        return activeMission;
    }

    public long getCurrentTimeStamp() {
        return currentTimeStamp;
    }

    public boolean isSyncedToday() {
        return LocalDate.now().equals(getLastSyncedDate());
    }

    public boolean isLastSyncedBefore(LocalDate date) {
        return isBefore(getLastSyncedDate(), date);
    }

    public void checkIfCompleteRefreshIsNeeded() {
        LocalDate threeMonthsBeforeLastDayOfMonth = LocalDate.now().minusMonths(2).withDayOfMonth(1).minusDays(1);
        completeRefreshNeeded = isBefore(getLastSyncedDate(), threeMonthsBeforeLastDayOfMonth);
    }

    public boolean isCompleteRefreshNeeded() {
        return completeRefreshNeeded;
    }

    private LocalDate getLastSyncedDate() {
        return Instant.ofEpochSecond(lastSynced).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private boolean isBefore(LocalDate date1, LocalDate date2) {
        int result = date1.getYear() - date2.getYear();
        if (result == 0) {
            result = date1.getMonthValue() - date2.getMonthValue();
            if (result == 0) {
                result = date1.getDayOfMonth() - date2.getDayOfMonth();
            }
        }
        return result < 0;
    }

}
