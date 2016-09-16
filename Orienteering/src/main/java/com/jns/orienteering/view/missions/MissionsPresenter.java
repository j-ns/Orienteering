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
package com.jns.orienteering.view.missions;

import java.io.IOException;

import com.gluonhq.charm.glisten.layout.layer.FloatingActionButton;
import com.gluonhq.connect.GluonObservableList;
import com.jns.orienteering.OrienteeringApp;
import com.jns.orienteering.control.cell.MissionCell;
import com.jns.orienteering.model.persisted.Mission;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.MissionFBRepo;
import com.jns.orienteering.util.Dialogs;
import com.jns.orienteering.view.common.ListViewPresenter;

import javafx.application.Platform;

public class MissionsPresenter extends ListViewPresenter<Mission> {

    private static final String MISSIONS_UPDATER = "missions_updater";

    private MissionFBRepo       cloudRepo;

    @Override
    protected void initialize() {
        super.initialize();
        cloudRepo = service.getRepoService().getCloudRepo(Mission.class);

        FloatingActionButton fab = addFab(view, e -> onCreateMission());
        fab.visibleProperty().bind(service.userProperty().isNotNull());

        lview.setCellFactory(listView -> new MissionCell(lview.selectedItemProperty(), this::onDeleteMission, this::onSetActiveMission,
                                                         scrollEventFiler.slidingProperty()));
        lview.setComparator(Mission::compareTo);
        lview.setOnSelection(this::onSelectMission);
    }

    @Override
    protected String getViewName() {
        return OrienteeringApp.MISSIONS_VIEW;
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
    protected void onShown() {
        super.onShown();
        service.setTempCity(null);

        if (OrienteeringApp.MISSION_VIEW.equals(service.getPreviousView())) {
            lview.refresh();
            service.setSelectedMission(null);
        } else {
            populateListView();
        }
    }

    @Override
    protected void populateListView() {
        String cityId = getCityIdFilter();

        GluonObservableList<Mission> missions =
                isPrivateAccess() ? cloudRepo.getPrivateMissions(cityId, service.getUserId()) : cloudRepo.getPublicMissions(cityId);

        AsyncResultReceiver.create(missions)
                           .defaultProgressLayer()
                           .onSuccess(lview::setSortableItems)
                           .start();
    }

    private void onCreateMission() {
        setListUpdater();
        showView(OrienteeringApp.MISSION_VIEW);
    }

    private void onSelectMission(Mission mission) {
        if (mission != null) {
            service.setSelectedMission(mission);
            setListUpdater();
            showView(OrienteeringApp.MISSION_VIEW);
        }
    }

    private void onSetActiveMission(Mission mission) {
        boolean missionContainsTasks = cloudRepo.missionContainsTasks(mission.getId());
        if (!missionContainsTasks) {
            Platform.runLater(() -> Dialogs.ok("view.activeMission.info.missionDoesntContainTask").showAndWait());
            return;
        }

        service.setActiveMission(mission);
        showView(OrienteeringApp.HOME_VIEW);
    }

    private void setListUpdater() {
        service.setListUpdater(MISSIONS_UPDATER, lview.getListUpdater(accessType));
    }

    private void onDeleteMission(Mission mission) {
        Platform.runLater(() ->
        {
            if (confirmDeleteMission()) {
                try {
                    cloudRepo.deleteMission(mission);
                    lview.getListUpdater().remove(mission);

                    if (mission.equals(service.getActiveMission())) {
                        service.setActiveMission(null);
                    }
                } catch (IOException e) {
                    Dialogs.ok(localize("view.mission.error.delete")).showAndWait();
                }
            }
        });
    }

    private boolean confirmDeleteMission() {
        return Dialogs.confirmDeleteAnswer(localize("view.mission.question.delete")).isYesOrOk();
    }

}