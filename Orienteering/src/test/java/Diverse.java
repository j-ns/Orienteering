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
import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.connect.provider.RestClient;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.storage.StorageScopes;
import com.google.cloud.AuthCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.jns.orienteering.OrienteeringApp;
import com.jns.orienteering.common.ImageHandler;
import com.jns.orienteering.common.Validator;
import com.jns.orienteering.model.common.FireBaseStorage;
import com.jns.orienteering.model.common.StorableImage;
import com.jns.orienteering.util.DateTimeFormatters;
import com.jns.orienteering.util.SpecialCharReplacer;
import com.jns.orienteering.util.Validators;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Diverse {

    @Rule
    public JavaFXThreadingRule    javafxRule      = new JavaFXThreadingRule();

    private static final Logger   LOGGER          = LoggerFactory.getLogger(Diverse.class);

    private static final String   APP_ID          = "https://orienteering-2dd97.firebaseio.com";
    protected static final String JSON_SUFFIX     = ".json";
    private static final String   AUTH_PARAM_NAME = "auth";
    protected static final String CREDENTIALS     = "2ekET9SyGxrYCeSWgPZaWdiCHxncCHmAvGCjDjwu";

    @Test
    public void regex() {
        String imageId = "tasks/23984-sdf234.jpg";
        String replaced = imageId.replaceAll("(^tasks/)?(\\.[a-z]{3,4})$?", "");
        LOGGER.debug("replaced: {}", replaced);
    }

    private static final Pattern                GPS_PATTERN           = Pattern.compile(
            "^([-+]?)([\\d]{1,2})(((\\.)(\\d+\\s*)(,\\s*)))(([-+]?)([\\d]{1,3})((\\.)(\\d+))?)$");



    @Test
    public void validateGPSData() {
     String test = "21.32353445345,  1.3";
     test = test.replaceAll("\\s", "");

     boolean check1 = Validators.isNotNullOrEmpty(test);
     boolean matches = GPS_PATTERN.matcher(test).matches();
     assertThat(matches).isTrue();

//        Validator<Object> validator = new Validator<>(e ->
//        test != null || !GPS_PATTERN.matcher(test).matches(), "error");
//        boolean check = validator.check("");
//        LOGGER.debug("check: {}", check);
    }

    @Test
    public void replaceChars() {
        String input = "Äe iou";
        String result = SpecialCharReplacer.replaceSpecialChars(input);
        LOGGER.debug("result: {}", result);

        result = SpecialCharReplacer.replaceWithSpecialChars(input);
        LOGGER.debug("result: {}", result);
    }

    @Test
    public void createTimeStamp() {
        String timeStamp = DateTimeFormatters.formatTimeStamp(LocalDateTime.now());
        LOGGER.debug("timeStamp: {}", timeStamp);
    }

    private RestClient createRestClient() {
        // RestClient client = RestClient.create().host("http://storage.googleapis.com/orienteering-2dd97.appspot.com");
        RestClient client = RestClient.create().host("https://www.googleapis.com/upload/storage/v1/b/orienteering-2dd97.appspot.com/o");
        // client.queryParam(AUTH_PARAM_NAME, CREDENTIALS);
        // client.queryParam("print", "pretty");
        return client;
    }

    @Test
    public void test() {
        String url = filterString("timeStamp");
        LOGGER.debug("url: {}", url);
    }

    private String filterString(String text) {
        return "\"" + text + "\"";
    }

    // @Test
    // public void cloudStorage() throws IOException {
    // String androidKey = "AIzaSyCqwU128pKACSMCER6RZILlkwL6wwhIPkg";
    // String browserKey = "AIzaSyAG5cW_O5ByMVUuW2j7JEn7k3sP-90QV80";
    // String serverKey = "AIzaSyApk42Hv1tkegjJAcPXAhDYNPkJIYfUghs";
    //
    // String clientSecret = "pWyovnujBm4uGsd99Zs_vfPG";
    //
    // List<String> scopes = Arrays.asList("https://www.googleapis.com/auth/devstorage.read_write");
    //
    // String host = "www.googleapis.com";
    // // /upload/storage/v1/b/orienteering-2dd97.appspot.com/o
    //
    // HttpUrl httpUrl = new HttpUrl.Builder()
    // .scheme("https")
    // .host(host)
    // .addPathSegments("upload/storage/v1/b/orienteering-2dd97.appspot.com/o")
    // .addQueryParameter("uploadType", "media")
    // .addQueryParameter("name", "idJnsProfile")
    // .addQueryParameter("key", androidKey)
    // .build();
    //
    // File file = new File(getClass().getResource("/images/compass.png").getFile());
    //
    // RequestBody requestBody = RequestBody.create(MediaType.parse("image/png"), file);
    //
    // Request request = new Request.Builder().url(httpUrl)
    // .post(requestBody)
    // .addHeader("Content-Type", "image/png")
    // .addHeader("Content-Length", String.valueOf(file.length()))
    // .build();
    //
    // OkHttpClient client = new OkHttpClient();
    // Response response = client.newCall(request).execute();
    //
    // LOGGER.debug("response {}", response);
    //
    // // POST https://www.googleapis.com/upload/storage/v1/b/myBucket/o?uploadType=media&name=myObject HTTP/1.1
    // // Content-Type: image/jpeg
    // // Content-Length: [NUMBER_OF_BYTES_IN_FILE]
    // // Authorization: Bearer [YOUR_AUTH_TOKEN]
    // //
    // // [JPEG_DATA]
    //
    // }

    // @Test
    // public void getToken() throws IOException {
    // String host = "accounts.google.com";
    //
    // HttpUrl url = new HttpUrl.Builder()
    // .scheme("https")
    // .host(host)
    // .addPathSegments("o/oauth2/v2/auth")
    // .addQueryParameter("scope", "email%20profile")
    // .addQueryParameter("redirect_uri", "urn:ietf:wg:oauth:2.0:oob")
    // .addQueryParameter("response_type", "code")
    // .addQueryParameter("client_id", "361082435566-9f8rphhvill6ke9cdh1hp91ountsspo1.apps.googleusercontent.com")
    // .build();
    //
    // Request request = new Request.Builder().url(url)
    // .get().build();
    //
    // Response response = new OkHttpClient().newCall(request).execute();
    // LOGGER.debug("response {}", response);
    //
    // // https://accounts.google.com/o/oauth2/v2/auth?
    // // scope=email%20profile&
    // // redirect_uri=http://127.0.0.1:9004&
    // // response_type=code&
    // // client_id=812741506391-h38jh0j4fv0ce1krdkiq0hfvt6n5amrf.apps.googleusercontent.com
    // }

    @Test
    public void buildService() throws IOException, GeneralSecurityException {
        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        GoogleCredential credential = GoogleCredential.fromStream(Diverse.class.getResourceAsStream("/service_secret.json"), transport, jsonFactory);

        // Depending on the environment that provides the default credentials (for
        // example: Compute Engine, App Engine), the credentials may require us to
        // specify the scopes we need explicitly. Check for this case, and inject
        // the Cloud Storage scope if required.
        if (credential.createScopedRequired()) {
            Collection<String> scopes = StorageScopes.all();
            credential = credential.createScoped(scopes);
        }

        Storage storage = StorageOptions.builder().projectId("orienteering-2dd97")
                                        .authCredentials(AuthCredentials.createForJson(Diverse.class.getResourceAsStream("/service_secret.json")))
                                        .projectId("orienteering-2dd97")
                                        .build()
                                        .service();

        String accessToken = credential.getAccessToken();
        LOGGER.debug("token {}", accessToken);
    }

    @Test
    public void storageCreate() throws IOException {
        FireBaseStorage storage = FireBaseStorage.INSTANCE;

        byte[] content = imageToByteArray();
        storage.create(content, "tasks/compass2.png");
    }

    @Test
    public void storageDelete() {
        FireBaseStorage storage = FireBaseStorage.INSTANCE;
        storage.delete("tasks/compass.png");
    }

//    @Test
//    public void storageUpdate() throws IOException {
//        FireBaseStorage storage = FireBaseStorage.INSTANCE;
//        storage.update(imageToByteArray(), "tasks/compass.png", "tasks/compass2.png");
//    }

    private byte[] imageToByteArray() throws IOException {
        InputStream in = getClass().getResourceAsStream("/images/compass.png");

        BufferedImage img = ImageIO.read(in);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        baos.flush();
        baos.close();

        return baos.toByteArray();
    }


    @Test
    public void imageHandlerStoreImage() {
        String source = OrienteeringApp.class.getResource("/images/compass.png").getFile();

        StorableImage storableImage = new StorableImage(getImage(), "test2.jpg");
        ImageHandler.storeImage(storableImage);
    }

    @Test
    public void imageHandlerLoadImage() {
        ImageView imageView = new ImageView();
        Scene scene = new Scene(new StackPane(imageView));
        Stage stage = new Stage();
        stage.setScene(scene);

         StorableImage image = ImageHandler.retrieveImageFromCloud("createTest.png");
//        String source = OrienteeringApp.class.getResource("/images/compass.png").getFile();

        stage.show();
//        Image image = ImageCache.getImage("test.jpg");
        imageView.setImage(image.get());
    }


    @Test
    public void imageHandlerDelete() {
        ImageHandler.deleteImage("tasks/compass4.png");
    }

    private Image getImage() {
        return new Image(this.getClass().getResourceAsStream("/images/compass.png"));
    }
}
