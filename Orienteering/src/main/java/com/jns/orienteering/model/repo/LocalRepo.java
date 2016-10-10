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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.GluonObservableObject;
import com.gluonhq.connect.provider.DataProvider;
import com.gluonhq.connect.provider.FileClient;
import com.gluonhq.connect.provider.ObjectDataReader;
import com.gluonhq.connect.provider.ObjectDataRemover;
import com.gluonhq.connect.provider.ObjectDataWriter;
import com.jns.orienteering.model.common.BaseModel;
import com.jns.orienteering.model.repo.readerwriter.FileIterableInputConverter;
import com.jns.orienteering.model.repo.readerwriter.FileTreeIterableOutputConverter;
import com.jns.orienteering.model.repo.readerwriter.JsonInputConverterExtended;
import com.jns.orienteering.model.repo.readerwriter.JsonOutputConverterExtended;
import com.jns.orienteering.platform.PlatformProvider;

public class LocalRepo<T, L> {

    private static final Logger            LOGGER = LoggerFactory.getLogger(LocalRepo.class);

    private static final File              BASE_DIR;

    private final Class<T>                 targetClass;
    private final Class<L>                 localClass;
    private final String                   fileName;

    private FileClient                     fileClient;
    private JsonInputConverterExtended<T>  inputConverter;
    private JsonOutputConverterExtended<T> outputConverter;

    static {
        BASE_DIR = PlatformProvider.getPlatformService().getStorage().getPrivate();
    }

    public LocalRepo(Class<T> targetClass, Class<L> localClass, String fileName) {
        this.targetClass = targetClass;
        this.localClass = localClass;
        this.fileName = fileName;

        inputConverter = new JsonInputConverterExtended<>(targetClass);
        outputConverter = new JsonOutputConverterExtended<>(targetClass);

        fileClient = FileClient.create(new File(BASE_DIR, fileName));
    }

    public boolean fileExists() {
        return new File(BASE_DIR, fileName).exists();
    }

    public void createOrUpdate(T obj) throws IOException {
        try {
            writer().writeObject(obj);
        } catch (IOException e) {
            LOGGER.error("failed to create or update file: {}/{}", BASE_DIR, fileName, e);
            throw e;
        }
    }

    public GluonObservableObject<T> createOrUpdateAsync(T obj) {
        return DataProvider.storeObject(obj, writer());
    }

    public <E extends BaseModel> void createOrUpdateList(Class<E> targetClass, String baseDir, List<E> items) {
        new FileTreeIterableOutputConverter<>(targetClass, baseDir, items).writeObjects();
    }

    public GluonObservableObject<L> createOrUpdateListAsync(L items) {
        ObjectDataWriter<L> writer = fileClient.createObjectDataWriter(new JsonOutputConverterExtended<>(localClass));
        return DataProvider.storeObject(items, writer);
    }

    public T retrieveObject() throws IOException {
        try {
            return reader().readObject();

        } catch (IOException e) {
            LOGGER.error("failed to read file: {} {}", BASE_DIR, fileName, e);
            throw e;
        }
    }

    public GluonObservableObject<T> retrieveObjectAsync() {
        return DataProvider.retrieveObject(reader());
    }

    public GluonObservableList<T> retrieveListAsync(String listIdentifier) {
        return DataProvider.retrieveList(new FileIterableInputConverter<>(fileClient.createFileDataSource(), targetClass,
                                                                          listIdentifier));
    }

    public void delete() throws IOException {
        try {
            remover().removeObject(null);
        } catch (IOException e) {
            LOGGER.error("failed to delete: {}", fileName, e);
            throw e;
        }
    }

    public GluonObservableObject<T> deleteAsync() {
        GluonObservableObject<T> obs = new GluonObservableObject<>();
        DataProvider.removeObject(obs, remover());
        return obs;
    }

    private ObjectDataWriter<T> writer() {
        return fileClient.createObjectDataWriter(outputConverter);
    }

    private ObjectDataReader<T> reader() {
        return fileClient.createObjectDataReader(inputConverter);
    }

    private ObjectDataRemover<T> remover() {
        return fileClient.createObjectDataRemover();
    }

}