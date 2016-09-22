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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.connect.GluonObservableList;
import com.jns.orienteering.model.common.RepoAction;
import com.jns.orienteering.model.dynamic.CityHolder;
import com.jns.orienteering.model.persisted.ChangeLogEntry;
import com.jns.orienteering.model.persisted.City;
import com.jns.orienteering.model.persisted.LocalCityList;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.CityFBRepo;
import com.jns.orienteering.model.repo.LocalRepo;

public class CitySynchronizer extends BaseSynchronizer<City, City, LocalCityList> {

    private static final Logger LOGGER               = LoggerFactory.getLogger(CitySynchronizer.class);

    public static final String  NAME                 = "city_synchronizer";
    private static final String CITY_LIST_IDENTIFIER = "cities";

    public CitySynchronizer(CityFBRepo cloudRepo, LocalRepo<City, LocalCityList> localRepo) {
        super(cloudRepo, localRepo, LocalCityList::new, CITY_LIST_IDENTIFIER);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void syncNow(SyncMetaData syncMetaData) {
        setRunning();
        setSyncMetaData(syncMetaData);

        boolean fileExits = localRepo.fileExists();
        if (!fileExits) {
            retrieveCloudDataAndStoreLocally();
        } else {
            readChangeLogAndSyncLocalData();
        }
    }

    @Override
    protected void storeLocally(GluonObservableList<City> cloudData) {
        CityHolder.createMapping(cloudData, getSyncMetaData().getUserId());
        super.storeLocally(cloudData);
    }

    @Override
    protected void syncLocalData(GluonObservableList<ChangeLogEntry> log) {
        GluonObservableList<City> obsLocalData = localRepo.retrieveListAsync(listIdentifier);
        AsyncResultReceiver.create(obsLocalData)
                           .onSuccess(resultLocal ->
                           {
                               CityHolder.createMapping(resultLocal, getSyncMetaData().getUserId());

                               if (log != null) {
                                   boolean localDataNeedsUpdate = false;

                                   for (ChangeLogEntry logEntry : log) {
                                       LOGGER.debug("logEntry {}: {}", listIdentifier, logEntry);

                                       String cityId = logEntry.getTargetId();

                                       if (logEntry.getAction() == RepoAction.DELETE) {
                                           LOGGER.debug("removed locally {}: {}", listIdentifier, CityHolder.get(cityId));
                                           localDataNeedsUpdate = CityHolder.remove(cityId) != null;

                                       } else {
                                           if (!CityHolder.contains(cityId) || CityHolder.get(cityId)
                                                                                         .getTimeStamp() < logEntry.getTimeStamp()) {
                                               try {
                                                   City cityFromCloud = cloudRepo.retrieveObject(cityId);
                                                   CityHolder.put(cityFromCloud);
                                                   localDataNeedsUpdate = true;

                                                   LOGGER.debug("added/udpated locally {}: {}", listIdentifier, cityFromCloud);

                                               } catch (IOException e) {
                                                   // consume
                                               }
                                           }
                                       }
                                   }
                                   if (localDataNeedsUpdate) {
                                       localRepo.createOrUpdateListAsync(new LocalCityList(CityHolder.getAll()));
                                   }
                               }
                               getOnSynced().accept(CityHolder.getAll());
                               setSucceeded();
                           })
                           .onException(this::setFailed)
                           .start();
    }
}