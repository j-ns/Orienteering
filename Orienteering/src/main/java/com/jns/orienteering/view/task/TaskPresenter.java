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
package com.jns.orienteering.view.task;

import static com.jns.orienteering.util.Dialogs.confirmDeleteAnswer;
import static com.jns.orienteering.util.Validators.isNotNullOrEmpty;
import static com.jns.orienteering.util.Validators.isNullOrEmpty;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.down.common.Position;
import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.control.ProgressBar;
import com.gluonhq.connect.GluonObservableObject;
import com.jns.orienteering.OrienteeringApp;
import com.jns.orienteering.common.ActivatorDeactivatorService;
import com.jns.orienteering.common.BaseService;
import com.jns.orienteering.common.ImageHandler;
import com.jns.orienteering.common.MultiValidator;
import com.jns.orienteering.common.Validator;
import com.jns.orienteering.control.ChoiceFloatingTextField;
import com.jns.orienteering.control.FloatingTextField;
import com.jns.orienteering.control.ScrollListener;
import com.jns.orienteering.control.ScrollPositionBuffer;
import com.jns.orienteering.model.CityHolder;
import com.jns.orienteering.model.common.AccessType;
import com.jns.orienteering.model.common.ListUpdater;
import com.jns.orienteering.model.common.StorableImage;
import com.jns.orienteering.model.persisted.City;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.TaskFBRepo;
import com.jns.orienteering.util.GluonObservableHelper;
import com.jns.orienteering.util.Icon;
import com.jns.orienteering.util.Validators;
import com.jns.orienteering.view.common.BasePresenter;

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

    private static final Logger                 LOGGER                = LoggerFactory.getLogger(TaskPresenter.class);

    private static final String                 TASKS_UPDATER         = "tasks_updater";
    private static final String                 MISSION_TASKS_UPDATER = "mission_tasks_updater";

    private static final Pattern                GPS_PATTERN           = Pattern.compile(
                                                                                        "^([-+]?)([\\d]{1,2})(((\\.)(\\d+\\s*)(,\\s*)))(([-+]?)([\\d]{1,3})((\\.)(\\d+))?)$");

    private static final Image                  NO_PLACE_HOLDER       = null;

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

    private Dialog<ButtonType>                  gpsDialog;

    @Inject
    private BaseService                         service;
    private ExecutorService                     executor;
    private TaskFBRepo                          cloudRepo;

    private Task                                task;
    private ReadOnlyObjectProperty<Position>    position              = platformService().getPositionService().positionProperty();
    private ObjectProperty<Image>               image;
    private boolean                             imageChanged;

    @Override
    protected void initialize() {
        super.initialize();
        cloudRepo = service.getRepoService().getCloudRepo(Task.class);

        initActionBar();

        choiceCity.setStringConverter(City::getCityName);
        choiceCity.setItems(service.getCities());

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
            LOGGER.debug("imageChanged == true");
        });

        btnTakePicture.setGraphic(Icon.CAMERA.icon(Icon.DEFAULT_ICON_SIZE));
        btnTakePicture.setOnAction(e -> platformService().takePicture());

        btnSelectPicture.setGraphic(Icon.PICTURES.icon(Icon.DEFAULT_ICON_SIZE));
        btnSelectPicture.setOnAction(e -> platformService().retrievePicture());

        btnClearPicture.setGraphic(Icon.DELETE.icon(Icon.DEFAULT_ICON_SIZE));
        btnClearPicture.setOnAction(e -> image.set(null));
        btnClearPicture.disableProperty().bind(imgView.imageProperty().isNull());

        imgView.fitWidthProperty().bind(scrollPane.widthProperty());
        imgView.setPreserveRatio(true);

        choiceAccess.setStringConverter(t -> localize(t));
        choiceAccess.setItems(FXCollections.observableArrayList(AccessType.values()));

        scrollListener = new ScrollListener(scrollPane);
        scrollPositionBuffer = new ScrollPositionBuffer(scrollPane, choiceAccess);

        ActivatorDeactivatorService activatorDeactivatorService = service.getActivatorDeactivatorService();
        activatorDeactivatorService.add(OrienteeringApp.TASK_VIEW, scrollListener);
        activatorDeactivatorService.add(OrienteeringApp.TASK_VIEW, scrollPositionBuffer);
        activatorDeactivatorService.addActivator(OrienteeringApp.TASK_VIEW, () -> platformService().getNodePositionAdjuster(scrollPane, view
                                                                                                                                            .getScene()
                                                                                                                                            .focusOwnerProperty()));
        activatorDeactivatorService.addDeactivator(OrienteeringApp.TASK_VIEW,
                                                   () ->
                                                   {
                                                       platformService().removeNodePositionAdjuster();
                                                       platformService().getPositionService().stopLocationListener();
                                                       if (position != null) {
                                                           position.removeListener(postionListener);
                                                       }
                                                   });
    }

    @Override
    protected void initAppBar() {
        setAppBar(createBackButton(), localize("view.task.title"));
    }

    private void initActionBar() {
        Button btnSave = Icon.Buttons.save(e -> onSave());
        Button btnSaveAndContinue = Icon.Buttons.saveAndContinue(e -> onSaveAndContinue());
        Button btnDelete = Icon.Buttons.delete(e -> onDelete());
        setActionBar(btnSave, btnSaveAndContinue, btnDelete);
    }

    @Override
    protected void onShown() {
        super.onShown();

        task = service.getSelectedTask();
        if (task == null) {
            task = new Task();
            setActionBarVisible(true);
        } else {
            boolean userIsOwnerOfTask = task.getOwnerId().equals(service.getUserId());
            setActionBarVisible(userIsOwnerOfTask);
        }
        setFields(task);
    }

    private void setFields(Task task) {
        City city = task.getCityId() != null ? CityHolder.get(task.getCityId()) : service.getSelectedCity();

        choiceCity.getSelectionModel().select(city);
        txtName.setText(task.getTaskName());
        txtPosition.setText(task.getPositionString());
        txtDescription.setText(task.getDescription());
        AsyncResultReceiver.create(ImageHandler.retrieveImageAsync(task.getImageUrl(), NO_PLACE_HOLDER))
                           .defaultProgressLayer()
                           .onSuccess(result ->
                           {
                               image.set(result.get());
                               imageChanged = false;
                               LOGGER.debug("imageChanged == {}", imageChanged);
                           })
                           .start();

        txtScanCode.setText(task.getScanCode());
        txtPoints.setText(Integer.toString(task.getPoints()));
        choiceAccess.getSelectionModel().select(task.getAccessType());
    }

    private void retrieveAndSetLocation() {
        position.addListener(postionListener);
        showGpsDialog();
    }

    private ChangeListener<? super Position> postionListener = (ov, p, p1) ->
    {
        if (p1 != null) {
            String position = Double.toString(p1.getLatitude()) + "," + Double.toString(p1.getLongitude());
            txtPosition.setText(position);
            gpsDialog.hide();
            removePositionListener();
        }
    };

    private void removePositionListener() {
        position.removeListener(postionListener);
    }

    private void showGpsDialog() {
        if (gpsDialog == null) {
            createGpsDialog();
        }
        gpsDialog.showAndWait();
    }

    private void createGpsDialog() {
        Label lblTitle = new Label(localize("view.task.info.tryToGetLocation"));

        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(200);
        HBox boxContent = new HBox(progressBar);
        boxContent.setAlignment(Pos.CENTER);

        gpsDialog = new Dialog<>();
        gpsDialog.setTitle(lblTitle);
        gpsDialog.setContent(boxContent);

        Button btnCancelOk = new Button(localize("button.cancel"));
        btnCancelOk.setOnAction(e ->
        {
            gpsDialog.setResult(ButtonType.CANCEL);
            gpsDialog.hide();
            removePositionListener();
        });

        PauseTransition pauseTransition = new PauseTransition(Duration.seconds(2));
        pauseTransition.setOnFinished(e ->
        {
            lblTitle.setText(localize("view.task.info.couldNotGetGpsLocation"));
            btnCancelOk.setText(localize("button.ok"));
            progressBar.setProgress(0);
            pauseTransition.stop();
            removePositionListener();
        });

        gpsDialog.getButtons().setAll(btnCancelOk);
        gpsDialog.setOnShowing(evt -> pauseTransition.play());
    }

    private void onSave() {
        if (!validateTask()) {
            return;
        }
        // saveResultReceiver().onSuccess(e -> showPreviousView()).start();
    }

    private void onSaveAndContinue() {
        if (!validateTask()) {
            return;
        }
        saveResultReceiver().onSuccess(result ->
        {
            task = new Task();
            setFields(task);
        }).start();
    }

    private AsyncResultReceiver<GluonObservableObject<Task>> saveResultReceiver() {
        return AsyncResultReceiver.create(saveTask())
                                  .defaultProgressLayer()
                                  .exceptionMessage(localize("view.task.error.save"));
    }

    private GluonObservableObject<Task> saveTask() {
        GluonObservableObject<Task> obsTask = new GluonObservableObject<>();

        getExecutor().execute(() ->
        {
            Task newTask = createTask();
            boolean nameChanged = !newTask.getTaskName().equals(task.getTaskName());

            try {
                if (isEditorModus()) {
                    if (nameChanged) {
                        cloudRepo.recreateNameLookup(task.getTaskName(), newTask);
                    }
                    boolean cityChanged = !newTask.getCityId().equals(task.getCityId());
                    if (cityChanged) {
                        cloudRepo.recreateCityLookup(task.getCityId(), newTask);
                    }
                    Image _image = image.get();

                    String previousImageId = imageChanged && _image == null ? task.getImageId() : null;
                    cloudRepo.updateTask(newTask, previousImageId);

                    if (imageChanged) {
                        if (_image == null) {
                            ImageHandler.deleteImageAsync(task.getImageUrl());
                        } else {
                            saveImage(_image, newTask.getImageUrl(), sImage -> ImageHandler.updateImage(sImage, task.getImageUrl()));
                        }
                    }
                } else {
                    cloudRepo.createTask(newTask);
                    if (image.get() != null) {
                        saveImage(image.get(), newTask.getImageUrl(), ImageHandler::storeImage);
                    }
                }
                updateTaskList(newTask, nameChanged);
                GluonObservableHelper.setInitialized(obsTask, true);

            } catch (IOException e) {
                LOGGER.error("Error saving task '{}'", newTask.getTaskName(), e);
                GluonObservableHelper.setException(obsTask, e);
            }
        });
        return obsTask;
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

    private boolean validateTask() {
        Validator<String> nameDoesntExistValidator = new Validator<>(name -> !cloudRepo.checkIfTaskNameExists(name),
                                                                     localize("view.task.info.taskNameAlreadyExists"));

        MultiValidator<String> validator = new MultiValidator<>();
        validator.addCheck(Validators::isNotNullOrEmpty, localize("view.task.info.taskNameCantBeEmpty"));
        validator.addCheck(e -> service.getSelectedCity() != null, localize("view.task.info.noCitySelected"));
        // validator.addCheck(e -> Validators.isNotNullOrEmpty(txtPosition.getText()) &&
        // GPS_PATTERN.matcher(txtPosition.getText()).matches(), localize(
        // "view.task.info.gpsDataInvalid"));
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

        return validator.check(txtName.getText());
    }

    private Task createTask() {
        String cityId = service.getSelectedCity().getId();
        String name = txtName.getText();
        String description = txtDescription.getText();
        String scanCode = txtScanCode.getText();
        int points = Integer.valueOf(txtPoints.getText());
        points = 45; // test:
        AccessType accessType = choiceAccess.getSelectionModel().getSelectedItem();
        boolean createImageId = isEditorModus() ? imageChanged && image.get() != null : image.get() != null;

        Task newTask = new Task(cityId, name, description, getPosition(), points, accessType, service.getUser().getId(), createImageId);
        newTask.setScancode(scanCode);

        if (isEditorModus()) {
            newTask.setId(task.getId());
            newTask.setPreviousTask(task);
            newTask.setAccessTypeChanged(task.getAccessType() != newTask.getAccessType());

            if (!imageChanged) {
                newTask.setImageId(service.getSelectedTask().getImageId());
            }
        }
        return newTask;
    }

    private Position getPosition() {
        Position positionTemp = position.get();
        if (positionTemp != null) {
            return positionTemp;
        }

        String positionText = txtPosition.getText();
        if (isNotNullOrEmpty(positionText)) {
            positionText.replaceAll("\\s", "");

            String[] split = positionText.split(",");
            double latitude = Double.valueOf(split[0]);
            double longitude = Double.valueOf(split[1]);
            return new Position(latitude, longitude);
        }
        return new Position(0, 0);
    }

    private Executor getExecutor() {
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor(runnable ->
            {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setName("SaveTaskThread");
                thread.setDaemon(true);
                return thread;
            });
        }
        return executor;
    }

    private void updateTaskList(Task newTask, boolean taskNameChanged) {
        ListUpdater<Task> tasksUpdater = service.getListUpdater(TASKS_UPDATER);
        boolean accessTypeMatchesList = tasksUpdater.getAccess() == newTask.getAccessType();

        if (isEditorModus()) {
            if (accessTypeMatchesList) {
                if (taskNameChanged) {
                    tasksUpdater.remove(task);
                    tasksUpdater.add(newTask);

                    if (isMissionEditorModus()) {
                        ListUpdater<Object> missionTasksUpdater = service.getListUpdater(MISSION_TASKS_UPDATER);
                        missionTasksUpdater.remove(task);
                        missionTasksUpdater.add(newTask);
                    }

                } else {
                    tasksUpdater.update(newTask);
                    if (isMissionEditorModus()) {
                        service.getListUpdater(MISSION_TASKS_UPDATER).update(newTask);
                    }
                }
            } else {
                tasksUpdater.remove(task);
            }
        } else {
            if (accessTypeMatchesList) {
                tasksUpdater.add(newTask);
            }
        }
    }

    private void onDelete() {
        if (!confirmDeleteAnswer("view.tasks.question.deleteTask").isYesOrOk()) {
            return;
        }

        GluonObservableObject<Task> obsTask = new GluonObservableObject<>();
        AsyncResultReceiver.create(obsTask)
                           .defaultProgressLayer()
                           .onSuccess(e -> showPreviousView())
                           .exceptionMessage(localize("view.task.error.delete"))
                           .start();

        getExecutor().execute(() ->
        {
            try {
                cloudRepo.deleteTask(task);
                service.getListUpdater(TASKS_UPDATER).remove(task);
                if (isMissionEditorModus()) {
                    service.getListUpdater(MISSION_TASKS_UPDATER).remove(task);
                }
                GluonObservableHelper.setInitialized(obsTask, true);

            } catch (IOException ex) {
                LOGGER.error("Error deleting task: {}", task.getTaskName(), ex);
                GluonObservableHelper.setException(obsTask, ex);
            }
        });
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
}