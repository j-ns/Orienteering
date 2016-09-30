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
package com.jns.orienteering.view;

import static com.jns.orienteering.util.Validators.isNullOrEmpty;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.Consumer;

import javax.inject.Inject;

import com.gluonhq.charm.down.common.JavaFXPlatform;
import com.gluonhq.charm.glisten.control.Avatar;
import com.gluonhq.connect.GluonObservableObject;
import com.jns.orienteering.common.BaseService;
import com.jns.orienteering.common.ImageHandler;
import com.jns.orienteering.control.ChoiceFloatingTextField;
import com.jns.orienteering.control.FloatingTextField;
import com.jns.orienteering.model.common.StorableImage;
import com.jns.orienteering.model.persisted.City;
import com.jns.orienteering.model.persisted.User;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.UserFBRepo;
import com.jns.orienteering.util.Dialogs;
import com.jns.orienteering.util.Icon;

import javafx.beans.binding.When;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

public class AccountPresenter extends BasePresenter {

    @FXML
    private Avatar                        avatar;
    @FXML
    private Button                        btnCamera;
    @FXML
    private Button                        btnPictures;

    @FXML
    private ScrollPane                    scrollPane;
    @FXML
    private VBox                          boxCenter;
    @FXML
    private FloatingTextField             txtUserName;
    @FXML
    private FloatingTextField             txtAlias;
    @FXML
    private FloatingTextField             txtEmailAdress;
    @FXML
    private FloatingTextField             txtPassword;
    @FXML
    private FloatingTextField             txtPasswordNew;
    @FXML
    private FloatingTextField             txtPasswordConfirmation;
    @FXML
    private ChoiceFloatingTextField<City> choiceDefaultCity;

    @FXML
    private Button                        btnSignUp;

    @Inject
    private BaseService                   service;
    private UserFBRepo                    userCloudRepo;

    private ObjectProperty<Image>         image = new SimpleObjectProperty<>();
    private boolean                       imageChanged;
    private String                        storedPassword;

    @Override
    protected void initialize() {
        super.initialize();

        avatar.setRadius(38);

        btnCamera.setGraphic(Icon.CAMERA.icon("20"));
        btnCamera.setOnAction(e -> platformService().takePicture());

        btnPictures.setGraphic(Icon.PICTURES.icon("20"));
        btnPictures.setOnAction(e -> platformService().retrievePicture());

        image.bind(platformService().imageProperty());
        image.addListener(l ->
        {
            avatar.setImage(getImage());
            imageChanged = true;
        });

        txtPassword.maskInput();
        txtPasswordNew.maskInput();
        txtPasswordConfirmation.maskInput();

        btnSignUp.setOnAction(e -> onSignUpOrUpdate());
        btnSignUp.textProperty().bind(new When(service.userProperty().isNotNull())
                                                                                  .then(localize("view.account.button.update"))
                                                                                  .otherwise(localize("view.account.button.signup")));

        choiceDefaultCity.setStringConverter(City::getCityName);
        choiceDefaultCity.setMissingDataTitle(localize("view.account.info.noCityExisting"));
        choiceDefaultCity.setItems(service.getCities());

        userCloudRepo = service.getRepoService().getCloudRepo(User.class);

        // test:
        if (JavaFXPlatform.isDesktop()) {
            view.addEventHandler(KeyEvent.KEY_RELEASED, evt ->
            {
                if (evt.getCode() == KeyCode.ESCAPE) {
                    showPreviousView();
                }
            });
        }
    }

    @Override
    protected void initAppBar() {
        showAppBar(false);
    }

    @Override
    protected void onShowing() {
        super.onShowing();
        platformService().getNodePositionAdjuster(scrollPane, scrollPane.getScene().focusOwnerProperty());

        imageChanged = false;

        if (service.getUser() != null) {
            if (boxCenter.getChildren().contains(txtUserName)) {
                boxCenter.getChildren().remove(txtUserName);
            }
            if (!boxCenter.getChildren().contains(txtPasswordNew)) {
                boxCenter.getChildren().add(txtPasswordNew);
            }
            setFields(service.getUser());

        } else {
            if (!boxCenter.getChildren().contains(txtUserName)) {
                boxCenter.getChildren().add(0, txtUserName);
            }
            if (boxCenter.getChildren().contains(txtPasswordNew)) {
                boxCenter.getChildren().remove(txtPasswordNew);
            }
            setFields(new User());
        }
    }

    private void setFields(User user) {
        avatar.setImage(service.getProfileImage());
        txtUserName.setText(user.getId());
        txtAlias.setText(user.getAlias());
        txtEmailAdress.setText(user.getEmailAdress());
        choiceDefaultCity.getSelectionModel().select(user.getDefaultCity());
        txtPassword.setText("");
        txtPasswordNew.setText("");
        txtPasswordConfirmation.setText("");
    }

    private void onSignUpOrUpdate() {
        if (!validatePassword()) {
            return;
        }
        if (service.getUser() != null) {
            update();
        } else {
            signUp();
        }
    }

    private void update() {
        User updatedUser = createUser();
        if (!imageChanged) {
            updatedUser.setImageId(service.getUser().getImageId());
        }

        updatedUser.setActiveMission(service.getActiveMission());

        GluonObservableObject<User> obsUser = userCloudRepo.createOrUpdateAsync(updatedUser, updatedUser.getId());
        AsyncResultReceiver.create(obsUser)
                           .defaultProgressLayer()
                           .onSuccess(result ->
                           {
                               if (imageChanged) {
                                   Image savedImage = saveImage(getImage(), updatedUser.getImageUrl(), sImage -> ImageHandler
                                                                                                                             .updateImageAsync(sImage,
                                                                                                                                               service.getUser()
                                                                                                                                                      .getImageUrl()));
                                   service.setProfileImage(savedImage);
                               }

                               service.setUser(result.get());
                               showPreviousView();
                               platformService().getInfoService().showToast(localize("view.account.info.userUpdated"));
                           })
                           .exceptionMessage(localize("view.account.error.updateUser"))
                           .start();
    }

    private void signUp() {
        if (!validateUserIsNew()) {
            return;
        }
        User newUser = createUser();

        GluonObservableObject<User> obsUser = userCloudRepo.createOrUpdateAsync(newUser, newUser.getId());
        AsyncResultReceiver.create(obsUser)
                           .defaultProgressLayer()
                           .onSuccess(result ->
                           {
                               if (image.get() != null) {
                                   Image savedImage = saveImage(getImage(), newUser.getImageUrl(), ImageHandler::storeImageAsync);
                                   if (savedImage != null) {
                                       service.setProfileImage(getImage());
                                   }
                               }

                               service.setUser(result.get());
                               showView(ViewRegistry.HOME);
                               platformService().getInfoService().showToast(localize("view.account.info.userSignedUp"));
                           })
                           .exceptionMessage(localize("view.account.error.signupUser"))
                           .start();
    }

    private Image saveImage(Image image, String imageUrl, Consumer<StorableImage> imageHandler) {
        try {
            StorableImage storableImage = new StorableImage(platformService().getImageInputStream(), image, imageUrl);
            imageHandler.accept(storableImage);
            return image;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean validateUserIsNew() {
        boolean userExists = userCloudRepo.checkIfUserExists(txtUserName.getText());
        if (userExists) {
            Dialogs.ok(localize("view.account.info.userAlreadyExists")).showAndWait();
            return false;
        }
        return true;
    }

    private boolean validatePassword() {
        String password = txtPassword.getText();
        String passwordNew = txtPasswordNew.getText();
        String passwordConfirmation = txtPasswordConfirmation.getText();

        if (isNullOrEmpty(password)) {
            Dialogs.ok(localize("view.account.info.enterPassword")).showAndWait();
            return false;
        }

        if (isUpdateModus()) {
            try {
                User user = userCloudRepo.retrieveObject(service.getUserId());
                storedPassword = user.getPassword();

                if (!password.equals(storedPassword)) {
                    Dialogs.ok(localize("view.account.info.wrongPassword")).showAndWait();
                    return false;
                }
            } catch (IOException e) {
                Dialogs.ok(localize("view.account.error.retrieveUser")).showAndWait();
                return false;
            }

            boolean changePassword = !isNullOrEmpty(passwordNew);
            if (changePassword) {
                if (isNullOrEmpty(passwordConfirmation)) {
                    Dialogs.ok(localize("view.account.info.confirmPassword")).showAndWait();
                    return false;
                }
                if (passwordNew.compareTo(passwordConfirmation) != 0) {
                    Dialogs.ok(localize("view.account.info.passwordsDontMatch")).showAndWait();
                    return false;
                }
            }
        } else {
            if (isNullOrEmpty(passwordConfirmation)) {
                Dialogs.ok(localize("view.account.info.confirmPassword")).showAndWait();
                return false;
            }
            if (password.compareTo(passwordConfirmation) != 0) {
                Dialogs.ok(localize("view.account.info.passwordsDontMatch")).showAndWait();
                return false;
            }

        }
        return true;
    }

    private User createUser() {
        String password = isUpdateModus() ? storedPassword : txtPassword.getText();

        return new User(txtUserName.getText(),
                        txtAlias.getText(),
                        txtEmailAdress.getText(),
                        password,
                        choiceDefaultCity.getSelectedItem());
    }

    private boolean isUpdateModus() {
        return service.getUser() != null;
    }

    private Image getImage() {
        return image.get();
    }

    @Override
    protected void onHidden() {
        storedPassword = null;
    }

}