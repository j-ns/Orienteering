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
package com.jns.orienteering.model.dynamic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.connect.GluonObservableList;
import com.jns.orienteering.model.common.GluonObservables;
import com.jns.orienteering.model.persisted.City;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.CityFBRepo;
import com.jns.orienteering.model.repo.RepoService;

import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;

/**
 * Caches public and private cities during start-up. When there is no internet connection,
 * when the app is started, the lists will be empty and each subsequent request for a city list
 * will retry to access the cloud data.
 */
public class CityCache {

    private static final Logger       LOGGER        = LoggerFactory.getLogger(CityCache.class);

    public static final CityCache     INSTANCE      = new CityCache();

    private CityFBRepo                cloudRepo     = RepoService.INSTANCE.getCloudRepo(City.class);
    // private LocalRepo<City, LocalCityList> localRepo = RepoService.INSTANCE.getLocalRepo(City.class);

    private String                    userId;
    private Map<String, City>         cityIds       = new HashMap<>();                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   // publicIds?
    private Map<String, City>         userCityIds   = new HashMap<>();                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   // privateIds?

    private GluonObservableList<City> publicCities  = new GluonObservableList<>();
    private GluonObservableList<City> privateCities = new GluonObservableList<>();

    private SortedList<City>          citiesSorted;

    private boolean                   initialized;

    private CityCache() {
    }

    public void createMapping(List<City> cities, String userId) {
        this.userId = userId;
        LOGGER.debug("initializing cityMapper with userId: {}", userId);

        for (City city : cities) {
            cityIds.put(city.getId(), city);

            if (userId != null && userId.equals(city.getOwnerId())) {
                userCityIds.put(city.getId(), city);
            }
        }
        initialized = true;
        publicCities = GluonObservables.newListInitialized(cityIds.values());
        privateCities = GluonObservables.newListInitialized(userCityIds.values());
    }

    public void setUserId(String userId) {
        this.userId = userId;
        userCityIds.clear();
        privateCities = GluonObservables.newListInitialized();
    }

    public GluonObservableList<City> getPrivateCities() {
        if (privateCities.isEmpty() && userId != null) {
            GluonObservables.setInitialized(privateCities, false);

            AsyncResultReceiver.create(getPublicCities())
                               .defaultProgressLayer()
                               .onSuccess(result ->
                               {
                                   for (City city : result) {
                                       if (userId.equals(city.getOwnerId())) {
                                           userCityIds.put(city.getId(), city);
                                       }
                                   }
                                   GluonObservables.setInitialized(privateCities, userCityIds.values(), true);
                               })
                               .start();
        }
        return privateCities;
    }

    public GluonObservableList<City> getPublicCities() {
        if (publicCities.isEmpty()) {
            publicCities = cloudRepo.getPublicListAsync();
        }
        return publicCities;
    }

    public ObservableList<City> getCitiesSorted() {
        if (citiesSorted == null) {
            citiesSorted = new SortedList<>(getPublicCities(), City::compareTo);
        }
        return citiesSorted;
    }

    public boolean contains(String cityId) {
        return cityIds.containsKey(cityId);
    }

    public City get(String cityId) {
        return cityIds.get(cityId);
    }

    public Optional<String> getName(String cityId) {
        return !contains(cityId) ? Optional.empty() : Optional.of(get(cityId).getCityName());
    }

    public void put(City city) {
        cityIds.put(city.getId(), city);
        publicCities.add(city);

        if (userId != null && userId.equals(city.getOwnerId())) {
            userCityIds.put(city.getId(), city);
            privateCities.add(city);
        }
    }

    public void remove(City city) {
        if (city != null) {
            remove(city.getId());
        }
    }

    public City remove(String cityId) {
        City removed = cityIds.remove(cityId);
        userCityIds.remove(cityId);

        publicCities.remove(removed);
        privateCities.remove(removed);

        if (removed != null) {
            LOGGER.debug("CityCache: removed city: {}", removed);
        }
        return removed;
    }

    public boolean isEmpty() {
        return cityIds.isEmpty();
    }

    public boolean isInitialized() {
        return initialized;
    }

}
