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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.jns.orienteering.model.common.Model;
import com.jns.orienteering.model.persisted.ActiveTaskList;
import com.jns.orienteering.model.persisted.City;
import com.jns.orienteering.model.persisted.LocalCityList;
import com.jns.orienteering.model.persisted.Mission;
import com.jns.orienteering.model.persisted.MissionStat;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.persisted.User;

public class RepoFactory {

    private static final int                               INITIAL_CAPACITY = 10;

    private final Map<Class<?>, Supplier<FireBaseRepo<?>>> cloudRepoSuppliers;
    private final Map<Class<?>, Supplier<LocalRepo<?, ?>>> localRepoSuppliers;

    public RepoFactory() {
        cloudRepoSuppliers = new HashMap<>(INITIAL_CAPACITY);
        localRepoSuppliers = new HashMap<>(INITIAL_CAPACITY);

        cloudRepoSuppliers.put(User.class, UserFBRepo::new);
        cloudRepoSuppliers.put(City.class, CityFBRepo::new);
        cloudRepoSuppliers.put(Mission.class, MissionFBRepo::new);
        cloudRepoSuppliers.put(Task.class, TaskFBRepo::new);
        cloudRepoSuppliers.put(MissionStat.class, MissionStatFBRepo::new);

        localRepoSuppliers.put(User.class, () -> new LocalRepo<>(User.class, User.class, "user.json"));
        localRepoSuppliers.put(City.class, () -> new LocalRepo<>(City.class, LocalCityList.class, "cities.json"));
        localRepoSuppliers.put(Task.class, () -> new LocalRepo<>(Task.class, ActiveTaskList.class, "activeTasks.json"));
    }

    @SuppressWarnings("unchecked")
    public <T extends Model, R extends FireBaseRepo<T>> R createCloudRepo(Class<T> dataClass) {
        return (R) cloudRepoSuppliers.get(dataClass).get();
    }

    @SuppressWarnings("unchecked")
    public <T extends Model, R extends LocalRepo<?, ?>> R createLocalRepo(Class<T> dataClass) {
        return (R) localRepoSuppliers.get(dataClass).get();
    }

}
