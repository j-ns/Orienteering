/**
 *
 *  Copyright (c) 2016, Jens Stroh
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

package com.jns.orienteering.view;

import static com.jns.orienteering.util.Dialogs.confirmDeleteAnswer;
import static com.jns.orienteering.util.Validators.isNullOrEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.layout.MobileLayoutPane;
import com.gluonhq.charm.glisten.layout.layer.FloatingActionButton;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.maps.MapView;
import com.jns.orienteering.common.BaseService;
import com.jns.orienteering.common.Validator;
import com.jns.orienteering.common.BaseService.CityTempBuffer;
import com.jns.orienteering.control.ChoiceFloatingTextField;
import com.jns.orienteering.control.FloatingTextField;
import com.jns.orienteering.control.ScrollEventFilter;
import com.jns.orienteering.control.cell.TaskCellSmall;
import com.jns.orienteering.model.common.AccessType;
import com.jns.orienteering.model.common.ListUpdater;
import com.jns.orienteering.model.common.ListViewExtended;
import com.jns.orienteering.model.dynamic.CityHolder;
import com.jns.orienteering.model.persisted.City;
import com.jns.orienteering.model.persisted.Mission;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.MissionFBRepo;
import com.jns.orienteering.util.Dialogs;
import com.jns.orienteering.util.Icon;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MissionPresenter extends BasePresenter {

    private static final Logger                 LOGGER                = LoggerFactory.getLogger(MissionPresenter.class);

    private static final String                 MISSION_TASKS_UPDATER = "mission_tasks_updater";
    private static final String                 MISSIONS_UPDATER      = "missions_updater";

    @FXML
    private VBox                                boxMissionDetails;
    @FXML
    private ChoiceFloatingTextField<City>       choiceCity;
    @FXML
    private FloatingTextField                   txtName;
    @FXML
    private FloatingTextField                   txtDistance;
    @FXML
    private ChoiceFloatingTextField<AccessType> choiceAccess;

    @FXML
    private MobileLayoutPane                    paneListView;
    @FXML
    private ListViewExtended<Task>              lviewMissionTasks;
    private ScrollEventFilter                   scrollEventFiler;

    @FXML
    private StackPane                           pneMapLayers;
    @FXML
    private VBox                                boxMapControls;
    @FXML
    private MapView                             map;
    private MapHelper                           mapHelper;

    @Inject
    private BaseService                         service;
    private MissionFBRepo                       cloudRepo;

    private Mission                             mission;
    private GluonObservableList<Task>           tasks;
    private List<Task>                          tasksBuffer;

    private FloatingActionButton                fab;

    @Override
    protected void initialize() {
        super.initialize();

        fab = addFab(paneListView, MaterialDesignIcon.PIN_DROP.text, e -> onSelectTasks());

        choiceCity.setStringConverter(City::getCityName);
        choiceCity.getSelectionModel().selectedItemProperty().addListener((obsValue, c, c1) -> onCityChanged(c1));
        choiceCity.setItems(service.getCities());

        choiceAccess.setStringConverter(accessType -> localize(accessType));
        choiceAccess.setItems(FXCollections.observableArrayList(AccessType.values()));

        lviewMissionTasks.setSelectableCellFactory(listView -> new TaskCellSmall(lviewMissionTasks.selectedItemProperty(), this::onRemoveTask,
                                                                       scrollEventFiler.slidingProperty()));
        lviewMissionTasks.setComparator(Task::compareTo);
        lviewMissionTasks.setOnSelection(this::onSelectTask);
        scrollEventFiler = new ScrollEventFilter(lviewMissionTasks);

        initMap();
        initActionBar();

        service.getActivatorDeactivatorService().add(ViewRegistry.MISSION.getViewName(), lviewMissionTasks);
        cloudRepo = service.getRepoService().getCloudRepo(Mission.class);
    }

    @Override
    protected void initAppBar() {
        setAppBar(createBackButton(), localize("view.mission.title"));
    }

    private void initMap() {
        mapHelper = new MapHelper(map);

        Button btnLocation = Icon.MAP_MARKER.button("18");
        btnLocation.setOnAction(e -> mapHelper.centerFirstMapPoint());
        boxMapControls.getChildren().add(btnLocation);
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
        platformService().getNodePositionAdjuster(boxMissionDetails, view.getScene().focusOwnerProperty());

        mission = service.getSelectedMission();
        boolean isCreatorModus = mission == null;
        if (isCreatorModus) {
            mission = new Mission();
            mission.setCityId(service.getSelectedCity().getId());
            tasks = new GluonObservableList<>();
            tasksBuffer = Collections.emptyList();
            lviewMissionTasks.setSortableItems(tasks);
            mapHelper.setMarkers(null);
            setFields(mission);

            fab.setVisible(true);
            setActionBarVisible(true);

        } else {
            if (ViewRegistry.MISSIONS.equals(service.getPreviousView()) || ViewRegistry.HOME.equals(service.getPreviousView())) {
                tasks = cloudRepo.retrieveTasksAsync(mission.getId());
                AsyncResultReceiver.create(tasks)
                                   .defaultProgressLayer()
                                   .onSuccess(result ->
                                   {
                                       lviewMissionTasks.setSortableItems(result);
                                       mapHelper.setMarkers(result);
                                       tasksBuffer = new ArrayList<>(result);
                                       setFields(mission);
                                   })
                                   .start();
            } else {
                lviewMissionTasks.refresh();
                mapHelper.setMarkers(tasks);
                service.setSelectedMission(null);

            }
            boolean userIsOwnerOfMission = service.getUserId() == null ? false : service.getUserId().equals(mission.getOwnerId());
            fab.setVisible(userIsOwnerOfMission);
            setActionBarVisible(userIsOwnerOfMission);
        }
    }

    private void setFields(Mission mission) {
        choiceCity.getSelectionModel().select(CityHolder.get(mission.getCityId()));
        txtName.setText(mission.getMissionName());
        txtDistance.setDoubleAsText(mission.getDistance());
        choiceAccess.getSelectionModel().select(mission.getAccessType());
    }

    private void onCityChanged(City city) {
        if (city != null) {
            if (service.getTempCity() != null && !city.getId().equals(service.getTempCity().getOriginalCityId())) {
                tasks.clear();
            } else {
                tasks.setAll(tasksBuffer);
            }
            service.setTempCity(new CityTempBuffer(city.getId(), mission.getCityId()));
        }
    }

    private void onSelectTask(Task task) {
        if (task != null) {
            service.setSelectedTask(task);
            service.setSelectedMission(createMission());
            service.setTempCity(new CityTempBuffer(getSelectedCityId(), mission.getCityId()));
            setListUpdater();
            showView(ViewRegistry.TASK);
        }
    }

    private void onSelectTasks() {
        service.setSelectedMission(createMission());
        service.setTempCity(new CityTempBuffer(getSelectedCityId(), mission.getCityId()));
        setListUpdater();
        showView(ViewRegistry.TASKS);
    }

    private void onRemoveTask(Task task) {
        tasks.remove(task);
        mapHelper.removeMarker(task);
    }

    private void onSave() {
        if (saveMission()) {
            showPreviousView();
        }
    }

    private void onSaveAndContinue() {
        if (saveMission()) {
            mission = new Mission();
            tasks = new GluonObservableList<>();
            lviewMissionTasks.setSortableItems(tasks);
            tasksBuffer = new ArrayList<>(tasks);
            setFields(mission);
        }
    }

    private boolean saveMission() {
        if (!validateMissionName(txtName.getText())) {
            return false;
        }

        Mission newMission = createMission();
        try {
            if (isEditorModus()) {
                cloudRepo.updateMission(newMission, mission, tasks, tasksBuffer);

                Mission activeMission = service.getActiveMission();
                if (activeMission != null && activeMission.getId().equals(newMission.getId())) {
                    service.setActiveMission(newMission);
                }
            } else {
                cloudRepo.createMission(newMission);
            }
            // hideProgressLayer
            updateMissionsList(newMission);
            return true;

        } catch (IOException e) {
            LOGGER.error("Error saving mission '{}'", newMission.getMissionName(), e);
            Dialogs.ok(localize("view.mission.error.save")).showAndWait();
            return false;
        }
    }

    private void updateMissionsList(Mission newMission) {
        ListUpdater<Mission> missionsUpdater = service.getListUpdater(MISSIONS_UPDATER);

        if (isEditorModus()) {
            new ListViewUpdater<>(missionsUpdater).update(newMission, mission, service.getSelectedCity());
        } else {
            new ListViewUpdater<>(missionsUpdater).add(newMission);
        }
    }

    private boolean validateMissionName(String name) {
        Validator<String> nameDoesntExistValidator = new Validator<>(cloudRepo::checkIfMissionNameDoesntExist,
                                                                     "view.mission.missionNameAlreadyExists");

        if (isNullOrEmpty(name)) {
            Dialogs.ok(localize("view.mission.warning.nameMustNotBeEmpty")).showAndWait();
            return false;
        }

        if (isEditorModus()) {
            boolean missionNameChanged = !name.equals(mission.getMissionName());
            if (missionNameChanged) {
                return nameDoesntExistValidator.check(name);
            }
            return true;
        } else {
            return nameDoesntExistValidator.check(name);
        }
    }

    private Mission createMission() {
        String missionName = txtName.getText();
        double distance = txtDistance.getTextAsDouble();
        AccessType accessType = choiceAccess.getSelectionModel().getSelectedItem();

        Mission newMission = Mission.create(missionName)
                                    .cityId(getSelectedCityId())
                                    .ownerId(service.getUserId())
                                    .distance(distance)
                                    .accessType(accessType);

        int maxPoints = 0;
        for (Task task : tasks) {
            maxPoints += task.getPoints();
        }
        newMission.setMaxPoints(maxPoints);

        newMission.updateTasksMap(tasks);

        if (isEditorModus()) {
            newMission.setId(mission.getId());
            newMission.setPreviousMission(mission);
        }
        return newMission;
    }

    private boolean isEditorModus() {
        return mission.getId() != null;
    }

    private String getSelectedCityId() {
        return choiceCity.getSelectionModel().getSelectedItem().getId();
    }

    private void setListUpdater() {
        service.setListUpdater(MISSION_TASKS_UPDATER, lviewMissionTasks.getListUpdater(choiceAccess.getSelectionModel().getSelectedItem()));
    }

    private void onDelete() {
        if (!confirmDeleteAnswer(localize("view.mission.question.delete")).isYesOrOk()) {
            return;
        }
        try {
            mission.updateTasksMap(tasks);
            cloudRepo.deleteMission(mission);
            service.getListUpdater(MISSIONS_UPDATER).remove(mission);
            showPreviousView();

        } catch (IOException e) {
            LOGGER.error("Error deleting mission", e);
        }
    }
}