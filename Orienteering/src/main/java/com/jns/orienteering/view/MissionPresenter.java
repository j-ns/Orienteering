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

import static com.jns.orienteering.control.Dialogs.confirmDeleteAnswer;

import javax.inject.Inject;

import com.gluonhq.charm.glisten.layout.MobileLayoutPane;
import com.gluonhq.charm.glisten.layout.layer.FloatingActionButton;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.connect.GluonObservable;
import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.GluonObservableObject;
import com.gluonhq.maps.MapView;
import com.jns.orienteering.common.BaseService;
import com.jns.orienteering.common.BaseService.CityBuffer;
import com.jns.orienteering.common.MultiValidator;
import com.jns.orienteering.common.SingleValidator;
import com.jns.orienteering.control.ChoiceFloatingTextField;
import com.jns.orienteering.control.FloatingTextField;
import com.jns.orienteering.control.Icon;
import com.jns.orienteering.control.ListViewExtended;
import com.jns.orienteering.control.ScrollEventFilter;
import com.jns.orienteering.control.cell.TaskCellSmall;
import com.jns.orienteering.model.common.GluonObservables;
import com.jns.orienteering.model.dynamic.CityCache;
import com.jns.orienteering.model.dynamic.MissionCache;
import com.jns.orienteering.model.persisted.AccessType;
import com.jns.orienteering.model.persisted.ActiveTaskList;
import com.jns.orienteering.model.persisted.City;
import com.jns.orienteering.model.persisted.Mission;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.LocalRepo;
import com.jns.orienteering.model.repo.MissionFBRepo;
import com.jns.orienteering.util.SpecialCharReplacer;
import com.jns.orienteering.util.Validators;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MissionPresenter extends BasePresenter {

    private static final PseudoClass            PSEUDO_CLASS_REORDER = PseudoClass.getPseudoClass("reorder");

    private ToggleButton                        tglSort;
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
    private TabPane                             tabPane;
    @FXML
    private Tab                                 tabTasks;
    @FXML
    private MobileLayoutPane                    paneListView;
    @FXML
    private ListViewExtended<Task>              lviewMissionTasks;
    private ScrollEventFilter                   scrollEventFilter;

    @FXML
    private StackPane                           pneMapLayers;
    @FXML
    private VBox                                boxMapControls;
    @FXML
    private MapView                             map;
    private MapHelper                           mapHelper;

    private FloatingActionButton                fab;

    @Inject
    private BaseService                         service;
    private MissionFBRepo                       cloudRepo;
    private MissionCache                        missionCache;
    private MultiValidator<String>              nameValidator;

    private Mission                             mission;
    private GluonObservableList<Task>           tasks;
    private Task                                taskToReorder;

    @Override
    protected void initialize() {
        super.initialize();

        fab = addFab(paneListView, MaterialDesignIcon.PIN_DROP.text, e -> onSelectTasks());

        choiceCity.setStringConverter(City::getCityName);
        choiceCity.getSelectionModel().selectedItemProperty().addListener((obsValue, c, c1) -> onCityChanged(c1));
        choiceCity.setItems(service.getCitiesSorted());

        choiceAccess.setStringConverter(accessType -> localize(accessType));
        choiceAccess.setItems(AccessType.observableValues());

        lviewMissionTasks.setCellFactory(listView -> new TaskCellSmall(lviewMissionTasks.selectedItemProperty(), this::onRemoveTask,
                                                                       scrollEventFilter.slidingProperty()));

        lviewMissionTasks.setComparator(Task::compareTo);
        lviewMissionTasks.setOnSelection(this::onSelectTask);
        scrollEventFilter = new ScrollEventFilter(lviewMissionTasks);
        scrollEventFilter.slidingProperty().addListener((obsValue, b, b1) ->
        {
            if (!b1) {
                taskToReorder = null;
            }
        });

        initMap();
        initActionBar();

        service.getActivatorDeactivatorService().add(ViewRegistry.MISSION.getViewName(), lviewMissionTasks);
        cloudRepo = service.getRepoService().getCloudRepo(Mission.class);
        missionCache = MissionCache.INSTANCE;
    }

    @Override
    protected void initAppBar() {
        Node icon = Icon.LIST_NUMBERED.icon("22");
        tglSort = new ToggleButton("", icon);
        tglSort.setId("sortIcon");
        tglSort.visibleProperty().bind(tabTasks.selectedProperty());
        tglSort.selectedProperty().addListener((obsValue, b, b1) ->
        {
            if (b1) {
                taskToReorder = null;
            }
            lviewMissionTasks.clearSelection();
            lviewMissionTasks.pseudoClassStateChanged(PSEUDO_CLASS_REORDER, b1);
        });

        setAppBar(createBackButton(), localize("view.mission.title"), tglSort);
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
        lviewMissionTasks.clearSelection();

        mission = service.getSelectedMission();
        boolean isCreatorModus = mission == null;
        if (isCreatorModus) {
            mission = new Mission();
            mission.setCityId(service.getSelectedCity().getId());
            tasks = new GluonObservableList<>();
            lviewMissionTasks.setItems(tasks);
            mapHelper.setMarkers(null);
            setFields(mission);

            fab.setVisible(true);
            setActionBarVisible(true);

        } else {
            if (ViewRegistry.MISSIONS.nameEquals(service.getPreviousViewName())) {
                tabPane.getSelectionModel().select(0);

                GluonObservableList<Task> obsTasks = missionCache.retrieveMissionTasksOrdered(mission.getId());
                AsyncResultReceiver.create(obsTasks)
                                   .defaultProgressLayer()
                                   .onSuccess(result ->
                                   {
                                       tasks = missionCache.getMissionTasksTemp();
                                       lviewMissionTasks.setItems(tasks);
                                       mapHelper.setMarkers(tasks);
                                       setFields(mission);
                                   })
                                   .start();
            } else {
                tasks = missionCache.getMissionTasksTemp();
                lviewMissionTasks.setItems(tasks);
                mapHelper.setMarkers(tasks);
                service.setSelectedMission(null);
                service.setSelectedTask(null);
            }

            boolean userIsOwnerOfMission = service.getUserId() == null ? false : service.getUserId().equals(mission.getOwnerId());
            fab.setVisible(userIsOwnerOfMission);
            setActionBarVisible(userIsOwnerOfMission);
        }
    }

    private void setFields(Mission mission) {
        choiceCity.getSelectionModel().select(CityCache.INSTANCE.get(mission.getCityId()));
        txtName.setText(mission.getMissionName());
        txtDistance.setDoubleAsText(mission.getDistance());
        choiceAccess.getSelectionModel().select(mission.getAccessType());
    }

    private void onCityChanged(City city) {
        if (city != null) {
            if (service.getCityBuffer() != null && !city.getId().equals(service.getCityBuffer().getOriginalCityId())) {
                tasks.clear();
            } else {
                tasks.setAll(missionCache.getMissionTasks());
            }
            service.setCityBuffer(createCityBuffer());
        }
    }

    private void onSelectTask(Task task) {
        if (task != null) {
            if (isReorderModus()) {
                reorderTasks(task);
            } else {
                selectTask(task);
            }
        }
    }

    private void reorderTasks(Task task) {
        if (taskToReorder == null) {
            taskToReorder = task;
        } else {
            lviewMissionTasks.reorder(taskToReorder, task);
            taskToReorder = null;
            lviewMissionTasks.clearSelection();
        }
    }

    private void selectTask(Task task) {
        service.setSelectedTask(task);

        if (isEditorModus() && mission.getOwnerId().equals(service.getUserId())) {
            service.setSelectedMission(createMission());
        }
        service.setCityBuffer(createCityBuffer());
        showView(ViewRegistry.TASK);
    }

    private void onSelectTasks() {
        if (!isEditorModus() || isEditorModus() && mission.getOwnerId().equals(service.getUserId())) {
            service.setSelectedMission(createMission());
        }
        service.setCityBuffer(createCityBuffer());
        showView(ViewRegistry.TASKS);
    }

    private void onRemoveTask(Task task) {
        if (tglSort.isSelected()) {
            return;
        }
        tasks.remove(task);
        mapHelper.removeMarker(task);
        lviewMissionTasks.clearSelection();
    }

    private void onSave() {
        if (!validateMissionName()) {
            return;
        }
        saveReceiver().onSuccess(e -> showPreviousView())
                      .start();
    }

    private void onSaveAndContinue() {
        if (!validateMissionName()) {
            return;
        }
        saveReceiver()
                      .onSuccess(e ->
                      {
                          missionCache.clearTasks();

                          mission = new Mission();
                          tasks = new GluonObservableList<>();
                          lviewMissionTasks.setItems(tasks);
                          mapHelper.setMarkers(null);

                          setFields(mission);
                          choiceCity.getSelectionModel().select(service.getSelectedCity());

                          tabPane.getSelectionModel().select(0);
                      })
                      .start();
    }

    private boolean validateMissionName() {
        return getNameValidator().check(txtName.getText());
    }

    private MultiValidator<String> getNameValidator() {
        if (nameValidator == null) {
            nameValidator = createNameValidator();
        }
        return nameValidator;
    }

    private MultiValidator<String> createNameValidator() {
        SingleValidator<String> nameDoesntExistValidator = new SingleValidator<>(cloudRepo::checkIfMissionNameDoesntExist,
                                                                     localize("view.mission.info.nameExists"));

        MultiValidator<String> validator = new MultiValidator<>();
        validator.addCheck(Validators::isNotNullOrEmpty, localize("view.mission.warning.nameMustNotBeEmpty"));
        validator.addCheck(SpecialCharReplacer::validateInput, localize("view.error.invalidCharEntered"));
        validator.addCheck(name ->
        {
            if (isEditorModus()) {
                boolean missionNameChanged = !name.equals(mission.getMissionName());
                if (missionNameChanged) {
                    return nameDoesntExistValidator.check(name);
                }
                return true;
            } else {
                return nameDoesntExistValidator.check(name);
            }
        });

        return validator;
    }

    private AsyncResultReceiver<GluonObservable> saveReceiver() {
        GluonObservable obsResult = saveMission(createMission());
        return AsyncResultReceiver.create(obsResult)
                                  .defaultProgressLayer();
    }

    private Mission createMission() {
        String missionName = txtName.getText();
        String ownerId = service.getUserId();
        double distance = txtDistance.getTextAsDouble();
        AccessType accessType = choiceAccess.getSelectedItem();
    
        Mission newMission = new Mission(missionName, getSelectedCityId(), ownerId, distance, accessType);
    
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

    private GluonObservable saveMission(Mission newMission) {
        GluonObservable obsSuccessful = new GluonObservableObject<>();

        if (isEditorModus()) {
            GluonObservableObject<Mission> obsMission = cloudRepo.updateMission(newMission, mission, tasks, missionCache.getMissionTasks());
            saveReceiver(obsMission, obsSuccessful)
                                                   .onSuccess(result ->
                                                   {
                                                       Mission activeMission = service.getActiveMission();

                                                       if (activeMission != null && activeMission.getId().equals(newMission.getId())) {
                                                           missionCache.updateTasksWithBuffer();
                                                           service.setActiveMission(newMission);

                                                           LocalRepo<Task, ActiveTaskList> localTasksRepo = service.getRepoService().getLocalRepo(
                                                                                                                                                  Task.class);
                                                           localTasksRepo.createOrUpdateListAsync(new ActiveTaskList(tasks));
                                                       }
                                                   })
                                                   .start();

        } else {
            GluonObservableObject<Mission> obsMission = cloudRepo.createMission(newMission);
            saveReceiver(obsMission, obsSuccessful).start();
        }
        return obsSuccessful;
    }

    private AsyncResultReceiver<GluonObservableObject<Mission>> saveReceiver(GluonObservableObject<Mission> obsMission,
                                                                             GluonObservable obsSuccessful) {
        return AsyncResultReceiver.create(obsMission)
                                  .defaultProgressLayer()
                                  .propagateException(obsSuccessful)
                                  .exceptionMessage(localize("view.mission.error.save"))
                                  .finalize(result ->
                                  {
                                      if (result.isInitialized()) {
                                          updateMissionsList(result.get(), mission);
                                          GluonObservables.setInitialized(obsSuccessful);
                                      }
                                  });
    }

    private void updateMissionsList(Mission newMission, Mission previousMission) {
        if (isEditorModus()) {
            missionCache.updateItem(newMission, previousMission);
        } else {
            missionCache.addItem(newMission);
        }
    }

    private CityBuffer createCityBuffer() {
        return new CityBuffer(getSelectedCityId(), mission.getCityId());
    }

    private String getSelectedCityId() {
        return choiceCity.getSelectedItem() != null ? choiceCity.getSelectedItem().getId() : null;
    }

    private boolean isEditorModus() {
        return mission.getId() != null;
    }

    private boolean isReorderModus() {
        return tglSort.isSelected();
    }

    private void onDelete() {
        if (!confirmDeleteAnswer(localize("view.mission.question.delete")).isYesOrOk()) {
            return;
        }
        if (mission.getId() == null) {
            showPreviousView();
            return;
        }

        mission.updateTasksMap(missionCache.getMissionTasks());

        GluonObservableObject<Mission> obsMission = cloudRepo.deleteMissionAsync(mission);
        AsyncResultReceiver.create(obsMission)
                           .defaultProgressLayer()
                           .onSuccess(e ->
                           {
                               missionCache.removeMissionAndTasks(mission);

                               Mission activeMission = service.getActiveMission();
                               if (activeMission != null && activeMission.getId().equals(mission.getId())) {
                                   service.setActiveMission(null);
                               }

                               showView(ViewRegistry.MISSIONS);
                           })
                           .exceptionMessage(localize("view.mission.error.delete"))
                           .start();
    }

}
