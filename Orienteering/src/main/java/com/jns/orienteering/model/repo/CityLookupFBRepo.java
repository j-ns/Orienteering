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

import java.io.IOException;

import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.provider.DataProvider;
import com.jns.orienteering.model.common.GluonObservables;
import com.jns.orienteering.model.persisted.AccessType;
import com.jns.orienteering.model.persisted.BaseModel;
import com.jns.orienteering.model.persisted.CityLookup;
import com.jns.orienteering.model.repo.readerwriter.RestMapReader;

public class CityLookupFBRepo<T extends CityLookup, LT extends BaseModel> extends FireBaseRepo<T> {

    private static final String PUBLIC  = "public";
    private static final String PRIVATE = "private";

    private String              lookupTargetUrl;
    private Class<LT>           lookupTargetClass;

    public CityLookupFBRepo(Class<T> lookupClass, Class<LT> lookupTargetClass, String lookupBaseUrl, String lookupTargetUrl) {
        super(lookupClass, lookupBaseUrl);
        this.lookupTargetUrl = lookupTargetUrl;
        this.lookupTargetClass = lookupTargetClass;
    }

    public GluonObservableList<LT> getPrivateListAsync(String cityId, String userId) {
        if (userId == null) {
            return GluonObservables.newListInitialized();
        }
        String url = buildUrlFromRelativePath(PRIVATE, cityId, userId);
        return retrieveList(url);
    }

    public GluonObservableList<LT> getPublicListAsync(String cityId) {
        String idsUrl = buildUrlFromRelativePath(PUBLIC, cityId);
        return retrieveList(idsUrl);
    }

    private GluonObservableList<LT> retrieveList(String sourceUrl) {
        return DataProvider.retrieveList(new RestMapReader<>(createRestClient(), targetClass, sourceUrl, lookupTargetClass, lookupTargetUrl));
    }

    public void createOrUpdate(T lookup) throws IOException {
        String lookupPath = buildPath(lookup);
        updateRestClientFromRelativePath(GET, lookupPath);

        T existingLookup = retrieveObject(lookupPath);
        if (existingLookup != null) {
            if (!existingLookup.containsValue(lookup.getTargetId())) {
                existingLookup.addValue(lookup.getTargetId());
                createOrUpdate(existingLookup, lookupPath);
            }
        } else {
            createOrUpdate(lookup, lookupPath);
        }
    }

    public void recreateCityLookup(T lookup) throws IOException {
        createOrUpdate(lookup);

        if (lookup.accessTypeChanged()) {
            AccessType newAccessType = lookup.getAccessType();
            AccessType previousAccessType = newAccessType == AccessType.PRIVATE ? AccessType.PUBLIC : AccessType.PRIVATE;
            lookup.setAccessType(previousAccessType);
        }
        if (lookup.cityChanged()) {
            lookup.setId(lookup.getPreviousId());
        }
        deleteLookup(lookup);
    }

    public void deleteLookup(T lookup) throws IOException {
        String path = UrlBuilder.buildPath(buildPath(lookup), lookupTargetUrl, lookup.getTargetId());
        updateRestClientFromRelativePath(GET, path);

        boolean urlExists = checkIfUrlExists(baseUrl, path);
        if (urlExists) {
            delete(path);
        }
    }

    private String buildPath(T lookup) {
        return UrlBuilder.buildPath(lookup.getAccessTypeName(), lookup.getId(), lookup.getOwnerId());
    }

}
