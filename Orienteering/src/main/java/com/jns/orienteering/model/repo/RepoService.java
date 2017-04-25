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

import java.util.HashMap;
import java.util.Map;

import com.jns.orienteering.model.persisted.Model;

public enum RepoService {

    INSTANCE;

    private final RepoFactory                                  repoFactory;

    private final Map<Class<?>, FireBaseRepo<? extends Model>> cloudRepoCache;
    private final Map<Class<?>, LocalRepo<?, ?>>               localRepoCache;

    RepoService() {
        repoFactory = new RepoFactory();
        cloudRepoCache = new HashMap<>();
        localRepoCache = new HashMap<>();
    }

    public <T extends Model, R extends FireBaseRepo<T>> R getCloudRepo(Class<T> modelClass) {
        @SuppressWarnings("unchecked")
        R cloudRepo = (R) cloudRepoCache.get(modelClass);

        if (cloudRepo == null) {
            cloudRepo = createCloudRepo(modelClass);
            cloudRepoCache.put(modelClass, cloudRepo);
        }
        return cloudRepo;
    }

    public <T extends Model, R extends LocalRepo<?, ?>> R getLocalRepo(Class<T> modelClass) {
        @SuppressWarnings("unchecked")
        R localRepo = (R) localRepoCache.get(modelClass);

        if (localRepo == null) {
            localRepo = createLocalRepo(modelClass);
            localRepoCache.put(modelClass, localRepo);
        }
        return localRepo;
    }

    public <T extends Model, R extends FireBaseRepo<T>> R createCloudRepo(Class<T> modelClass) {
        return repoFactory.createCloudRepo(modelClass);
    }

    public <T extends Model, R extends LocalRepo<?, ?>> R createLocalRepo(Class<T> modelClass) {
        return repoFactory.createLocalRepo(modelClass);
    }

}