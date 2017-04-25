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

import static com.jns.orienteering.util.Validations.isNullOrEmpty;

import java.time.LocalTime;
import java.util.Iterator;
import java.util.Optional;

import javax.inject.Inject;

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.BarcodeScanService;
import com.gluonhq.charm.down.plugins.Position;
import com.gluonhq.maps.MapView;
import com.jns.orienteering.common.BaseService;
import com.jns.orienteering.control.Dialogs;
import com.jns.orienteering.control.Dialogs.DialogAnswer;
import com.jns.orienteering.control.Icon;
import com.jns.orienteering.control.ListViewExtended;
import com.jns.orienteering.control.cell.TaskCellLarge;
import com.jns.orienteering.control.cell.TaskCellSmall;
import com.jns.orienteering.control.cell.TimeLineCell;
import com.jns.orienteering.model.persisted.Mission;
import com.jns.orienteering.model.persisted.MissionStat;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.persisted.TaskStat;
import com.jns.orienteering.model.persisted.TrackData;
import com.jns.orienteering.model.repo.MissionStatFBRepo;
import com.jns.orienteering.platform.PositionServiceExtended;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ActiveMissionPresenter extends BasePresenter {

    private static final int                 TAB_COMPACT   = 0;
    private static final int                 TAB_MAP       = 1;
    private static final int                 TAB_TASKS     = 3;

    private static final int                 SIDE_MARGIN   = 50;
    private static final double              TARGET_RADIUS = 7;

    private static final boolean             COMPLETED     = true;
    private static final boolean             UNCOMPLETED   = false;

    @FXML
    private TabPane                          tabPane;
    @FXML
    private StackPane                        paneCompact;
    @FXML
    private VBox                             boxCompact;
    @FXML
    private MapView                          mapCompact;
    @FXML
    private VBox                             boxNavigationCompact;

    private MapHelper                        mapCompactHelper;
    @FXML
    private ListViewExtended<Task>           lviewTask;

    @FXML
    private StackPane                        paneLarge;
    @FXML
    private MapView                          mapLarge;
    private MapHelper                        mapLargeHelper;
    @FXML
    private VBox                             boxNavigationLarge;

    @FXML
    private StackPane                        paneTasks;
    @FXML
    private ListViewExtended<Task>           lviewTasks;

    @FXML
    private StackPane                        paneStats;
    @FXML
    private ListViewExtended<TaskStat>       lviewStats;
    private Button                           btnScan;

    private Button                           btnSkipTask;
    @Inject
    private BaseService                      service;

    private Mission                          mission;
    private TaskIterator                     taskIterator;
    private Task                             activeTask;

    private StatsCollector                   statsCollector;

    private ReadOnlyObjectProperty<Position> position;
    private PositionServiceExtended          positionService;

    private MissionStatFBRepo                missionStatCloudRepo;

    @Override
    protected void initialize() {
        super.initialize();
        missionStatCloudRepo = service.getRepoService().getCloudRepo(MissionStat.class);

        initMaps();
        initListViews();

        addBlindArea(paneCompact, boxNavigationCompact);
        addBlindArea(paneLarge, boxNavigationLarge);
        addBlindArea(paneStats, null);
        addBlindArea(paneTasks, null);

        service.stopMissionProperty().addListener((ov, b, b1) ->
        {
            if (b1) {
                onStopMission();
            }
        });

        btnScan = Icon.BARCODE.button(e -> scanBarcode());
        btnSkipTask = Icon.SKIP_FORWARD.button(e -> advanceTask(UNCOMPLETED));
    }

    @Override
    protected void initAppBar() {
        setAppBar(createGoHomeButton(), localize("view.activeMission.title"), btnScan, btnSkipTask);
    }

    private void initMaps() {
        positionService = platformService().getPositionService();
        position = positionService.positionProperty();

        mapCompactHelper = new MapHelper(mapCompact, position.get());
        mapLargeHelper = new MapHelper(mapLarge, position.get());

        boxNavigationCompact.getChildren().addAll(mapCompactHelper.createCenterLocationButton(position::get),
                                                  mapCompactHelper.createCenterMarkerButton(this::getActiveOrLastTaskPosition));

        boxNavigationLarge.getChildren().addAll(mapLargeHelper.createCenterLocationButton(position::get),
                                                mapLargeHelper.createCenterMarkerButton(this::getActiveOrLastTaskPosition));

        mapCompact.setOnMouseClicked(e -> selectTab(TAB_MAP));
        mapLarge.setOnMouseClicked(e -> selectTab(TAB_COMPACT));

    }

    private Position getActiveOrLastTaskPosition() {
        if (!taskIterator.hasNext()) {
            return taskIterator.getLast().getPosition();
        }
        if (activeTask.getLatitude() == 0 && activeTask.getLongitude() == 0) {
            return null;
        }
        return activeTask.getPosition();
    }

    private void initListViews() {
        lviewTask.setSelectableCellFactory(TaskCellSmall::new);
        lviewTasks.setCellFactory(listView -> new TaskCellLarge());
        lviewStats.setCellFactory(listView -> new TimeLineCell());

        lviewTask.setOnMouseClicked(e -> onSelectTask(activeTask));
        lviewTasks.setOnMouseClicked(e -> selectTab(TAB_COMPACT));
        lviewStats.setOnMouseClicked(e -> selectTab(TAB_COMPACT));

        lviewTasks.addListeners();
        lviewStats.addListeners();
    }

    /**
     * Adds an border to <code>root</code>, which doesn't react to touch gestures.
     * This is neccessary to prevent the current showing view from being accidentially switched to another view,
     * when you hold the device in your hand during an active mission.
     *
     * @param root
     *            node to add the border area to
     * @param nodeOnTop
     *            node which is added on top of the blind area, to make it responsive to touch gestures, e.g. a map navigation
     *            button
     */
    private void addBlindArea(StackPane root, Node nodeOnTop) {
        HBox boxLeft = new HBox();
        boxLeft.setMinWidth(SIDE_MARGIN);

        HBox boxRight = new HBox();
        boxRight.setMinWidth(SIDE_MARGIN);

        HBox boxCenter = new HBox();
        boxCenter.setMouseTransparent(true);
        HBox.setHgrow(boxCenter, Priority.ALWAYS);

        HBox boxBlindArea = new HBox(boxLeft, boxCenter, boxRight);
        boxBlindArea.setPickOnBounds(false);

        root.getChildren().add(boxBlindArea);
        if (nodeOnTop != null) {
            root.getChildren().add(nodeOnTop);
        }
    }

    private void scanBarcode() {
        Optional<BarcodeScanService> scanService = Services.get(BarcodeScanService.class);
        scanService.ifPresent(s ->
        {
            s.scan().ifPresent(barcode -> onBarcodeScaned(barcode));
        });
    }

    private void onBarcodeScaned(String barcode) {
        if (barcode != null && barcode.equals(activeTask.getScanCode())) {
            advanceTask(COMPLETED);
            notifyTaskCompleted();
        }
    };

    @Override
    protected void onShown() {
        super.onShown();

        if (mission == null) {
            mission = service.getActiveMission();

            taskIterator = new TaskIterator(service.getActiveTasks());
            activeTask = taskIterator.next();

            statsCollector = new StatsCollector();
            statsCollector.start();
            service.setActiveMissionStat(statsCollector.missionStat);

            mapCompactHelper.setFirstMarker(activeTask);
            mapLargeHelper.setFirstMarker(activeTask);

            lviewTask.setItems(FXCollections.singletonObservableList(activeTask));
            lviewTasks.setItems(taskIterator.tasks);
            lviewStats.setItems(statsCollector.missionStat.getTaskStats());

            position.addListener(positionListener);
            positionService.activate();

            btnScan.setVisible(!isNullOrEmpty(activeTask.getScanCode()));
            btnSkipTask.setVisible(true);
        }
    }

    private ChangeListener<? super Position> positionListener = (ov, p, p1) ->
    {
        if (p != null && p1 != null) {
            updateCurrentPosition(p1);

            if (!statsCollector.isMissionFinished()) {
                float distance = positionService.getDistance(p, p1);
                statsCollector.addDistance(distance);

                checkInTargetRadius(p1);
            }
        }
    };

    private void updateCurrentPosition(Position position) {
        int selectedTabIdx = getSelectedTabIdx();

        if (selectedTabIdx == TAB_COMPACT) { //
            mapCompactHelper.updateCurrentLocation(position);
        } else if (selectedTabIdx == TAB_MAP) {
            mapLargeHelper.updateCurrentLocation(position);
        }
    }

    private void checkInTargetRadius(Position position) {
        boolean inTargetRadius = positionService.isInRadius(position, activeTask.getPosition(), TARGET_RADIUS);
        if (inTargetRadius) {
            advanceTask(COMPLETED);
            notifyTaskCompleted();
        }
    }

    private void advanceTask(boolean completed) {
        activeTask.setCompleted(completed);
        statsCollector.updateTaskStat();

        if (taskIterator.hasNext()) {
            setActiveTaskAndUpdateListTask(taskIterator.next());

            mapCompactHelper.addMarker(activeTask, completed);
            mapLargeHelper.addMarker(activeTask, completed);

            lviewTasks.scrollTo(taskIterator.idx - 1);
            btnScan.setVisible(!isNullOrEmpty(activeTask.getScanCode()));

        } else {
            statsCollector.setMissionFinished();
            setActiveTaskAndUpdateListTask(Task.finishedInstance(completed));

            mapCompactHelper.addMarker(null, completed);
            mapLargeHelper.addMarker(null, completed);

            lviewStats.setItems(statsCollector.missionStat.getTaskStatsWithSummary());

            btnScan.setVisible(false);
            btnSkipTask.setVisible(false);
        }
    }

    private void setActiveTaskAndUpdateListTask(Task task) {
        activeTask = task;
        lviewTask.setItems(FXCollections.singletonObservableList(task));
    }

    private void onSelectTask(Task task) {
        if (task != null) {
            selectTab(TAB_TASKS);
            lviewTasks.scrollTo(taskIterator.idx - 1);
        }
    }

    private void selectTab(int tabIdx) {
        tabPane.getSelectionModel().select(tabIdx);
    }

    private int getSelectedTabIdx() {
        return tabPane.getSelectionModel().getSelectedIndex();
    }

    private void notifyTaskCompleted() {
        platformService().vibrate();
        platformService().playRingtone();
    }

    private void onStopMission() {
        if (!statsCollector.missionStat.isCompleted()) {
            clearView();
            return;
        }

        DialogAnswer cancelOkAnswer = Dialogs.cancelOkAnswer(localize("view.activeMission.question.saveStat"), localize("button.cancel"), localize(
                                                                                                                                                   "button.save"));
        if (cancelOkAnswer.isYesOrOk()) {
            missionStatCloudRepo.createOrUpdate(statsCollector.missionStat);
        }
        clearView();
    }

    private void clearView() {
        mission = null;
        taskIterator = null;
        activeTask = null;

        mapCompactHelper.clearMarkers();
        mapLargeHelper.clearMarkers();

        lviewTask.setItems(null);
        lviewTasks.setItems(null);
        lviewStats.setItems(null);

        position.removeListener(positionListener);
        positionService.deactivate();
    }

    private class StatsCollector {

        private MissionStat missionStat;
        private TrackData   trackData;
        private int         start;

        private void start() {
            start = LocalTime.now().toSecondOfDay();
            missionStat = new MissionStat(mission, service.getUserId(), start);
            trackData = new TrackData();
        }

        private void addDistance(float distance) {
            trackData.addDistance(distance);
        }

        private void updateTaskStat() {
            int end = LocalTime.now().toSecondOfDay();

            TaskStat taskStat = new TaskStat(activeTask, trackData);
            taskStat.setStart(start);
            taskStat.setEnd(end);

            missionStat.addTaskStat(taskStat);
            trackData = new TrackData();
            start = end;
        }

        private boolean isMissionFinished() {
            return missionStat.isFinished();
        }

        private void setMissionFinished() {
            missionStat.setFinished(true);
        }
    }

    private class TaskIterator implements Iterator<Task> {

        private ObservableList<Task> tasks;
        private int                  idx;

        private TaskIterator(ObservableList<Task> tasks) {
            this.tasks = tasks;
        }

        @Override
        public boolean hasNext() {
            return !isNullOrEmpty(tasks) && idx < tasks.size();
        }

        @Override
        public Task next() {
            return tasks.get(idx++);
        }

        public Task getLast() {
            int idx = tasks.size() - 1;
            return tasks.get(idx);
        }
    }

}