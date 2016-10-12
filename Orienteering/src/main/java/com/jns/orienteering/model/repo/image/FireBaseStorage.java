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
package com.jns.orienteering.model.repo.image;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.AuthCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class FireBaseStorage {

    private static final Logger         LOGGER                   = LoggerFactory.getLogger(FireBaseStorage.class);

    private static final String         SERVICE_CREDENTIALS_FILE = "/service_secret.json";
    private static final String         PROJECT_ID               = "orienteering-2dd97";
    private static final String         BUCKET                   = "orienteering-2dd97.appspot.com";

    private Storage                     storage;

    public static final FireBaseStorage INSTANCE                 = new FireBaseStorage();

    private FireBaseStorage() {
        initService();
    }

    private void initService() {
        try {
            storage = StorageOptions.builder()
                                    .projectId(PROJECT_ID)
                                    .authCredentials(AuthCredentials.createForJson(FireBaseStorage.class.getResourceAsStream(SERVICE_CREDENTIALS_FILE)))
                                    .build()
                                    .service();
        } catch (IOException e) {
            LOGGER.error("Failed to initialize FireBaseStorage", e);
        }
    }

    public void create(byte[] content, String url) {
        storage.create(blobInfo(url, ImageType.fromUrl(url)), content);
    }

    public boolean delete(String url) {
        return storage.delete(blobId(url));
    }

    public byte[] retrieve(String url) {
        Blob blob = storage.get(blobId(url));
        if (blob != null) {
            return blob.content();
        }
        return new byte[0];
    }

    private BlobId blobId(String url) {
        return BlobId.of(BUCKET, url);
    }

    private BlobInfo blobInfo(String url, ImageType type) {
        return BlobInfo.builder(BUCKET, url)
                       .contentType(type.get())
                       .build();

    }

}
