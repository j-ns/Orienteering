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
package com.jns.orienteering.model.repo.readerwriter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.converter.InputStreamInputConverter;
import com.gluonhq.connect.provider.ListDataReader;
import com.gluonhq.connect.provider.RestClient;

public abstract class AbstractRestObjectsReader<T, E> implements ListDataReader<E>, Iterator<E> {

    private static final Logger            LOGGER      = LoggerFactory.getLogger(AbstractRestObjectsReader.class);

    protected static final String          JSON_SUFFIX = ".json";

    protected RestClient                   client;
    private RestClient                     targetClient;
    protected Class<T>                     sourceClass;
    protected Class<E>                     targetClass;
    protected String                       targetUrl;

    protected InputStreamInputConverter<E> converter;
    protected Iterator<String>             keysIterator;

    public AbstractRestObjectsReader(RestClient sourceClient, RestClient targetClient, Class<T> sourceClass, String sourceUrl, Class<E> targetClass,
                                     String targetUrl) {
        this(sourceClient, sourceClass, sourceUrl, targetClass, targetUrl);
        this.targetClient = targetClient;
    }

    public AbstractRestObjectsReader(RestClient client, Class<T> sourceClass, String sourceUrl, Class<E> targetClass, String targetUrl) {
        this.client = client;
        this.sourceClass = sourceClass;
        this.targetClass = targetClass;

        if (sourceUrl.endsWith(JSON_SUFFIX)) {
            client.path(sourceUrl);
        } else {
            client.path(sourceUrl + JSON_SUFFIX);
        }
        if (!targetUrl.endsWith("/")) {
            this.targetUrl = targetUrl + "/";
        } else {
            this.targetUrl = targetUrl;
        }

        converter = new JsonInputConverterExtended<>(targetClass);
    }

    @Override
    public GluonObservableList<E> newGluonObservableList() {
        return new GluonObservableList<>();
    }

    @Override
    public Iterator<E> iterator() throws IOException {
        initKeysIterator(client);
        return this;
    }

    protected abstract void initKeysIterator(RestClient client) throws IOException;

    @Override
    public boolean hasNext() {
        return keysIterator.hasNext();
    }

    @Override
    public E next() {
        String url = targetUrl + keysIterator.next() + JSON_SUFFIX;

        InputStream inputStream;
        try {
            if (targetClient == null) {
                client.path(url);
                inputStream = client.createRestDataSource().getInputStream();
            } else {
                targetClient.path(url);
                inputStream = targetClient.createRestDataSource().getInputStream();
            }
            converter.setInputStream(inputStream);

        } catch (IOException e) {
            LOGGER.error("Failed to retrieve: '{}'", url, e);
        }
        return converter.read();
    }

}
