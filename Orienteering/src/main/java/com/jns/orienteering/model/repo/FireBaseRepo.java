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

import static com.jns.orienteering.util.Validators.isNullOrEmpty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import com.jns.orienteering.model.persisted.Model;
import com.jns.orienteering.model.persisted.Postable;
import com.jns.orienteering.model.repo.readerwriter.JsonInputConverterExtended;
import com.jns.orienteering.model.repo.readerwriter.JsonOutputConverterExtended;
import com.jns.orienteering.model.repo.readerwriter.JsonTreeConverter;
import com.jns.orienteering.util.ExceptionalTrigger;
import com.jns.orienteering.util.GluonObservables;

public class FireBaseRepo<T extends Model> {

    private static final Logger              LOGGER          = LoggerFactory.getLogger(FireBaseRepo.class);

    protected static final String            GET             = "GET";
    protected static final String            PUT             = "PUT";
    protected static final String            POST            = "POST";

    private static final ExecutorService     executor        = Executors.newFixedThreadPool(4, runnable ->
                                                             {
                                                                 Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                                                                 thread.setName("FireBaseRepoThread");
                                                                 thread.setDaemon(true);
                                                                 return thread;
                                                             });

    private static final ChangeLogRepo       CHANGE_LOG_REPO = new ChangeLogRepo();

    private RestClient                       restClient;
    protected String                         baseUrl;
    private UrlBuilder                       urlBuilder;

    protected final Class<T>                 targetClass;
    protected JsonOutputConverterExtended<T> outputConverter;
    protected JsonInputConverterExtended<T>  inputConverter;
    protected JsonTreeConverter<T>           listInputConverter;

    public FireBaseRepo(Class<T> targetClass, String baseUrl) {
        this.targetClass = targetClass;
        this.baseUrl = baseUrl;
        urlBuilder = new UrlBuilder(baseUrl);
        restClient = createRestClient();
        outputConverter = new JsonOutputConverterExtended<>(targetClass);
        inputConverter = new JsonInputConverterExtended<>(targetClass);
    }

    protected RestClient createRestClient() {
        return RestClientFactory.baseClient();
    }

    protected void updateRestClientUrl(String method, String... urlParts) {
        updateRestClient(method, UrlBuilder.buildUrl(urlParts));
    }

    protected void updateRestClientFromRelativePath(String method, String... urlParts) {
        updateRestClient(method, buildUrlFromRelativePath(urlParts));
    }

    protected void updateRestClient(String method, String path) {
        if (isNullOrEmpty(path)) {
            throw new IllegalArgumentException("restclient path must not be null or empty");
        }
        restClient.method(method);
        restClient.path(path);
    }

    protected String buildUrlFromRelativePath(String... urlParts) {
        return urlBuilder.buildUrlFromRelativePath(urlParts);
    }

    protected String buildPath(String... urlParts) {
        return UrlBuilder.buildPath(urlParts);
    }

    public boolean checkIfUrlExists(String... urlParts) {
        String string = null;

        try {
            RestClient client = RestClientFactory.queryClient(UrlBuilder.buildUrl(urlParts));
            RestDataSource createRestDataSource = client.createRestDataSource();
            InputStream input = createRestDataSource.getInputStream();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                string = stringBuilder.toString();
                LOGGER.debug("payload: {}", string);
            }
        } catch (IOException ex) {
            LOGGER.error("Failed to checkIfExists: '{}'", buildPath(urlParts), ex);
        }
        return string != null && !"null".equals(string);
    }

    public void createOrUpdate(T obj, String... urlParts) throws IOException {
        try {
            updateRestClientFromRelativePath(PUT, urlParts);
            writer().writeObject(obj);

        } catch (IOException ex) {
            LOGGER.error("Failed to write obj: {}", urlParts, ex);
            throw ex;
        }
    }

    public GluonObservableObject<T> createOrUpdateAsync(T obj, String... urlParts) {
        updateRestClientFromRelativePath(PUT, urlParts);
        return DataProvider.storeObject(obj, writer());
    }

    public T addToList(T obj) throws IOException {
        Objects.requireNonNull(obj, "POST object must not be null");

        try {
            updateRestClientUrl(POST, baseUrl);
            Optional<T> result = writer().writeObject(obj);
            if (result.isPresent()) {
                updateId(obj, result.get());
            }
        } catch (IOException ex) {
            LOGGER.error("POST failed: '{}'", obj, ex);
            throw ex;
        }
        return obj;
    }

    private void updateId(T obj, T result) throws IOException {
        try {
            if (obj instanceof Postable) {
                Postable p = (Postable) result;
                obj.setId(p.getPostId());
            }
            updateRestClientFromRelativePath(PUT, obj.getId());
            writer().writeObject(obj);

        } catch (IOException ex) {
            LOGGER.error("Failed to write: '{}'", obj, ex);
            throw ex;
        }
    }

    public GluonObservableObject<RemoveObject> delete(String... urlParts) throws IOException {
        String url = buildUrlFromRelativePath(urlParts);

        GluonObservableObject<RemoveObject> obs = RemoveObject.observableInstance(url);
        try {
            remover(RestClientFactory.deleteClient(url)).removeObject(obs);

        } catch (IOException ex) {
            LOGGER.error("Failed to delete: '{}'", url, ex);
            throw ex;
        }
        return obs;
    }

    public GluonObservableObject<RemoveObject> deleteAsync(String... urlParts) {
        String url = buildUrlFromRelativePath(urlParts);

        GluonObservableObject<RemoveObject> obs = RemoveObject.observableInstance(url);
        DataProvider.removeObject(obs, remover(RestClientFactory.deleteClient(url)));
        return obs;
    }

    public T retrieveObject(String... urlParts) throws IOException {
        try {
            updateRestClientFromRelativePath(GET, urlParts);
            return reader(restClient).readObject();

        } catch (IOException ex) {
            LOGGER.error("Failed to read: {}", urlParts, ex);
            throw ex;
        }
    }

    public GluonObservableObject<T> retrieveObjectAsync(String... urlParts) {
        updateRestClientFromRelativePath(GET, urlParts);
        return DataProvider.retrieveObject(reader(restClient));
    }

    public GluonObservableList<T> retrieveListAsync(String... urlParts) {
        updateRestClientFromRelativePath(GET, urlParts);
        return DataProvider.retrieveList(listReader(restClient));
    }

    public GluonObservableList<T> retrieveListFilteredAsync(List<QueryParameter> queryParams, String... urlParts) {
        RestClient client = RestClientFactory.queryClient(queryParams, buildUrlFromRelativePath(urlParts));
        return DataProvider.retrieveList(listReader(client));
    }

    public GluonObservableObject<T> retrieveObjectFilteredAsync(List<QueryParameter> queryParams, String... urlParts) {
        RestClient client = RestClientFactory.queryClient(queryParams, buildUrlFromRelativePath(urlParts));
        return DataProvider.retrieveObject(reader(client));
    }

    protected ObjectDataWriter<T> writer() {
        return restClient.createObjectDataWriter(outputConverter, inputConverter);
    }

    protected ObjectDataReader<T> reader(RestClient client) {
        return client.createObjectDataReader(inputConverter);
    }

    protected ListDataReader<T> listReader(RestClient client) {
        return client.createListDataReader(listInputConverter());
    }

    protected InputStreamIterableInputConverter<T> listInputConverter() {
        if (listInputConverter == null) {
            listInputConverter = new JsonTreeConverter<>(targetClass);
        }
        return listInputConverter;
    }

    protected ObjectDataRemover<RemoveObject> remover(RestClient client) {
        return client.createObjectDataRemover(new JsonOutputConverterExtended<>(RemoveObject.class), new JsonInputConverterExtended<>(
                                                                                                                                      RemoveObject.class));
    }

    public long createTimeStamp() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        long epochDay = now.toLocalDate().toEpochDay();
        long secs = epochDay * 86400 + now.toLocalTime().toSecondOfDay();
        secs -= now.getOffset().getTotalSeconds();
        return secs;
    }

    protected ChangeLogRepo getChangeLogRepo() {
        return CHANGE_LOG_REPO;
    }

    protected GluonObservableObject<T> newGluonObservableObject() {
        return new GluonObservableObject<>();
    }

    protected GluonObservableObject<T> executeAsync(T sourceObject, ExceptionalTrigger action) {
        GluonObservableObject<T> obsResult = newGluonObservableObject();
        executeAsync(Optional.of(sourceObject), obsResult, action);
        return obsResult;
    }

    protected void executeAsync(Optional<T> sourceObject, GluonObservableObject<T> result, ExceptionalTrigger action) {
        executor.execute(() ->
        {
            try {
                action.start();
                sourceObject.ifPresent(result::set);
                GluonObservables.setInitialized(result);

            } catch (Exception ex) {
                LOGGER.error("Error on executeAsync", ex);
                result.setException(ex);
            }
        });
    }

    private static class RemoveObject {

        private final String url;

        private RemoveObject(String url) {
            this.url = url;
        }

        private static GluonObservableObject<RemoveObject> observableInstance(String url) {
            return GluonObservables.newObject(new RemoveObject(url));
        }

        @Override
        public String toString() {
            return url;
        }
    }

}
