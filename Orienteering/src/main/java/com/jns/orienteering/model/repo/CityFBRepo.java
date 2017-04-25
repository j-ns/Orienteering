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
package com.jns.orienteering.model.repo;

import static com.jns.orienteering.model.repo.BaseUrls.*;

import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.GluonObservableObject;
import com.gluonhq.connect.provider.DataProvider;
import com.jns.orienteering.model.common.GluonObservables;
import com.jns.orienteering.model.persisted.CitiesByUser;
import com.jns.orienteering.model.persisted.City;
import com.jns.orienteering.model.persisted.CityNameLookup;
import com.jns.orienteering.model.persisted.RepoAction;
import com.jns.orienteering.model.repo.readerwriter.RestMapReader;

public class CityFBRepo extends FireBaseRepo<City> {

    private static final String              PUBLIC             = "public";
    private static final String              PRIVATE            = "private";

    private NameLookupFBRepo<CityNameLookup> nameLookupRepo     = new NameLookupFBRepo<>(CityNameLookup.class, CITY_NAMES);
    private FireBaseRepo<CitiesByUser>       citiesByUserLookup = new FireBaseRepo<>(CitiesByUser.class, CITIES_BY_USER);

    public CityFBRepo() {
        super(City.class, CITIES);
    }

    public GluonObservableList<City> getPrivateListAsync(String userId) {
        if (userId == null) {
            return GluonObservables.newListInitialized();
        }

        String idsUrl = UrlBuilder.buildUrl(CITIES_BY_USER, userId);
        return DataProvider.retrieveList(new RestMapReader<>(createRestClient(), CitiesByUser.class, idsUrl, City.class, CITIES));
    }

    public GluonObservableList<City> getPublicListAsync() {
        return retrieveListAsync();
    }

    public boolean checkIfNameExists(String cityName) {
        return nameLookupRepo.checkIfNameExists(cityName);
    }

    public GluonObservableObject<City> createAsync(City city) {
        return executeAsync(city, () ->
        {
            city.setTimeStamp(createTimeStamp());
            City result = addToList(city);

            if (result != null) {
                writeLogEntry(result, RepoAction.ADD);

                nameLookupRepo.createOrUpdate(new CityNameLookup(city));

                CitiesByUser existingLookup = citiesByUserLookup.retrieveObject(city.getOwnerId());
                if (existingLookup != null) {
                    existingLookup.addValue(city.getId());
                    citiesByUserLookup.createOrUpdate(existingLookup, city.getOwnerId());
                } else {
                    CitiesByUser newLookup = new CitiesByUser(city.getOwnerId(), city.getId());
                    citiesByUserLookup.createOrUpdate(newLookup, city.getOwnerId());
                }
            }
        });
    }

    public GluonObservableObject<City> updateAsync(City city, String previousName) {
        return executeAsync(city, () ->
        {
            city.setTimeStamp(createTimeStamp());
            createOrUpdate(city, city.getId());
            writeLogEntry(city, RepoAction.UPDATE);

            if (!city.getCityName().equals(previousName)) {
                nameLookupRepo.recreateLookup(new CityNameLookup(city), previousName);
            }
        });
    }

    public GluonObservableObject<City> deleteAsync(City city) {
        return executeAsync(city, () ->
        {
            city.setTimeStamp(createTimeStamp());
            delete(city.getId());
            writeLogEntry(city, RepoAction.DELETE);

            nameLookupRepo.deleteLookup(new CityNameLookup(city));

            CitiesByUser existingLookup = citiesByUserLookup.retrieveObject(city.getOwnerId());
            existingLookup.removeValue(city.getId());
            citiesByUserLookup.createOrUpdate(existingLookup, city.getOwnerId());
        });
    }

    public boolean isCityValidForDelete(String cityId) {
        return !checkIfUrlExists(TASKS_BY_CITY, PRIVATE, cityId) && !checkIfUrlExists(TASKS_BY_CITY, PUBLIC, cityId) &&
                !checkIfUrlExists(MISSIONS_BY_CITY, PRIVATE, cityId) && !checkIfUrlExists(MISSIONS_BY_CITY, PUBLIC, cityId);
    }

    private void writeLogEntry(City city, RepoAction action) {
        getChangeLogRepo().writeLogAsync(city, action, CITIES);
    }

}
