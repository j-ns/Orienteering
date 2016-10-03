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

import java.util.function.BiConsumer;

import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.GluonObservableObject;
import com.jns.orienteering.model.common.RepoAction;
import com.jns.orienteering.model.common.Synchronizable;
import com.jns.orienteering.model.persisted.ChangeLogEntry;

import javafx.util.Pair;

public class ChangeLogRepo extends FireBaseRepo<ChangeLogEntry> {

    private static final String CHANGE_LOG        = "change_log";
    private static final String CITIES            = "cities";
    private static final String TASKS             = "tasks";
    private static final String IMAGES            = "images";
    private static final String MISSIONS          = "missions";
    private static final String TIME_STAMP_FILTER = "timeStamp";

    public ChangeLogRepo() {
        super(ChangeLogEntry.class, CHANGE_LOG);
    }

    public void writeLog(Synchronizable obj, RepoAction action, BiConsumer<ChangeLogRepo, ChangeLogEntry> logWriter) {
        obj.setRepoAction(action);
        ChangeLogEntry changeLogEntry = new ChangeLogEntry(obj);
        logWriter.accept(this, changeLogEntry);
    }

    public void writeCityLogAsync(ChangeLogEntry entry) {
        writeAsync(entry, CITIES, entry.getId());
    }

    public void writeMissionLogAsync(ChangeLogEntry entry) {
        writeAsync(entry, MISSIONS, entry.getId());
    }

    public void writeTaskLogAsync(ChangeLogEntry entry) {
        writeAsync(entry, TASKS, entry.getId());
    }

    public void writeImageLogAsync(ChangeLogEntry entry) {
        writeAsync(entry, IMAGES, entry.getId());
    }

    public GluonObservableObject<ChangeLogEntry> writeAsync(ChangeLogEntry entry, String... urlParts) {
        return createOrUpdateAsync(entry, urlParts);
    }

    public GluonObservableList<ChangeLogEntry> readListAsync(long lastSynced, String... urlParts) {
        return retrieveListFilteredAsync(TIME_STAMP_FILTER, startAt(lastSynced),
                                         urlParts);
    }

    public GluonObservableObject<ChangeLogEntry> readObjectAsync(long lastSynced, String... urlParts) {
        return retrieveObjectFilteredAsync(TIME_STAMP_FILTER, startAt(lastSynced),
                                           urlParts);
    }

    public void removeLogEntriesBefore(long timeStamp) {
        GluonObservableList<ChangeLogEntry> obsCityLogEntries = retrieveListFilteredAsync(TIME_STAMP_FILTER, new Pair<String, String>("endAt", String
                                                                                                                                                     .valueOf(
                                                                                                                                                              timeStamp)),
                                                                                          CITIES);

        AsyncResultReceiver.create(obsCityLogEntries)
                           .onSuccess(result ->
                           {
                               for (ChangeLogEntry candidate : obsCityLogEntries) {
                                   deleteAsync(CITIES, candidate.getId());
                               }
                           })
                           .start();
    }

    private Pair<String, String> startAt(long lastSynced) {
        return new Pair<String, String>("startAt", Long.toString(lastSynced));
    }
}
