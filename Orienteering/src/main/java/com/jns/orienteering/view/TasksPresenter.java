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

import static com.jns.orienteering.util.Dialogs.confirmDeleteAnswer;

import java.io.IOException;

import com.gluonhq.charm.glisten.layout.MobileLayoutPane;
import com.gluonhq.charm.glisten.layout.layer.FloatingActionButton;
import com.gluonhq.connect.GluonObservableList;
import com.jns.orienteering.control.cell.TaskCellSmall;
import com.jns.orienteering.model.common.ListUpdater;
import com.jns.orienteering.model.persisted.Mission;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.TaskFBRepo;
import com.jns.orienteering.util.Dialogs;
import com.jns.orienteering.util.Icon;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class TasksPresenter extends ListViewPresenter<Task> {

    private static final String  TASKS_UPDATER         = "tasks_updater";
    private static final String  MISSION_TASKS_UPDATER = "mission_tasks_updater";

    @FXML
    private MobileLayoutPane     innerView;

    private TaskFBRepo           taskRepo;

    private Mission              selectedMission;
    private ObservableList<Task> missionTasks;

    @Override
    protected void initialize() {
        super.initialize();

        FloatingActionButton fab = addFab(innerView, e -> onCreateTask());
        fab.visibleProperty().bind(service.userProperty().isNotNull());

        lview.setOnSelection(this::onTaskSelected);
        lview.setComparator((t, t1) -> t.getTaskName().compareTo(t1.getTaskName()));

        initActionBar();

        taskRepo = service.getRepoService().getCloudRepo(Task.class);
    }

    @Override
    protected String getViewName() {
        return ViewRegistry.TASKS.getViewName();
    }

    @Override
    protected String getTitle() {
        return localize("view.tasks.title");
    }

    @Override
    protected String getNoDataExistingMessage() {
        return localize("view.tasks.info.noTasksExisting");
    }

    @Override
    protected void initAppBar() {
        Button btnBack = isMissionEditorModus() ? createBackButton() : createGoHomeButton();
        if (ViewRegistry.MISSION.equals(service.getPreviousView())) {
            setAppBar(btnBack, getTitle(), tglAccessType);
        } else {
            setAppBar(btnBack, getTitle(), tglAccessType, choiceCityFilter);
        }
    }

    private void initActionBar() {
        Button btnSave = Icon.Buttons.save(e -> onUpdateMissionTasks());
        Button btnRemoveAllTasks = Icon.MAP_MARKER_OFF.button(e -> onRemoveMissionTasks());
        setActionBar(btnSave, btnRemoveAllTasks);
        setActionBarVisible(false);
    }

    @Override
    protected void onShown() {
        super.onShown();

        if (ViewRegistry.TASK.equals(service.getPreviousView())) {
            lview.refresh();
            service.setSelectedTask(null);
        } else {
            if (isMissionEditorModus()) {
                setActionBarVisible(true);
                selectedMission = service.getSelectedMission();
                missionTasks = service.<Task> getListUpdater(MISSION_TASKS_UPDATER).createItemsCopy();
            }
            populateListView();
        }
    }

    @Override
    protected void populateListView() {
        updateCellFactory();

        String cityId = isMissionEditorModus() ? service.getTempCity() == null ? null : service.getTempCity().getTempCityId()
                : service.getSelectedCityId();
        if (cityId == null) {
            lview.setItems(FXCollections.emptyObservableList());
            return;
        }

        GluonObservableList<Task> obsTasks =
                isPrivateAccess() ? taskRepo.getPrivateTasksAsync(cityId, service.getUserId()) : taskRepo.getPublicTasksAsync(cityId);

        AsyncResultReceiver.create(obsTasks)
                           .defaultProgressLayer()
                           .onSuccess(lview::setSortableItems)
                           .start();
    }

    private void updateCellFactory() {
        if (isMissionEditorModus()) {
            lview.setCellFactory(
                                 listView -> new TaskCellSmall(lview.selectedItemProperty(), this::isPartOfMission, null,
                                                               this::onChangePartOfMission, scrollEventFiler.slidingProperty()));
        } else {
            lview.setCellFactory(listView -> new TaskCellSmall(lview.selectedItemProperty(), this::onDeleteTask, scrollEventFiler.slidingProperty()));
        }
    }

    private boolean isMissionEditorModus() {
        return service.getSelectedMission() != null;
    }

    private boolean isPartOfMission(Task task) {
        return missionTasks.contains(task);
    }

    private void onChangePartOfMission(Task task, boolean addToMission) {
        if (addToMission) {
            missionTasks.add(task);
        } else {
            missionTasks.remove(task);
        }
    }

    private void onUpdateMissionTasks() {
        service.<Task> getListUpdater(MISSION_TASKS_UPDATER).setAll(missionTasks);
        showPreviousView();
    }

    private void onRemoveMissionTasks() {
        missionTasks.clear();
        lview.refresh();
    }

    private void onTaskSelected(Task task) {
        if (task != null) {
            service.setSelectedTask(task);
            setListUpdater();
            onCreateTask();
        }
    }

    private void onCreateTask() {
        setListUpdater();
        showView(ViewRegistry.TASK);
    }

    private void setListUpdater() {
        ListUpdater<Task> listUpdater = lview.getListUpdater(getAccessType());
        service.setListUpdater(TASKS_UPDATER, listUpdater);
    }

    private void onDeleteTask(Task task) {
        Platform.runLater(() ->
        {
            if (!task.getOwnerId().equals(service.getUserId())) {
                Dialogs.ok(localize("view.tasks.info.taskCanOnlyBeDeletedByOwner")).showAndWait();
                return;
            }

            if (confirmDeleteAnswer(localize("view.tasks.question.deleteTask")).isYesOrOk()) {
                try {
                    taskRepo.deleteTask(task);
                    lview.getListUpdater().remove(task);

                    if (isMissionEditorModus()) {
                        service.getListUpdater(MISSION_TASKS_UPDATER).remove(task);

                        if (selectedMission.equals(service.getActiveMission())) {
                            service.setActiveMission(null);
                        }
                    }

                } catch (IOException e) {
                    Dialogs.ok(localize("view.tasks.error.deleteTask")).showAndWait();
                }
            }
        });
    }

    @Override
    protected void onHidden() {
        setActionBarVisible(false);
    }

}