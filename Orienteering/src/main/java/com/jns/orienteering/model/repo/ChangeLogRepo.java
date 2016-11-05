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

import static com.jns.orienteering.model.repo.BaseUrls.CITIES;
import static com.jns.orienteering.model.repo.BaseUrls.IMAGES;
import static com.jns.orienteering.model.repo.BaseUrls.MISSIONS;
import static com.jns.orienteering.model.repo.BaseUrls.TASKS;
import static com.jns.orienteering.model.repo.QueryParameter.endAt;
import static com.jns.orienteering.model.repo.QueryParameter.limitToFirst;
import static com.jns.orienteering.model.repo.QueryParameter.orderByTimeStamp;
import static com.jns.orienteering.model.repo.QueryParameter.startAt;

import java.time.LocalDate;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.SettingsService;
import com.gluonhq.connect.GluonObservableList;
import com.jns.orienteering.model.persisted.ChangeLogEntry;
import com.jns.orienteering.model.persisted.RepoAction;
import com.jns.orienteering.model.persisted.Synchronizable;

public class ChangeLogRepo extends FireBaseRepo<ChangeLogEntry> {

    private static final Logger   LOGGER                 = LoggerFactory.getLogger(ChangeLogRepo.class);

    private static final String   LAST_CLEAN_UP_PROPERTY = "last_clean_up";

    private static SettingsService settingService         = Services.get(SettingsService.class).orElseThrow(() -> new IllegalStateException(
                                                                                                                                           "Failed to get SettingsService"));

    private static ChangeLogRepo  instance;

    private ChangeLogRepo() {
        super(ChangeLogEntry.class, BaseUrls.CHANGE_LOG);
    }

    public static ChangeLogRepo getInstance() {
        if (instance == null) {
            instance = new ChangeLogRepo();
        }
        return instance;
    }

    public void writeImageLogAsync(ChangeLogEntry entry) {
        createOrUpdateAsync(entry, BaseUrls.IMAGES, entry.getId());
    }

    public void writeLogAsync(Synchronizable synchronizable, RepoAction action, String baseUrl) {
        synchronizable.setRepoAction(action);
        ChangeLogEntry changeLogEntry = new ChangeLogEntry(synchronizable);
        createOrUpdateAsync(changeLogEntry, baseUrl, changeLogEntry.getId());
    }

    public GluonObservableList<ChangeLogEntry> readListAsync(long lastSynced, String... urlParts) {
        return retrieveListFilteredAsync(Arrays.asList(orderByTimeStamp(), startAt(lastSynced)), urlParts);
    }

    // public GluonObservableObject<ChangeLogEntry> readObjectAsync(long lastSynced, String... urlParts) {
    // return retrieveObjectFilteredAsync(Arrays.asList(orderByTimeStamp(), startAt(lastSynced)), urlParts);
    // }

    public void cleanLog() {
        LocalDate now = LocalDate.now();
        int thisMonth = now.getMonthValue();

        if (!isCleanedUp(thisMonth)) {
            LOGGER.debug("start changeLog cleanUp");

            LocalDate threeMonthsBeforeLastDayOfMonth = now.minusMonths(2).withDayOfMonth(1).minusDays(1);
            long timeStamp = TimeStampCreator.timeStamp(threeMonthsBeforeLastDayOfMonth);
            removeLogEntriesUntil(timeStamp);

            storeLastCleanUp(thisMonth);
        }
    }

    private boolean isCleanedUp(int month) {
        String lastCleanUp = settingService.retrieve(LAST_CLEAN_UP_PROPERTY);
        return String.valueOf(month).equals(lastCleanUp);
    }

    /**
     * Remove log entries unitl <code>timeStamp</code> (inclusive).
     * The max number of delete operations is limited for a single user, in order to distribute the cleanup operations amongst all
     * users
     *
     * @param timeStamp
     *            the timeStamp in epochseconds until (inclusive) the logEntries should be deleted
     */
    private void removeLogEntriesUntil(long timeStamp) {
        removeLogEntries(CITIES, getLogEntriesUntil(CITIES, timeStamp));
        removeLogEntries(TASKS, getLogEntriesUntil(TASKS, timeStamp));
        removeLogEntries(MISSIONS, getLogEntriesUntil(MISSIONS, timeStamp));
        removeLogEntries(IMAGES, getLogEntriesUntil(IMAGES, timeStamp));
    }

    private GluonObservableList<ChangeLogEntry> getLogEntriesUntil(String url, long timeStamp) {
        return retrieveListFilteredAsync(Arrays.asList(orderByTimeStamp(), endAt(timeStamp), limitToFirst("50")), url);
    }

    private void removeLogEntries(String url, GluonObservableList<ChangeLogEntry> logEntries) {
        AsyncResultReceiver.create(logEntries)
                           .onSuccess(result ->
                           {
                               for (ChangeLogEntry candidate : logEntries) {
                                   deleteAsync(url, candidate.getId());
                               }
                           })
                           .start();
    }

    private void storeLastCleanUp(int month) {
        settingService.store(LAST_CLEAN_UP_PROPERTY, String.valueOf(month));
    }

}
