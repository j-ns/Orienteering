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

import static com.jns.orienteering.control.Dialogs.confirmDeleteAnswer;
import static com.jns.orienteering.util.Validators.isNullOrEmpty;

import javax.inject.Inject;

import com.gluonhq.charm.down.common.JavaFXPlatform;
import com.gluonhq.charm.glisten.control.Avatar;
import com.gluonhq.connect.GluonObservable;
import com.gluonhq.connect.GluonObservableObject;
import com.jns.orienteering.common.BaseService;
import com.jns.orienteering.common.ImageHandler;
import com.jns.orienteering.control.Dialogs;
import com.jns.orienteering.control.FloatingTextField;
import com.jns.orienteering.model.persisted.User;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.UserFBRepo;

import javafx.beans.binding.When;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

public class UserPresenter extends BasePresenter {

    @FXML
    private Avatar            avatar;
    @FXML
    private VBox              boxTextFields;
    @FXML
    private FloatingTextField txtUserName;
    @FXML
    private FloatingTextField txtPassword;
    @FXML
    private Button            btnLogInOrLogOff;
    @FXML
    private Button            btnSignUpOrUpdate;
    @FXML
    private Button            btnDeleteUser;

    @Inject
    private BaseService       service;

    private UserFBRepo        userCloudRepo;

    @Override
    protected void initialize() {
        super.initialize();

        avatar.setRadius(38);
        avatar.imageProperty().bind(service.profileImageProperty());

        txtPassword.maskInput();

        btnLogInOrLogOff.setOnAction(e -> onLoginOrLogOff());
        btnLogInOrLogOff.textProperty().bind(
                                             new When(service.userProperty().isNull()).then(localize("view.user.button.login"))
                                                                                      .otherwise(localize("view.user.button.logoff")));

        btnSignUpOrUpdate.setOnAction(e -> showView(ViewRegistry.ACCOUNT));
        btnSignUpOrUpdate.textProperty().bind(
                                              new When(service.userProperty().isNull()).then(localize("view.user.button.signup"))
                                                                                       .otherwise(localize("view.user.button.edit")));

        btnDeleteUser.setOnAction(e -> onDeleteUser());
        btnDeleteUser.visibleProperty().bind(service.userProperty().isNotNull());

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
        platformService().getNodePositionAdjuster(boxTextFields, boxTextFields.getScene().focusOwnerProperty());

        User user = service.getUser() == null ? new User() : service.getUser();
        setFields(user);
    }

    private void setFields(User user) {
        txtUserName.setText(user.getId());
        txtPassword.setText("");
    }

    private void onLoginOrLogOff() {
        if (service.getUser() == null) {
            if (isNullOrEmpty(txtUserName.getText())) {
                Dialogs.ok(localize("view.user.info.enterName")).showAndWait();
                return;
            }
            if (isNullOrEmpty(txtPassword.getText())) {
                Dialogs.ok(localize("view.user.info.enterPassword")).showAndWait();
                return;
            }
            login();
        } else {
            logoff();
        }
    }

    private void login() {
        GluonObservableObject<User> obsUser = userCloudRepo.retrieveObjectAsync(txtUserName.getText());
        AsyncResultReceiver.create(obsUser)
                           .defaultProgressLayer()
                           .onSuccess(resultUser ->
                           {
                               User user = resultUser.get();
                               if (user == null) {
                                   Dialogs.ok(localize("view.user.info.userDoesntExist")).showAndWait();
                                   return;
                               }
                               if (!validatePassword(user.getPassword(), txtPassword.getText())) {
                                   return;
                               }

                               GluonObservableObject<Image> obsImage = ImageHandler.retrieveImageAsync(user.getImageUrl(),
                                                                                                       ImageHandler.AVATAR_PLACE_HOLDER);
                               AsyncResultReceiver.create(obsImage)
                                                  .defaultProgressLayer()
                                                  .onSuccess(resultImage -> service.setProfileImage(resultImage.get()))
                                                  .finalize(() ->
                                                  {
                                                      service.setUser(resultUser.get());
                                                      showHomeView();
                                                      platformService().getInfoService().showToast(localize("view.user.info.userLoggedIn"));
                                                  })
                                                  .start();
                           }).start();

    }

    private boolean validatePassword(String storedPassword, String enteredPassword) {
        if (!enteredPassword.equals(storedPassword)) {
            Dialogs.ok(localize("view.user.info.wrongPassword")).showAndWait();
            return false;
        }
        return true;
    }

    private void logoff() {
        service.setUser(null);
        service.setProfileImage(ImageHandler.AVATAR_PLACE_HOLDER);
        setFields(new User());
    }

    private void onDeleteUser() {
        if (confirmDeleteAnswer(localize("view.user.question.delete")).isYesOrOk()) {
            GluonObservable obsResult = userCloudRepo.deleteAsync(txtUserName.getText());
            AsyncResultReceiver.create(obsResult)
                               .defaultProgressLayer()
                               .onSuccess(e -> logoff())
                               .start();
        }
    }

}