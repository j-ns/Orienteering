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
/*
* Copyright (c) 2016, Jens Stroh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL JENS STROH BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jns.orienteering.model.repo.image;

import static com.jns.orienteering.model.common.GluonObservables.setInitialized;
import static com.jns.orienteering.util.Validations.isNullOrEmpty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.Cache;
import com.gluonhq.charm.down.plugins.CacheService;
import com.gluonhq.connect.GluonObservableObject;
import com.google.cloud.storage.StorageException;
import com.jns.orienteering.model.persisted.ChangeLogEntry;
import com.jns.orienteering.platform.PlatformProvider;

import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageHandler {

    private static final Logger          LOGGER              = LoggerFactory.getLogger(ImageHandler.class);

    /**
     * https://commons.wikimedia.org/wiki/File:WikiFont_uniE600_-_userAvatar_-_blue.svg
     * By User:MGalloway (WMF) (mw:Design/WikiFont) [CC BY-SA 3.0 (http://creativecommons.org/licenses/by-sa/3.0)], via Wikimedia
     * Commons
     */

    /**
     *
     *
     * photo credit: http://www.flickr.com/photos/25414200@N07/2588362340 by Sebastian Ludwig via
     * http://photopin.com https://creativecommons.org/licenses/by-nc-sa/2.0
     */

    /**
     * https://commons.wikimedia.org/wiki/File:Gesundbrunnen_Humboldthain_Rosengarten_Diana-001.jpg
     * By user:Fridolin f0reudenfett [CC-BY-SA-4.0] (https://creativecommons.org/licenses/by-sa/4.0/deed.en), via Wikimedia
     * Commons
     */

    /**
     * https://commons.wikimedia.org/wiki/File:Tempelhofer_Feld_(16895371610).jpg
     * By user:Tony Webster [CC-BY- 2.0] (https://creativecommons.org/licenses/by/2.0/deed.en), via Wikimedia Commons
     */

    public static final Image            AVATAR_PLACE_HOLDER = new Image("/images/WikiFont_uniE600_-_userAvatar_-_blue.svg.png");
    public static final Image            IMAGE_PLACE_HOLDER  = new Image("/images/army_texture2.jpg");

    private static final FireBaseStorage STORAGE             = FireBaseStorage.INSTANCE;
    private static final ImageCache      IMAGE_CACHE         = new ImageCache();

    private static final ExecutorService EXECUTOR            = Executors.newFixedThreadPool(4, runnable ->
                                                             {
                                                                 Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                                                                 thread.setName("ImageHandlerThread");
                                                                 thread.setDaemon(true);
                                                                 return thread;
                                                             });

    private ImageHandler() {
    }

    public static GluonObservableObject<Image> storeImageAsync(StorableImage storableImage) {
        return executeAsync(storableImage, ImageHandler::storeImage);
    }

    public static boolean storeImage(StorableImage storableImage) {
        IMAGE_CACHE.add(storableImage);

        String targetUrl = storableImage.getTargetUrl();
        try {
            STORAGE.create(storableImage.getContent(), targetUrl);

        } catch (StorageException e) {
            LOGGER.error("Failed to upload image: {}", targetUrl, e);
            IMAGE_CACHE.remove(targetUrl);
            throw e;
        }
        return true;
    }

    public static GluonObservableObject<Image> updateImageAsync(StorableImage storableImage, String oldUrl) {
        return executeAsync(storableImage, i -> updateImage(i, oldUrl));
    }

    public static void updateImage(StorableImage storableImage, String oldUrl) {
        storeImage(storableImage);
        if (oldUrl != null) {
            deleteImage(oldUrl);
        }
    }

    public static GluonObservableObject<Image> retrieveImageAsync(String url, Image placeHolder) {
        GluonObservableObject<Image> obsImage = new GluonObservableObject<>();

        if (isNullOrEmpty(url)) {
            setInitialized(obsImage, placeHolder, true);
            return obsImage;
        }

        EXECUTOR.execute(() ->
        {
            Image localImage = IMAGE_CACHE.getImage(url);
            if (localImage != null) {
                setInitialized(obsImage, localImage, true);

            } else {
                StorableImage cloudImage = retrieveImageFromCloud(url);
                setInitialized(obsImage, cloudImage.get(), true);

                IMAGE_CACHE.add(cloudImage);
            }
        });
        return obsImage;
    }

    public static StorableImage retrieveImage(String url, Image placeHolder) {
        if (isNullOrEmpty(url)) {
            return new StorableImage(placeHolder);
        }

        Image localImage = IMAGE_CACHE.getImage(url);
        if (localImage != null) {
            return new StorableImage(localImage, url);
        }
        StorableImage storableImage = retrieveImageFromCloud(url);
        EXECUTOR.execute(() -> IMAGE_CACHE.add(storableImage));
        return storableImage;
    }

    public static StorableImage retrieveImageFromCloud(String url) {
        if (url == null) {
            return StorableImage.emptyInstance();
        }

        byte[] content = STORAGE.retrieve(url);
        if (content.length > 0) {
            return new StorableImage(content, url);
        }
        return StorableImage.emptyInstance();
    }

    public static GluonObservableObject<Image> deleteImageAsync(String url) {
        return executeAsync(StorableImage.emptyInstance(), e -> deleteImage(url));
    }

    public static void deleteImage(String url) {
        IMAGE_CACHE.remove(url);

        boolean deleted = STORAGE.delete(url);
        if (deleted) {
            LOGGER.debug("image deleted: {}", url);
        } else {
            LOGGER.debug("failed to delete image: {}", url);
        }
    }

    public static void cacheImageAsync(String targetUrl) {
        EXECUTOR.execute(() ->
        {
            Image localImage = IMAGE_CACHE.getImage(targetUrl);
            if (localImage == null) {
                IMAGE_CACHE.add(retrieveImageFromCloud(targetUrl));
            }
        });
    }

    public static void removeFromCacheAsync(ObservableList<ChangeLogEntry> changeLog) {
        if (isNullOrEmpty(changeLog)) {
            return;
        }
        EXECUTOR.execute(() ->
        {
            for (ChangeLogEntry logEntry : changeLog) {
                IMAGE_CACHE.remove("tasks/" + logEntry.getTargetId() + ".jpg");
            }
        });
    }

    public static void loadInto(ImageView imageView, String url, boolean showDefaultPlaceHolder) {
        loadInto(imageView, url, showDefaultPlaceHolder ? IMAGE_PLACE_HOLDER : null);
    }

    public static void loadInto(ImageView imageView, String url, Image placeHolder) {
        if (isNullOrEmpty(url)) {
            imageView.setImage(placeHolder);
            return;
        }

        EXECUTOR.execute(() ->
        {
            Image localImage = IMAGE_CACHE.getImage(url);
            if (localImage != null) {
                imageView.setImage(localImage);

            } else {
                StorableImage cloudImage = retrieveImageFromCloud(url);
                imageView.setImage(cloudImage.get());
                IMAGE_CACHE.add(cloudImage);
            }
        });
    }

    // todo: cleanUp images which have not been used for a while

    private static GluonObservableObject<Image> executeAsync(StorableImage image, Consumer<StorableImage> action) {
        GluonObservableObject<Image> obsImage = new GluonObservableObject<>();

        EXECUTOR.execute(() ->
        {
            try {
                action.accept(image);
                setInitialized(obsImage, image.get(), true);
            } catch (StorageException ex) {
                obsImage.setException(ex);
            }
        });
        return obsImage;
    }

    // public static void shutdownExecutor() {
    // if (executor != null) {
    // executor.shutdown();
    // LOGGER.debug("shutting down executor");
    // }
    // }

    private static class ImageCache {

        private static final String  IMAGES_DIR = "images";
        private final File           imageStore;

        private Cache<String, Image> cache;

        private ImageCache() {
            imageStore = PlatformProvider.getPlatformService().getStorage().getPrivateFile(IMAGES_DIR);
            cache = Services.get(CacheService.class).orElseThrow(() -> new IllegalStateException("Failed to get CacheService")).getCache(
                                                                                                                                         "image_cache");

            LOGGER.debug("imageStore: {}", imageStore);
        }

        public boolean add(StorableImage storableImage) {
            String targetUrl = storableImage.getTargetUrl();

            if (storableImage.get() == null || isNullOrEmpty(targetUrl)) {
                return false;
            }

            File target = new File(imageStore, targetUrl);
            if (target.exists()) {
                LOGGER.debug("image already exists: {}", target);
                return true;
            }
            target.getParentFile().mkdirs();

            try {
                storeImageLocal(storableImage, target);
                cache.put(targetUrl, storableImage.get());

                LOGGER.debug("image cached: {}", targetUrl);
            } catch (IOException e) {
                LOGGER.error("failed to cache image: {}", targetUrl, e);
                return false;
            }
            return true;
        }

        public Image getImage(String url) {
            if (isNullOrEmpty(url)) {
                return null;
            }

            Image cachedImage = cache.get(url);
            if (cachedImage == null) {
                LOGGER.debug("image not found in cache: {}", url);

                Image storedImage = loadLocalImage(url);
                if (storedImage == null) {
                    LOGGER.debug("image not found on disk: {}", url);

                } else {
                    cache.put(url, storedImage);
                    LOGGER.debug("image found on disk, and cached: {}", url);
                    return storedImage;
                }
            }
            return cachedImage;
        }

        public void remove(String url) {
            if (isNullOrEmpty(url)) {
                return;
            }
            cache.remove(url);

            File storedFile = new File(imageStore, url);
            if (storedFile.isFile()) {
                boolean deleted = storedFile.delete();
                if (deleted) {
                    LOGGER.debug("image removed from disk and cache: {}", url);
                } else {
                    LOGGER.debug("failed to delete file: {}", url);
                }
            } else {
                LOGGER.info("failed to delete file: {} (file does not exist, or is not a file)", url);
            }
        }

        private void storeImageLocal(StorableImage storableImage, File target) throws IOException {
            try (FileOutputStream outputStream = new FileOutputStream(target)) {
                outputStream.write(storableImage.getContent());
            }
        }

        private Image loadLocalImage(String url) {
            File file = new File(imageStore, url);
            return file.exists() ? new Image(file.toURI().toString()) : null;
        }

    }
}
