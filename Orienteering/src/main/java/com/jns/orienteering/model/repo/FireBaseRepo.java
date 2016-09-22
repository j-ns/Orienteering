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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import javax.json.stream.JsonParsingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.GluonObservableObject;
import com.gluonhq.connect.converter.InputStreamIterableInputConverter;
import com.gluonhq.connect.provider.DataProvider;
import com.gluonhq.connect.provider.ListDataReader;
import com.gluonhq.connect.provider.ObjectDataReader;
import com.gluonhq.connect.provider.ObjectDataRemover;
import com.gluonhq.connect.provider.ObjectDataWriter;
import com.gluonhq.connect.provider.RestClient;
import com.gluonhq.connect.source.RestDataSource;
import com.jns.orienteering.model.common.Model;
import com.jns.orienteering.model.common.Postable;
import com.jns.orienteering.model.common.Synchronizable;
import com.jns.orienteering.model.persisted.ChangeLogEntry;
import com.jns.orienteering.model.repo.readerwriter.JsonInputConverterExtended;
import com.jns.orienteering.model.repo.readerwriter.JsonOutputConverterExtended;
import com.jns.orienteering.model.repo.readerwriter.JsonTreeConverter;
import com.jns.orienteering.util.GluonObservableHelper;
import com.jns.orienteering.util.Validators;

import javafx.util.Pair;

public class FireBaseRepo<T extends Model> {

    private static final Logger              LOGGER             = LoggerFactory.getLogger(FireBaseRepo.class);

    private static final String              APP_ID             = "https://orienteering-2dd97.firebaseio.com";
    protected static final String            JSON_SUFFIX        = ".json";
    private static final String              AUTH_PARAM_NAME    = "auth";
    protected static final String            CREDENTIALS        = "2ekET9SyGxrYCeSWgPZaWdiCHxncCHmAvGCjDjwu";

    protected static final String            GET                = "GET";
    protected static final String            PUT                = "PUT";
    protected static final String            POST               = "POST";
    private static final String              OVERRIDE_PARAMETER = "x-http-method-override";

    private static ChangeLogRepo             changeLogRepo      = new ChangeLogRepo();

    protected RestClient                     restClient;
    protected String                         baseUrl;

    protected final Class<T>                 targetClass;
    protected JsonOutputConverterExtended<T> outputConverter;
    protected JsonInputConverterExtended<T>  inputConverter;
    protected JsonTreeConverter<T>           listInputConverter;

    public FireBaseRepo(Class<T> targetClass, String baseUrl) {
        this.targetClass = targetClass;
        this.baseUrl = baseUrl;

        restClient = createRestClient();
        outputConverter = new JsonOutputConverterExtended<>(targetClass);
        inputConverter = new JsonInputConverterExtended<>(targetClass);
    }

    protected RestClient createRestClient() {
        RestClient client = RestClient.create().host(APP_ID);
        client.queryParam(AUTH_PARAM_NAME, CREDENTIALS);
//        client.queryParam("print", "pretty");
        return client;
    }

    protected void recreateRestClient() {
        restClient = createRestClient();
    }

    protected void updateRestClientUrl(String method, String... urlParts) {
        updateRestClient(method, buildFullUrl(urlParts));
    }

    protected void updateRestClientPath(String method, String... urlParts) {
        String url = buildFullUrlFromRelativePath(urlParts);
        updateRestClient(method, url);

        LOGGER.debug("rest URL:  {} {}", method, url);
    }

    protected void updateRestClient(String method, String path) {
        if (Validators.isNullOrEmpty(path)) {
            throw new IllegalArgumentException("restClient path must not be null or empty");
        }
        restClient.method(method);
        restClient.path(path);
    }

    protected String buildFullUrl(String... urlParts) {
        return buildPath(urlParts) + JSON_SUFFIX;
    }

    protected String buildFullUrlFromRelativePath(String... urlParts) {
        if (urlParts.length > 0) {
            return baseUrl + buildPath(urlParts) + JSON_SUFFIX;
        }
        return baseUrl + JSON_SUFFIX;
    }

    protected static String buildPath(String... urlParts) {
        String result = "";

        for (String child : urlParts) {
            if (Validators.isNullOrEmpty(child)) {
                continue;
            }

            if (!child.startsWith("/")) {
                result = result + "/" + child;
            } else {
                result = result + child;
            }
        }
        return result;
    }

    public boolean checkIfUrlExists(String... urlParts) {
        String string = null;

        try {
            updateRestClientUrl(GET, urlParts);
            restClient.queryParam("shallow", "true");

            RestDataSource createRestDataSource = restClient.createRestDataSource();
            InputStream input = createRestDataSource.getInputStream();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                string = stringBuilder.toString();
                LOGGER.debug("json: {}", string);
            }
        } catch (IOException e) {
            LOGGER.error("Error on checkIfExists: '{}'", buildPath(urlParts), e);
        }
        return string != null && !"null".equals(string);
    }

    public void createOrUpdate(T obj, String... urlParts) throws IOException {
        try {
            updateRestClientPath(PUT, urlParts);
            writer().writeObject(obj);

        } catch (IOException e) {
            LOGGER.error("Error writing obj: {}", urlParts, e);
            throw e;
        }
    }

    public GluonObservableObject<T> createOrUpdateAsync(T obj, String... urlParts) {
        updateRestClientPath(PUT, urlParts);
        return DataProvider.storeObject(obj, writer());
    }

    public GluonObservableObject<RemoveObject> delete(String... urlParts) throws IOException {
        GluonObservableObject<RemoveObject> obs = RemoveObject.observableInstance(buildPath(urlParts));
        try {
            updateRestClientPath(POST, urlParts);
            restClient.queryParam(OVERRIDE_PARAMETER, "Delete");
            remover().removeObject(obs);

        } catch (IOException e) {
            LOGGER.error("Error deleting object: ''{}", urlParts, e);
            restClient = createRestClient();
            throw e;
        } catch (JsonParsingException e2) {
            LOGGER.error("JsonParcingException on delete: '{}'", urlParts, e2);
        }
        restClient = createRestClient();
        return obs;
    }

    public GluonObservableObject<RemoveObject> deleteAsync(String... urlParts) {
        GluonObservableObject<RemoveObject> obs = RemoveObject.observableInstance(buildPath(urlParts));

        updateRestClientPath(POST, urlParts);
        restClient.queryParam(OVERRIDE_PARAMETER, "Delete");
        DataProvider.removeObject(obs, remover());

        return obs;
    }

    public T retrieveObject(String... urlParts) throws IOException {
        try {
            updateRestClientPath(GET, urlParts);
            return reader().readObject();

        } catch (IOException e) {
            LOGGER.error("Error writing obj: {}", urlParts, e);
            throw e;
        }
    }

    public GluonObservableObject<T> retrieveObjectAsync(String... urlParts) {
        updateRestClientPath(GET, urlParts);
        return DataProvider.retrieveObject(reader());
    }

    public T addToList(T obj) throws IOException {
        try {
            updateRestClientUrl(POST, baseUrl);
            Optional<T> result = writer().writeObject(obj);
            if (result.isPresent()) {
                updateId(obj, result.get());
            }

        } catch (IOException e) {
            LOGGER.error("Error adding item: '{}'", obj, e);
            throw e;
        }
        return obj;
    }

    private void updateId(T obj, T result) throws IOException {
        try {
            if (obj instanceof Postable) {
                Postable p = (Postable) result;
                obj.setId(p.getPostId());
            }
            updateRestClientPath(PUT, obj.getId());
            writer().writeObject(obj);

        } catch (IOException e) {
            LOGGER.error("Error writing object: '{}'", obj, e);
            throw e;
        }
    }

    public List<T> retrieveList(String... urlParts) throws IOException {
        return retrieveList(listReader(), urlParts);
    }

    public List<T> retrieveList(ListDataReader<T> listReader, String... urlParts) throws IOException {
        updateRestClientPath(GET, urlParts);

        List<T> result = new ArrayList<>();
        try {
            for (Iterator<T> it = listReader().iterator(); it.hasNext();) {
                T e = it.next();
                if (e != null) {
                    result.add(e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error retrieving list", e);
            throw e;
        }
        return result;
    }

    public GluonObservableList<T> retrieveListAsync(String... urlParts) {
        updateRestClientPath(GET, urlParts);
        return DataProvider.retrieveList(listReader());
    }

    public GluonObservableList<T> retrieveListAsync(RestClient client) {
        return DataProvider.retrieveList(listReader());
    }

    public GluonObservableList<T> retrieveListFilteredAsync(String orderBy, Pair<String, String> filter, String... urlParts) {
        updateRestClientPath(GET, urlParts);
        restClient.queryParam("orderBy", "\"" + orderBy + "\"");
        restClient.queryParam(filter.getKey(), filter.getValue());
        GluonObservableList<T> result = DataProvider.retrieveList(listReader());
        recreateRestClient();
        return result;
    }

    public GluonObservableList<T> retrieveListFilteredAsync(String orderBy, List<Pair<String, String>> filters, String... urlParts) {
        updateRestClientPath(GET, urlParts);
        restClient.queryParam("orderBy", "\"" + orderBy + "\"");

        for (Pair<String, String> filter : filters) {
            restClient.queryParam(filter.getKey(), filter.getValue());
        }

        GluonObservableList<T> result = DataProvider.retrieveList(listReader());
        recreateRestClient();
        return result;
    }

    public GluonObservableObject<T> retrieveObjectFilteredAsync(String orderBy, Pair<String, String> filter, String... urlParts) {
        updateRestClientPath(GET, urlParts);
        restClient.queryParam("orderBy", "\"" + orderBy + "\"");
        restClient.queryParam(filter.getKey(), filter.getValue());
        return DataProvider.retrieveObject(reader());
    }

    public T retrieveObjectFiltered(String orderBy, Pair<String, String> filter, String... urlParts) throws IOException {
        updateRestClientPath(GET, urlParts);
        restClient.queryParam("orderBy", "\"" + orderBy + "\"");
        restClient.queryParam(filter.getKey(), filter.getValue());
        return reader().readObject();
    }

    protected ObjectDataWriter<T> writer() {
        return restClient.createObjectDataWriter(outputConverter, inputConverter);
    }

    protected ObjectDataReader<T> reader() {
        return restClient.createObjectDataReader(inputConverter);
    }

    protected ListDataReader<T> listReader() {
        return restClient.createListDataReader(listInputConverter());
    }

    protected InputStreamIterableInputConverter<T> listInputConverter() {
        if (listInputConverter == null) {
            listInputConverter = new JsonTreeConverter<>(targetClass);
        }
        return listInputConverter;
    }

    protected ObjectDataRemover<RemoveObject> remover() {
        return restClient.createObjectDataRemover(new JsonOutputConverterExtended<>(RemoveObject.class), new JsonInputConverterExtended<>(
                                                                                                                                          RemoveObject.class));
    }

    public long createTimeStamp() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        long epochDay = now.toLocalDate().toEpochDay();
        long secs = epochDay * 86400 + now.toLocalTime().toSecondOfDay();
        secs -= now.getOffset().getTotalSeconds();
        return secs;
    }

    protected void writeLogEntry(ChangeLogEntry logEntry, BiConsumer<ChangeLogRepo, ChangeLogEntry> logWriter) {
        logWriter.accept(changeLogRepo, logEntry);
    }

    protected <S extends Synchronizable> void writeLogEntry(S obj, BiConsumer<ChangeLogRepo, ChangeLogEntry> logWriter) {
        ChangeLogEntry entry = new ChangeLogEntry(obj);
        logWriter.accept(changeLogRepo, entry);
    }

    public static class RemoveObject {

        private final String url;

        private RemoveObject(String url) {
            this.url = url;
        }

        private static GluonObservableObject<RemoveObject> observableInstance(String url) {
            return GluonObservableHelper.newGluonObservable(new RemoveObject(url));
        }

        @Override
        public String toString() {
            return url;
        }
    }

}
