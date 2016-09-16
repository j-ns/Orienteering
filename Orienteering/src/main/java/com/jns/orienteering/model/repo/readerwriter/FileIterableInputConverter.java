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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.provider.ListDataReader;
import com.gluonhq.connect.source.FileDataSource;

public class FileIterableInputConverter<E> implements ListDataReader<E>, Iterator<E> {

    private static final Logger      LOGGER = LoggerFactory.getLogger(FileIterableInputConverter.class);

    private FileDataSource           dataSource;
    private String                   listIdentifier;

    private JsonConverterExtended<E> converter;

    private JsonArray                jsonArray;
    private int                      index;

    public FileIterableInputConverter(FileDataSource dataSource, Class<E> targetClass, String listIdentifier) {
        this.dataSource = dataSource;
        this.listIdentifier = listIdentifier;

        converter = new JsonConverterExtended<>(targetClass);
    }

    @Override
    public GluonObservableList<E> newGluonObservableList() {
        return new GluonObservableList<>();
    }

    @Override
    public Iterator<E> iterator() throws IOException {
        if (dataSource.getFile().exists()) {
            try (JsonReader reader = Json.createReader(dataSource.getInputStream())) {

                JsonObject jsonObject = reader.readObject();
                jsonArray = jsonObject.getJsonArray(listIdentifier);

            } catch (FileNotFoundException e) {
                LOGGER.error("File not found: {}", dataSource.getFile());
            }
        }
        return this;
    }

    @Override
    public boolean hasNext() {
        return jsonArray != null && index < jsonArray.size();
    }

    @Override
    public E next() {
        JsonObject jsonObject = jsonArray.getJsonObject(index++);
        return converter.readFromJson(jsonObject);

    }

}
