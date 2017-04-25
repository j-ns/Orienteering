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
import static com.jns.orienteering.control.Dialogs.showInfo;

import java.util.ArrayList;
import java.util.List;

import com.gluonhq.charm.glisten.layout.MobileLayoutPane;
import com.gluonhq.charm.glisten.layout.layer.FloatingActionButton;
import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.GluonObservableObject;
import com.jns.orienteering.control.Icon;
import com.jns.orienteering.control.cell.TaskCellSmall;
import com.jns.orienteering.model.dynamic.MissionCache;
import com.jns.orienteering.model.dynamic.ModelCache;
import com.jns.orienteering.model.dynamic.TaskCache;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.TaskFBRepo;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class TasksPresenter extends ListViewPresenter<Task> {

    @FXML
    private MobileLayoutPane innerView;

    private TaskFBRepo       cloudRepo;
    private TaskCache        taskCache;

    private List<Task>       missionTasks;

    @Override
    protected void initialize() {
        super.initialize();

        FloatingActionButton fab = addFab(innerView, e -> onCreateTask());
        fab.visibleProperty().bind(service.userProperty().isNotNull());

        lview.setComparator(Task::compareTo);
        lview.setOnSelection(this::onSelectTask);

        initActionBar();

        cloudRepo = service.getRepoService().getCloudRepo(Task.class);
        taskCache = TaskCache.INSTANCE;
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
    protected ModelCache<?> getCache() {
        return taskCache;
    }

    @Override
    protected void initAppBar() {
        if (isMissionEditorModus()) {
            setAppBar(createBackButton(), getTitle(), btnRefresh, tglAccessType);
        } else {
            setAppBar(createGoHomeButton(), getTitle(), btnRefresh, tglAccessType, choiceCity);
        }
    }

    private void initActionBar() {
        Button btnSave = Icon.Buttons.actionBarButton(Icon.DONE, localize("label.save"), e -> onUpdateMissionTasks());
        Button btnRemoveAllTask = Icon.Buttons.actionBarButton(Icon.MAP_MARKER_OFF, localize("label.clear"), e -> onRemoveMissionTasks());
        setActionBar(btnSave, btnRemoveAllTask);

        setActionBarVisible(false);
    }

    @Override
    protected void onShown() {
        super.onShown();

        if (ViewRegistry.TASK.nameEquals(service.getPreviousViewName())) {
            lview.refresh();
            service.setSelectedTask(null);
        } else {
            if (isMissionEditorModus()) {
                missionTasks = new ArrayList<>(MissionCache.INSTANCE.getMissionTasksTemp());
            }
            populateListView();
        }
        setActionBarVisible(isMissionEditorModus());
    }

    @Override
    protected void populateListView() {
        updateCellFactory();

        // todo:
        String cityId = isMissionEditorModus() ? service.getCityBuffer() == null ? null : service.getCityBuffer().getCurrentCityId()
                : service.getSelectedCityId();

        if (cityId == null || service.getUserId() == null) {
            lview.setItems(FXCollections.observableArrayList());
            return;
        }

        GluonObservableList<Task> obsTasks =
                isPrivateAccess() ? taskCache.getPrivateItems(cityId, service.getUserId()) : taskCache.getPublicItems(cityId);

        AsyncResultReceiver.create(obsTasks)
                           .defaultProgressLayer()
                           .onSuccess(lview::setSortableItems)
                           .start();
    }

    private void updateCellFactory() {
        if (isMissionEditorModus()) {
            lview.setCellFactory(
                                 listView -> new TaskCellSmall(lview.selectedItemProperty(), this::isPartOfMission, null,
                                                               this::onChangePartOfMission, scrollEventFilter.slidingProperty()));
        } else {
            lview.setCellFactory(listView -> new TaskCellSmall(lview.selectedItemProperty(), this::onDeleteTask, scrollEventFilter
                                                                                                                                  .slidingProperty()));
        }
    }

    private boolean isMissionEditorModus() {
        return service.getSelectedMission() != null;
    }

    private void onCreateTask() {
        showView(ViewRegistry.TASK);
    }

    private void onSelectTask(Task task) {
        if (task != null) {
            service.setSelectedTask(task);
            showView(ViewRegistry.TASK);
        }
    }

    private void onDeleteTask(Task task) {
        Platform.runLater(() ->
        {
            if (!task.getOwnerId().equals(service.getUserId())) {
                showInfo(localize("view.tasks.info.taskCanOnlyBeDeletedByOwner"));
                return;
            }
            if (!confirmDeleteAnswer(localize("view.tasks.question.deleteTask")).isYesOrOk()) {
                return;
            }

            GluonObservableObject<Task> obsTask = cloudRepo.deleteTaskAsync(task);
            AsyncResultReceiver.create(obsTask)
                               .defaultProgressLayer()
                               .onSuccess(e ->
                               {
                                   taskCache.removeItem(task);

                                   if (service.activeMissionContainsTask(task)) {
                                       service.setActiveMission(null);
                                   }
                                   if (isMissionEditorModus()) {
                                       MissionCache.INSTANCE.removeTask(task);
                                   }
                               })
                               .exceptionMessage(localize("view.tasks.error.deleteTask"))
                               .start();
        });
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

    private void onRemoveMissionTasks() {
        missionTasks.clear();
        lview.refresh();
    }

    private void onUpdateMissionTasks() {
        MissionCache.INSTANCE.getMissionTasksTemp().setAll(missionTasks);
        showPreviousView();
    }

    @Override
    protected void onHidden() {
        super.onHidden();
        setActionBarVisible(false);
    }

}