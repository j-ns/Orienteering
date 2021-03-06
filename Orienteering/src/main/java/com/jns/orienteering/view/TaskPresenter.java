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
package com.jns.orienteering.view;

import static com.jns.orienteering.control.Dialogs.confirmDeleteAnswer;
import static com.jns.orienteering.control.Dialogs.showError;
import static com.jns.orienteering.util.Validations.isNotNullOrEmpty;

import java.io.FileNotFoundException;
import java.util.function.Consumer;

import javax.inject.Inject;

import com.gluonhq.charm.down.plugins.Position;
import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.control.ProgressBar;
import com.gluonhq.connect.GluonObservableObject;
import com.jns.orienteering.common.ActivatorDeactivatorService;
import com.jns.orienteering.common.BaseService;
import com.jns.orienteering.common.MultiValidator;
import com.jns.orienteering.common.SingleValidator;
import com.jns.orienteering.control.ChoiceFloatingTextField;
import com.jns.orienteering.control.FloatingTextField;
import com.jns.orienteering.control.Icon;
import com.jns.orienteering.control.Message;
import com.jns.orienteering.control.ScrollListener;
import com.jns.orienteering.control.ScrollPositionBuffer;
import com.jns.orienteering.model.dynamic.CityCache;
import com.jns.orienteering.model.dynamic.MissionCache;
import com.jns.orienteering.model.dynamic.TaskCache;
import com.jns.orienteering.model.persisted.AccessType;
import com.jns.orienteering.model.persisted.City;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.TaskFBRepo;
import com.jns.orienteering.model.repo.image.ImageHandler;
import com.jns.orienteering.model.repo.image.StorableImage;
import com.jns.orienteering.platform.PositionHelper;
import com.jns.orienteering.util.SpecialCharReplacer;
import com.jns.orienteering.util.Validations;

import javafx.animation.PauseTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class TaskPresenter extends BasePresenter {

    private static final Image                  NO_PLACEHOLDER = null;

    @FXML
    private ChoiceFloatingTextField<City>       choiceCity;
    @FXML
    private FloatingTextField                   txtName;
    @FXML
    private FloatingTextField                   txtPosition;
    @FXML
    private TextArea                            txtDescription;
    @FXML
    private Button                              btnTakePicture;
    @FXML
    private Button                              btnSelectPicture;
    @FXML
    private Button                              btnClearPicture;
    @FXML
    private ImageView                           imgView;
    @FXML
    private FloatingTextField                   txtScanCode;
    @FXML
    private FloatingTextField                   txtPoints;
    @FXML
    private ChoiceFloatingTextField<AccessType> choiceAccess;
    @FXML
    private ScrollPane                          scrollPane;
    private ScrollListener                      scrollListener;
    private ScrollPositionBuffer                scrollPositionBuffer;

    private GPSDialog                           gpsDialog;

    @Inject
    private BaseService                         service;
    private TaskFBRepo                          cloudRepo;
    private MultiValidator<String>              taskValidator;

    private Task                                task;
    private ReadOnlyObjectProperty<Position>    position       = platformService().getPositionService().positionProperty();
    private ObjectProperty<Image>               image;
    private boolean                             imageChanged;

    @Override
    protected void initialize() {
        super.initialize();
        initActionBar();

        choiceCity.setStringConverter(City::getCityName);
        choiceCity.setMissingDataMessage(Message.create().title(localize("dialog.error.connectionFailed")));
        choiceCity.setItems(service.getCitiesSorted());

        Label gpsIcon = Icon.GPS_LOCATION.label("22");
        gpsIcon.setPrefWidth(32);
        gpsIcon.setOnMouseClicked(e -> retrieveAndSetLocation());
        txtPosition.setGraphic(gpsIcon);

        txtDescription.setPrefRowCount(4);

        image = platformService().imageProperty();
        image.addListener((ov, i, i1) ->
        {
            imgView.setImage(image.get());
            imageChanged = true;
        });

        btnTakePicture.setGraphic(Icon.CAMERA.icon(Icon.DEFAULT_FONT_SIZE));
        btnTakePicture.setOnAction(e -> platformService().takePicture());

        btnSelectPicture.setGraphic(Icon.PICTURES.icon(Icon.DEFAULT_FONT_SIZE));
        btnSelectPicture.setOnAction(e -> platformService().retrievePicture());

        btnClearPicture.setGraphic(Icon.DELETE.icon(Icon.DEFAULT_FONT_SIZE));
        btnClearPicture.setOnAction(e -> image.set(null));
        btnClearPicture.disableProperty().bind(imgView.imageProperty().isNull());

        imgView.fitWidthProperty().bind(scrollPane.widthProperty());
        imgView.setPreserveRatio(true);

        choiceAccess.setStringConverter(accessType -> localize(accessType));
        choiceAccess.setItems(FXCollections.observableArrayList(AccessType.values()));

        scrollListener = new ScrollListener(scrollPane);
        scrollPositionBuffer = new ScrollPositionBuffer(scrollPane, choiceAccess);

        position = platformService().getPositionService().positionProperty();

        initActivatorsDeactivators();

        cloudRepo = service.getRepoService().getCloudRepo(Task.class);
    }

    private void initActivatorsDeactivators() {
        ActivatorDeactivatorService activatorDeactivatorService = service.getActivatorDeactivatorService();
        String taskViewName = ViewRegistry.TASK.getViewName();

        activatorDeactivatorService.add(taskViewName, scrollListener, scrollPositionBuffer, platformService().getPositionService());

        activatorDeactivatorService.addActivator(taskViewName, () -> platformService().getNodePositionAdjuster(scrollPane, view
                                                                                                                               .getScene()
                                                                                                                               .focusOwnerProperty()));
    }

    @Override
    protected void initAppBar() {
        setAppBar(createBackButton(), localize("view.task.title"));
    }

    private void initActionBar() {
        Button btnSave = Icon.Buttons.actionBarButton(Icon.DONE, localize("label.save"), e -> onSave());
        Button btnSaveAndContinue = Icon.Buttons.actionBarButton(Icon.DONE_ALL, localize("label.saveNext"), e -> onSaveAndContinue());
        Button btnDelete = Icon.Buttons.actionBarButton(Icon.DELETE, localize("label.delete"), e -> onDelete());
        setActionBar(btnSave, btnSaveAndContinue, btnDelete);
    }

    @Override
    protected void onShown() {
        super.onShown();

        task = service.getSelectedTask();
        if (task == null) {
            task = new Task();
            task.setCityId(service.getSelectedCityId());
            setActionBarVisible(true);
        } else {
            boolean userIsOwnerOfTask = task.getOwnerId().equals(service.getUserId());
            setActionBarVisible(userIsOwnerOfTask);
        }
        setFields(task);
    }

    private void setFields(Task task) {
        City city = CityCache.INSTANCE.get(task.getCityId());

        choiceCity.getSelectionModel().select(city);
        txtName.setText(task.getTaskName());
        txtPosition.setText(task.getPositionString());
        txtDescription.setText(task.getDescription());
        txtScanCode.setText(task.getScanCode());
        txtPoints.setText(Integer.toString(task.getPoints()));
        choiceAccess.getSelectionModel().select(task.getAccessType());

        AsyncResultReceiver.create(ImageHandler.retrieveImageAsync(task.getImageUrl(), NO_PLACEHOLDER))
                           .defaultProgressLayer()
                           .onSuccess(result ->
                           {
                               image.set(result.get());
                               imageChanged = false;
                           })
                           .start();
    }

    private void retrieveAndSetLocation() {
        position.addListener(postionListener);
        showGpsDialog();
    }

    private ChangeListener<? super Position> postionListener = (ov, p, p1) ->
    {
        if (p1 != null) {
            String position = Double.toString(p1.getLatitude()) + "," + Double.toString(p1
                                                                                          .getLongitude());
            txtPosition.setText(position);
            gpsDialog.hide();
            gpsDialog = null;
            removePositionListener();
        }
    };

    private void removePositionListener() {
        position.removeListener(postionListener);
    }

    private void showGpsDialog() {
        // todo: Dialog.showAndWait NPE when called twice
        // if (gpsDialog == null) {
        gpsDialog = new GPSDialog();
        // }
        gpsDialog.showAndWait();
    }

    private void onSave() {
        if (!validateTask()) {
            return;
        }
        saveResultReceiver().onSuccess(result ->
        {
            updateTasksList(result.get(), task);
            showPreviousView();
        }).start();
    }

    private void onSaveAndContinue() {
        if (!validateTask()) {
            return;
        }
        saveResultReceiver().onSuccess(result ->
        {
            updateTasksList(result.get(), task);
            task = new Task();
            setFields(task);
        }).start();
    }

    private boolean validateTask() {
        try {
            getPosition();
        } catch (Exception ex) {
            Message msg = Message.create()
                                 .title(localize("view.task.error.invalidCoordinates"))
                                 .text(localize("view.task.error.validCoordinates"));
            showError(msg);
            return false;
        }
        return getTaskValidator().check(txtName.getText());
    }

    private MultiValidator<String> getTaskValidator() {
        if (taskValidator == null) {
            taskValidator = createTaskValidator();
        }
        return taskValidator;
    }

    private MultiValidator<String> createTaskValidator() {
        SingleValidator<String> nameDoesntExistValidator = new SingleValidator<>(name -> !cloudRepo.checkIfTaskNameExists(name),
                                                                                 localize("view.task.info.nameAlreadyExists"));

        MultiValidator<String> validator = new MultiValidator<>();
        validator.addCheck(Validations::isNotNullOrEmpty, localize("view.task.info.taskNameCantBeEmpty"));
        validator.addCheck(e -> choiceCity.getSelectedItem() != null, localize("view.task.info.selectCity")); // possible?
        validator.addCheck(SpecialCharReplacer::validateInput, localize("view.error.invalidCharEntered"));
        validator.addCheck(name ->
        {
            if (isEditorModus()) {
                boolean nameChanged = !task.getTaskName().equals(name);
                if (nameChanged) {
                    return nameDoesntExistValidator.check(name);
                }
                return true;
            } else {
                return nameDoesntExistValidator.check(name);
            }
        });

        return validator;
    }

    private AsyncResultReceiver<GluonObservableObject<Task>> saveResultReceiver() {
        return AsyncResultReceiver.create(saveTask(createTask()))
                                  .defaultProgressLayer()
                                  .exceptionMessage(localize("view.task.error.save"));
    }

    private Task createTask() {
        String cityId = choiceCity.getSelectedItem().getId();
        String name = txtName.getText();
        String description = txtDescription.getText();
        String scanCode = txtScanCode.getText();
        int points = Integer.valueOf(txtPoints.getText());
        AccessType accessType = choiceAccess.getSelectionModel().getSelectedItem();
        boolean createImageId = isEditorModus() ? imageChanged && image.get() != null : image.get() != null;

        Task newTask = new Task(cityId, name, description, getPosition(), points, accessType, service.getUser().getId(), createImageId);
        newTask.setScanCode(scanCode);

        if (isEditorModus()) {
            newTask.setId(task.getId());
            newTask.setPreviousTask(task);

            if (!imageChanged) {
                newTask.setImageId(service.getSelectedTask().getImageId());
            }
        }
        return newTask;
    }

    private Position getPosition() {
        String positionText = txtPosition.getText();
        if (isNotNullOrEmpty(positionText)) {
            return PositionHelper.toPosition(positionText);
        }
        return new Position(0, 0);
    }

    private GluonObservableObject<Task> saveTask(Task newTask) {
        GluonObservableObject<Task> obsTask = null;

        if (isEditorModus()) {
            Image _image = image.get();
            String previousImageId = imageChanged && _image == null ? task.getImageId() : null;

            obsTask = cloudRepo.updateTaskAsync(newTask, previousImageId);

            if (imageChanged) {
                if (_image == null) {
                    ImageHandler.deleteImageAsync(task.getImageUrl());
                } else {
                    saveImage(_image, newTask.getImageUrl(), sImage -> ImageHandler.updateImageAsync(sImage, task.getImageUrl()));
                }
            }

        } else {
            obsTask = cloudRepo.createTaskAsync(newTask);
            AsyncResultReceiver.create(obsTask)
                               .defaultProgressLayer()
                               .onSuccess(e ->
                               {
                                   if (image.get() != null) {
                                       saveImage(image.get(), newTask.getImageUrl(), ImageHandler::storeImageAsync);
                                   }
                               })
                               .start();
        }

        return obsTask;
    }

    private void saveImage(Image image, String imageUrl, Consumer<StorableImage> imageHandler) {
        try {
            StorableImage storableImage = new StorableImage(platformService().getImageInputStream(), image, imageUrl);
            imageHandler.accept(storableImage);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void updateTasksList(Task newTask, Task previousTask) {
        if (isEditorModus()) {
            TaskCache.INSTANCE.updateItem(newTask, previousTask);

            if (service.activeMissionContainsTask(task)) {
                service.setActiveMission(null);
            }
        } else {
            TaskCache.INSTANCE.addItem(newTask);
        }

        if (isEditorModus() && isMissionEditorModus()) {
            MissionCache.INSTANCE.udpateMissionTask(newTask, previousTask);
        }
    }

    private void onDelete() {
        if (!confirmDeleteAnswer(localize("view.tasks.question.deleteTask")).isYesOrOk()) {
            return;
        }

        if (task.getId() == null) {
            showPreviousView();
            return;
        }

        GluonObservableObject<Task> obsTask = cloudRepo.deleteTaskAsync(task);
        AsyncResultReceiver.create(obsTask)
                           .defaultProgressLayer()
                           .onSuccess(e ->
                           {
                               TaskCache.INSTANCE.removeItem(task);

                               if (service.activeMissionContainsTask(task)) {
                                   service.setActiveMission(null);
                               }
                               if (isMissionEditorModus()) {
                                   MissionCache.INSTANCE.removeTask(task);
                               }
                               showPreviousView();
                           })
                           .exceptionMessage(localize("view.task.error.delete"))
                           .start();
    }

    private boolean isEditorModus() {
        return service.getSelectedTask() != null;
    }

    private boolean isMissionEditorModus() {
        return service.getSelectedMission() != null;
    }

    @Override
    protected void onHidden() {
        service.setSelectedTask(null);
        image.set(null);
        clearFields();
    }

    private void clearFields() {
        txtName.setText("");
        txtPosition.setText("");
        txtDescription.setText("");
        txtPoints.setText("");
        choiceAccess.getSelectionModel().select(AccessType.PRIVATE);
    }

    private class GPSDialog {

        private Dialog<ButtonType> gpsDialog = new Dialog<>();

        private GPSDialog() {
            Label lblTitle = new Label(localize("view.task.info.tryToGetLocation"));

            ProgressBar progressBar = new ProgressBar();
            progressBar.setPrefWidth(200);
            HBox boxContent = new HBox(progressBar);
            boxContent.setAlignment(Pos.CENTER);

            gpsDialog.setTitle(lblTitle);
            gpsDialog.setContent(boxContent);

            Button btnCancelOk = new Button(localize("button.cancel"));
            btnCancelOk.setOnAction(e ->
            {
                gpsDialog.setResult(ButtonType.CANCEL);
                gpsDialog.hide();
                removePositionListener();
            });

            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(20));
            pauseTransition.setOnFinished(e ->
            {
                if (position.get() == null) {
                    lblTitle.setText(localize("view.task.info.couldNotGetGpsLocation"));
                    btnCancelOk.setText(localize("button.ok"));
                    progressBar.setProgress(0);
                    removePositionListener();
                }
            });

            gpsDialog.getButtons().setAll(btnCancelOk);
            gpsDialog.setOnShowing(evt -> pauseTransition.play());
        }

        public void hide() {
            gpsDialog.hide();
        }

        public void showAndWait() {
            gpsDialog.showAndWait();
        }

    }
}