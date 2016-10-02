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

import java.io.IOException;

import javax.json.stream.JsonParsingException;

import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.provider.DataProvider;
import com.jns.orienteering.model.common.AccessType;
import com.jns.orienteering.model.common.BaseModel;
import com.jns.orienteering.model.common.CityLookup;
import com.jns.orienteering.model.repo.readerwriter.RestMapReader;
import com.jns.orienteering.util.GluonObservableHelper;

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
            return GluonObservableHelper.newGluonObservableListInitialized();
        }
        String idsUrl = buildFullUrlFromRelativePath(PRIVATE, cityId, userId);
        return retrieveList(idsUrl);
    }

    public GluonObservableList<LT> getPublicListAsync(String cityId) {
        String idsUrl = buildFullUrlFromRelativePath(PUBLIC, cityId);
        return retrieveList(idsUrl);
    }

    private GluonObservableList<LT> retrieveList(String idsUrl) {
        return DataProvider.retrieveList(new RestMapReader<>(createRestClient(), targetClass, idsUrl, lookupTargetClass, lookupTargetUrl));
    }

    public void recreateCityLookup(T lookup) throws IOException {
        createOrUpdate(lookup);

        if (lookup.hasAccessTypeChanged()) {
            AccessType newAccessType = lookup.getAccessType();
            AccessType previousAccessType = newAccessType == AccessType.PRIVATE ? AccessType.PUBLIC : AccessType.PRIVATE;
            lookup.setAccessType(previousAccessType);
        }
        if (lookup.hasCityChanged()) {
            lookup.setId(lookup.getPreviousId());
        }
        deleteLookup(lookup);
    }

    public void createOrUpdate(T lookup) throws IOException {
        try {
            String lookupPath = buildLookupPath(lookup);
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
        } catch (JsonParsingException e) {
            super.createOrUpdate(lookup, buildLookupPath(lookup));
        }
    }

    public void deleteLookup(T lookup) throws IOException {
        String lookupPath = buildLookupPath(lookup) + "/" + lookupTargetUrl + "/" + lookup.getTargetId();
        updateRestClientFromRelativePath(GET, lookupPath);

        boolean urlExists = checkIfUrlExists(baseUrl, lookupPath);
        if (urlExists) {
            delete(lookupPath);
        }
    }

    private String buildLookupPath(T lookup) {
        String cityId = lookup.getId();
        String access = lookup.getAccessTypeName();
        String ownerId = lookup.getOwnerId();
        return buildPath(access, cityId, ownerId);
    }

}
