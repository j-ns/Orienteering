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
package com.jns.orienteering.platform;

import static android.app.Activity.RESULT_OK;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jns.orienteering.util.DateTimeFormatters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafxports.android.FXActivity;

public class AndroidPlatform implements PlatformService {

    private static final Logger     LOGGER                       = LoggerFactory.getLogger(AndroidPlatform.class);

    private static final long[]     VIBRATOR_PATTERN             = { 0, 1000, 500, 1000 };

    private static final int        SELECT_PICTURE               = 1;
    private static final int        TAKE_PICTURE                 = 2;

    private static final int        MARSHMALLOW                  = 23;

    private static final String     KEY_PERMISSIONS              = "permissions";
    private static final String     KEY_REQUEST_CODE             = "requestCode";
    private static final int        REQUEST_CODE_ASK_PERMISSIONS = 246;

    private Storage                 storage;

    private InfoService             infoService;

    private PositionServiceExtended positionService;

    private Vibrator                vibrator;

    private NodePositionAdjuster    nodePositionAdjuster;

    private ObjectProperty<Image>   image;

    private Uri                     imageUri;

    public AndroidPlatform() {
        super();
    }

    @Override
    public void checkPermissions() {
        if (Build.VERSION.SDK_INT >= MARSHMALLOW) {
            FXActivity activity = FXActivity.getInstance();

            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Intent permIntent = new Intent(activity, PermissionRequestActivity.class);
                permIntent.putExtra(KEY_PERMISSIONS, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
                        Manifest.permission.INTERNET });
                permIntent.putExtra(KEY_REQUEST_CODE, 11111);
                activity.startActivityForResult(permIntent, REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
        }
    }

    @Override
    public Storage getStorage() {
        if (storage == null) {
            storage = new AndroidStorage();
        }
        return storage;
    }

    @Override
    public InfoService getInfoService() {
        if (infoService == null) {
            infoService = new AndroidToastService();
        }
        return infoService;
    }

    @Override
    public PositionServiceExtended getPositionService() {
        if (positionService == null) {
            positionService = new AndroidPositionService();
        }
        return positionService;
    }

    @Override
    public NodePositionAdjuster getNodePositionAdjuster(Parent parent, ObservableValue<Node> focusOwner) {
//        if (nodePositionAdjuster == null) {
//            nodePositionAdjuster = new AndroidNodePositionAdjuster(parent, focusOwner);
//        } else {
//            nodePositionAdjuster.update(parent, focusOwner);
//        }
        return nodePositionAdjuster;
    }

    @Override
    public void removeNodePositionAdjuster() {
        if (nodePositionAdjuster != null) {
            nodePositionAdjuster.removeListeners();
            nodePositionAdjuster = null;
        }
    }

    @Override
    public Object getLaunchIntentExtra(String name, Object defaultValue) {
        Intent intent = FXActivity.getInstance().getIntent();

        if (defaultValue instanceof Boolean) {
            Boolean defaultVal = (Boolean) defaultValue;
            return intent.getBooleanExtra(name, defaultVal);
        }
        return defaultValue;
    }

    @Override
    public void sendEmail(String[] addresses, String subject, File attachment) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(attachment));

        if (intent.resolveActivity(FXActivity.getInstance().getPackageManager()) != null) {
            FXActivity.getInstance().startActivity(intent);
        }
    }

    @Override
    public void vibrate() {
        if (vibrator == null) {
            vibrator = (Vibrator) FXActivity.getInstance().getSystemService(Context.VIBRATOR_SERVICE);
        }
        vibrator.vibrate(VIBRATOR_PATTERN, -1);
    }

    @Override
    public void playRingtone() {
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }
        Ringtone ringtone = RingtoneManager.getRingtone(FXActivity.getInstance(), alarmUri);
        ringtone.play();
    }

    @Override
    public void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(FXActivity.getInstance().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                LOGGER.error("GalleryActivity", "create image file failed " + ex.getMessage());

            }
            if (photoFile != null) {
                final Uri fromFile = Uri.fromFile(photoFile);
                imageUri = fromFile;
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fromFile);

                FXActivity.getInstance().setOnActivityResultHandler((requestCode, resultCode, data) ->
                {
                    if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
                        LOGGER.debug("took picture: {}", fromFile);
                        imageProperty().set(new Image(fromFile.toString()));
                    }
                });

                FXActivity.getInstance().startActivityForResult(intent, TAKE_PICTURE);
            }
        } else {
            LOGGER.error("GalleryActivity", "resolveActivity failed");
        }
    }

    @Override
    public void retrievePicture() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(FXActivity.getInstance().getPackageManager()) != null) {
            FXActivity.getInstance().setOnActivityResultHandler((requestCode, resultCode, data) ->
            {
                if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
                    InputStream is = null;
                    try {
                        Uri selectedImageUri = data.getData();
                        imageUri = selectedImageUri;
                        is = FXActivity.getInstance().getContentResolver().openInputStream(selectedImageUri);
                        LOGGER.debug("new Imgage: {}", selectedImageUri);
                        imageProperty().set(new Image(is));

                    } catch (FileNotFoundException ex) {
                        LOGGER.error("GalleryActivity resolveActivity failed", ex);
                    } finally {
                        try {
                            if (is != null) {
                                is.close();
                            }
                        } catch (IOException ex) {
                        }
                    }
                }
            });

            FXActivity.getInstance().startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
        } else {
            LOGGER.error("GalleryActivity resolveActivity failed");
        }
    }

    @Override
    public FileInputStream getImageInputStream() throws FileNotFoundException {
        return (FileInputStream) FXActivity.getInstance().getContentResolver().openInputStream(imageUri);
    }

    @Override
    public ObjectProperty<Image> imageProperty() {
        if (image == null) {
            image = new SimpleObjectProperty<>();
        }
        return image;
    }

    private File createImageFile() throws IOException {
        String timeStamp = DateTimeFormatters.createTimeStamp();
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    public static class PermissionRequestActivity extends Activity {

        private String[] permissions;
        private int      requestCode;

        @Override
        public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
            FXActivity.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
            finish();
        }

        @Override
        protected void onStart() {
            super.onStart();
            permissions = this.getIntent().getStringArrayExtra(KEY_PERMISSIONS);
            requestCode = this.getIntent().getIntExtra(KEY_REQUEST_CODE, 0);

            ActivityCompat.requestPermissions(this, permissions, requestCode);
        }
    }

}