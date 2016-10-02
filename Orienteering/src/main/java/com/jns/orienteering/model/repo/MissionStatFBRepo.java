/*
* Copyright (c) 2016, Jens Stroh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL JENS STROH BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jns.orienteering.model.repo;

import static com.jns.orienteering.locale.Localization.localize;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.provider.DataProvider;
import com.gluonhq.connect.provider.RestClient;
import com.jns.orienteering.model.persisted.MissionStat;
import com.jns.orienteering.model.persisted.StatByMission;
import com.jns.orienteering.model.persisted.StatByUser;
import com.jns.orienteering.model.repo.readerwriter.RestObjectsReader;
import com.jns.orienteering.util.Dialogs;
import com.jns.orienteering.util.GluonObservableHelper;

public class MissionStatFBRepo extends FireBaseRepo<MissionStat> {

    private static final Logger         LOGGER            = LoggerFactory.getLogger(MissionStatFBRepo.class);

    private static final String         MISSION_STATS     = "mission_stats";
    private static final String         STATS_BY_MISSION  = "stats_by_mission";
    private static final String         STATS_BY_USER     = "stats_by_user";

    private FireBaseRepo<StatByMission> statByMissionRepo = new FireBaseRepo<>(StatByMission.class, STATS_BY_MISSION);
    private FireBaseRepo<StatByUser>    statByUserRepo    = new FireBaseRepo<>(StatByUser.class, STATS_BY_USER);

    public MissionStatFBRepo() {
        super(MissionStat.class, MISSION_STATS);
    }

    public void createOrUpdate(MissionStat missionStat) {
        try {
            String userId = missionStat.getUserId();
            String missionId = missionStat.getMission().getId();
            StatByUser existingStatByUser = statByUserRepo.retrieveObject(userId, missionStat.getMissionId());

            addToList(missionStat);

            statByUserRepo.createOrUpdate(new StatByUser(missionStat), userId, missionId);
            statByMissionRepo.createOrUpdate(new StatByMission(missionStat), missionId, missionStat.getId());

            if (existingStatByUser != null) {
                statByMissionRepo.delete(missionId, existingStatByUser.getLookupId());
                delete(existingStatByUser.getLookupId());
            }

        } catch (IOException ex) {
            LOGGER.error("Failed to save mission stat", ex);
            Dialogs.ok(localize("view.activeMission.error.saveStat")).showAndWait();
        }
    }

    public GluonObservableList<StatByUser> getStatsByUser(String userId) {
        boolean statsByUserExists = checkIfUrlExists(userId);
        if (statsByUserExists) {
            return statByUserRepo.retrieveListAsync(userId);
        }
        return GluonObservableHelper.newGluonObservableListInitialized();
    }

    public GluonObservableList<MissionStat> getMissionStats(String missionId) {
        RestClient client = createRestClient();
        client.path(statByMissionRepo.buildUrlFromRelativePath(missionId));
        client.queryParam("orderBy", "\"duration\"");
        client.queryParam("limitToFirst", "5");

        String sourceUrl = STATS_BY_MISSION + "/" + missionId;

        return DataProvider.retrieveList(new RestObjectsReader<>(client, createRestClient(), StatByMission.class,
                                                                 sourceUrl, MissionStat.class, MISSION_STATS));
    }

    public void deleteAsync(String missionId) {
        if (!checkIfUrlExists(missionId)) {
            return;
        }

        GluonObservableList<MissionStat> obsMissionStats = DataProvider.retrieveList(new RestObjectsReader<>(createRestClient(), StatByMission.class,
                                                                                                             STATS_BY_MISSION + "/" + missionId,
                                                                                                             MissionStat.class,
                                                                                                             MISSION_STATS));

        AsyncResultReceiver.create(obsMissionStats)
                           .onSuccess(result ->
                           {
                               for (MissionStat missionStat : result) {
                                   super.deleteAsync(missionStat.getId());
                               }
                               statByMissionRepo.deleteAsync(missionId);
                           })
                           .start();
    }

    public void deleteStatByUserAsync(String userId, String statId) {
        statByUserRepo.deleteAsync(userId, statId);
    }

}
