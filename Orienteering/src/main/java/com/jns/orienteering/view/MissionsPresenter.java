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
import static com.jns.orienteering.control.Dialogs.showInfo;

import com.gluonhq.charm.glisten.layout.layer.FloatingActionButton;
import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.GluonObservableObject;
import com.jns.orienteering.control.cell.MissionCell;
import com.jns.orienteering.model.dynamic.MissionCache;
import com.jns.orienteering.model.dynamic.ModelCache;
import com.jns.orienteering.model.persisted.Mission;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.MissionFBRepo;

import javafx.application.Platform;
import javafx.collections.FXCollections;

public class MissionsPresenter extends ListViewPresenter<Mission> {

    private MissionFBRepo cloudRepo;
    private MissionCache  missionCache;

    @Override
    protected void initialize() {
        super.initialize();

        FloatingActionButton fab = addFab(view, e -> onCreateMission());
        fab.visibleProperty().bind(service.userProperty().isNotNull());

        lview.setCellFactory(listView -> new MissionCell(lview.selectedItemProperty(), this::onDeleteMission, this::onSetActiveMission,
                                                         scrollEventFilter.slidingProperty()));
        lview.setComparator(Mission::compareTo);
        lview.setOnSelection(this::onSelectMission);

        cloudRepo = service.getRepoService().getCloudRepo(Mission.class);
        missionCache = MissionCache.INSTANCE;
    }

    @Override
    protected String getViewName() {
        return ViewRegistry.MISSIONS.getViewName();
    }

    @Override
    protected String getTitle() {
        return localize("view.missions.title");
    }

    @Override
    protected String getNoDataExistingMessage() {
        return localize("view.missions.info.noMissionExisting");
    }

    @Override
    protected ModelCache<?> getCache() {
        return missionCache;
    }

    @Override
    protected void onShown() {
        super.onShown();

        service.setSelectedMission(null);
        service.setCityBuffer(null);
        missionCache.clearMissionTasksTemp();

        if (ViewRegistry.MISSION.nameEquals(service.getPreviousViewName())) {
            lview.refresh();
        } else {
            populateListView();
        }
    }

    @Override
    protected void populateListView() {
        String cityId = service.getSelectedCityId();
        String userId = service.getUserId();

        if (cityId == null || userId == null) {
            lview.setItems(FXCollections.observableArrayList());
            return;
        }

        GluonObservableList<Mission> obsMissions = isPrivateAccess() ? missionCache.getPrivateItems(cityId, userId) : missionCache
                                                                                                                                  .getPublicItems(cityId);
        AsyncResultReceiver.create(obsMissions)
                           .defaultProgressLayer()
                           .onSuccess(lview::setSortableItems)
                           .start();
    }

    private void onCreateMission() {
        showView(ViewRegistry.MISSION);
    }

    private void onSelectMission(Mission mission) {
        if (mission != null) {
            service.setSelectedMission(mission);
            showView(ViewRegistry.MISSION);
        }
    }

    private void onSetActiveMission(Mission mission) {
        if (!cloudRepo.missionContainsTasks(mission.getId())) {
            Platform.runLater(() -> showInfo(localize("view.missions.info.missionDoesntContainTask")));
            return;
        }

        service.setActiveMission(mission);
        showView(ViewRegistry.HOME);
    }

    private void onDeleteMission(Mission mission) {
        Platform.runLater(() ->
        {
            if (!mission.getOwnerId().equals(service.getUserId())) {
                showInfo(localize("missions.info.missionCanOnlyBeDeletedByOwner"));
                return;
            }

            if (confirmDeleteAnswer(localize("view.mission.question.delete")).isYesOrOk()) {
                GluonObservableObject<Mission> obsMission = cloudRepo.deleteMissionAsync(mission);
                AsyncResultReceiver.create(obsMission)
                                   .defaultProgressLayer()
                                   .onSuccess(e ->
                                   {
                                       missionCache.removeMissionAndTasks(mission);
                                       if (mission.equals(service.getActiveMission())) {
                                           service.setActiveMission(null);
                                       }
                                   })
                                   .exceptionMessage(localize("view.mission.error.delete"))
                                   .start();
            }
        });
    }

}