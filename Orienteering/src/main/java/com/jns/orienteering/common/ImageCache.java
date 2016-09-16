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
package com.jns.orienteering.common;

import static com.jns.orienteering.util.Validators.isNullOrEmpty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.down.common.PlatformFactory;
import com.gluonhq.charm.down.common.cache.Cache;
import com.jns.orienteering.model.common.StorableImage;
import com.jns.orienteering.platform.PlatformProvider;

import javafx.scene.image.Image;

public class ImageCache {

    private static final Logger               LOGGER     = LoggerFactory.getLogger(ImageCache.class);

    private static final String               IMAGES_DIR = "images";
    private static final File                 IMAGE_STORE;

    private static final Cache<String, Image> imageCache;

    static {
        IMAGE_STORE = PlatformProvider.getPlatform().getStorage().getPrivateFile(IMAGES_DIR);
        imageCache = PlatformFactory.getPlatform().getCacheManager().createCache("ImageCache");

        LOGGER.debug("imageStore: {}", IMAGE_STORE);
    }

    public static boolean add(StorableImage storableImage) {
        if (storableImage.get() == null || isNullOrEmpty(storableImage.getTargetUrl())) {
            return false;
        }
        String targetUrl = storableImage.getTargetUrl();

        File target = new File(IMAGE_STORE, targetUrl);
        if (target.exists()) {
            LOGGER.debug("image already exists: {}", target);
            return true;
        }
        target.getParentFile().mkdirs();

        try {
            storeImageLocal(storableImage, target);
            imageCache.put(targetUrl, storableImage.get());

            LOGGER.debug("cached image {}", targetUrl);
        } catch (IOException e) {
            LOGGER.error("Error caching image: {}", targetUrl, e);
            return false;
        }
        return true;
    }

    public static Image getImage(String url) {
        LOGGER.debug("getImage: {}", url);
        if (isNullOrEmpty(url)) {
            return null;
        }

        Image cachedImage = imageCache.get(url);
        if (cachedImage == null) {
            LOGGER.debug("image not in cache: {}", url);

            Image storedImage = loadLocalImage(url);
            if (storedImage == null) {
                LOGGER.debug("image not on disk: {}", url);

            } else {
                imageCache.put(url, storedImage);
                LOGGER.debug("image loaded from disk, put into cache {}", url);
                return storedImage;
            }
        }
        return cachedImage;
    }

    public static void remove(String url) {
        if (isNullOrEmpty(url)) {
            return;
        }
        imageCache.remove(url);

        File storedFile = new File(IMAGE_STORE, url);
        if (storedFile.isFile()) {
            boolean deleted = storedFile.delete();
            if (deleted) {
                LOGGER.debug("removed from cache and disk: {}", url);
            } else {
                LOGGER.debug("could not delete file: {}", url);
            }
        } else {
            LOGGER.info("Can't delete url: {} (file does not exist, or is not a file)", url);
        }
    }

    private static void storeImageLocal(StorableImage storableImage, File target) throws IOException {
            try (FileOutputStream outputStream = new FileOutputStream(target)) {
                outputStream.write(storableImage.getContent());
            }
    }

    private static Image loadLocalImage(String url) {
        File file = new File(IMAGE_STORE, url);
        return file.exists() ? new Image(file.toURI().toString()) : null;
    }

}
