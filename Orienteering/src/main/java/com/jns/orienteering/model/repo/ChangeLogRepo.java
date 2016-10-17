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
import static com.jns.orienteering.model.repo.QueryParameter.orderByTimeStamp;
import static com.jns.orienteering.model.repo.QueryParameter.shallow;
import static com.jns.orienteering.model.repo.QueryParameter.startAt;

import java.util.Arrays;

import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.GluonObservableObject;
import com.jns.orienteering.model.persisted.ChangeLogEntry;
import com.jns.orienteering.model.persisted.RepoAction;
import com.jns.orienteering.model.persisted.Synchronizable;

public class ChangeLogRepo extends FireBaseRepo<ChangeLogEntry> {

    private static ChangeLogRepo instance;

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

    public GluonObservableObject<ChangeLogEntry> readObjectAsync(long lastSynced, String... urlParts) {
        return retrieveObjectFilteredAsync(Arrays.asList(orderByTimeStamp(), startAt(lastSynced)), urlParts);
    }

    // todo:delete log entries of the previous month but one. If lastSync was at that time, reload all data
    // limit the max number of delete operations for a single user, to distribute the changeLog cleanup operations amongst all
    // users
    public void removeLogEntriesUntil(long timeStamp) {
        removeLogEntries(CITIES, getLogEntriesBefore(CITIES, timeStamp));
        removeLogEntries(TASKS, getLogEntriesBefore(TASKS, timeStamp));
        removeLogEntries(MISSIONS, getLogEntriesBefore(MISSIONS, timeStamp));
        removeLogEntries(IMAGES, getLogEntriesBefore(IMAGES, timeStamp));
    }

    private GluonObservableList<ChangeLogEntry> getLogEntriesBefore(String url, long timeStamp) {
        return retrieveListFilteredAsync(Arrays.asList(orderByTimeStamp(), endAt(timeStamp), shallow()), url);
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

}
