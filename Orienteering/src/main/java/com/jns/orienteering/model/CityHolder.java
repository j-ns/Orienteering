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
package com.jns.orienteering.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.connect.GluonObservableList;
import com.jns.orienteering.model.persisted.City;
import com.jns.orienteering.util.GluonObservableHelper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;

public class CityHolder {

    private static final Logger      LOGGER      = LoggerFactory.getLogger(CityHolder.class);

    private static String            userId;
    private static Map<String, City> cityIds     = new HashMap<>();                                                                                                                                                                                                                 // publicIds?
    private static Map<String, City> userCityIds = new HashMap<>();                                                                                                                                                                                                                 // privateIds?

    private CityHolder() {
    }

    public static void createMapping(List<City> cities, String userId) {
        CityHolder.userId = userId;
        LOGGER.debug("initializing cityMapper with userId: {}", userId);

        for (City city : cities) {
            cityIds.put(city.getId(), city);

            if (userId != null && userId.equals(city.getOwnerId())) {
                userCityIds.put(city.getId(), city);
            }
        }
    }

    public static void setUserId(String userId) {
        userCityIds.clear();

        CityHolder.userId = userId;
        if (userId != null) {
            for (City city : cityIds.values()) {
                if (userId.equals(city.getOwnerId())) {
                    userCityIds.put(city.getId(), city);
                }
            }
        }
    }

    public static void put(City city) {
        cityIds.put(city.getId(), city);
        if (userId != null && userId.equals(city.getOwnerId())) {
            userCityIds.put(city.getId(), city);
        }
    }

    public static void remove(City city) {
        cityIds.remove(city.getId());
        userCityIds.remove(city.getId());
    }

    public static City remove(String cityId) {
        userCityIds.remove(cityId);
        return cityIds.remove(cityId);
    }

    public static boolean isEmpty() {
        return cityIds.isEmpty();
    }

    public static boolean contains(String cityId) {
        return cityIds.containsKey(cityId);
    }

    public static ObservableList<City> getAll() {
        return new SortedList<>(FXCollections.observableArrayList(cityIds.values()), City::compareTo);
    }

    public static City get(String cityId) {
        return cityIds.get(cityId);
    }

    public static GluonObservableList<City> getPrivateCities() {
        return getCitiesList(userCityIds.values());
    }

    public static GluonObservableList<City> getPublicCities() {
        return getCitiesList(cityIds.values());
    }

    private static GluonObservableList<City> getCitiesList(Collection<City> source) {
        return GluonObservableHelper.newGluonObservableListInitialized(source);
    }

}
