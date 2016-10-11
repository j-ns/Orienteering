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

import static com.jns.orienteering.util.DateTimeFormatters.formatLong;
import static com.jns.orienteering.util.DateTimeFormatters.formatTime;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.inject.Inject;

import com.jns.orienteering.common.BaseService;
import com.jns.orienteering.control.Dialogs;
import com.jns.orienteering.control.DurationDisplay;
import com.jns.orienteering.model.persisted.Mission;
import com.jns.orienteering.model.persisted.MissionStat;

import javafx.beans.binding.When;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;

public class HomePresenter extends BasePresenter {

    @FXML
    private Label           lblDate;
    @FXML
    private Label           lblMission;
    @FXML
    private Button          btnActiveMission;
    @FXML
    private Label           lblStart;
    @FXML
    private Label           lblEnd;
    @FXML
    private DurationDisplay lblDuration;
    @FXML
    private Label           lblPoints;

    @FXML
    private Label           lblMissionStatus;
    @FXML
    private HBox            boxButtons;
    @FXML
    private ToggleButton    tglStartStop;
    @FXML
    private Button          btnContinue;

    private Button          btnMenu;

    @Inject
    private BaseService     service;

    @Override
    protected void initialize() {
        super.initialize();

        btnMenu = createMenuButton();
        btnMenu.setOnAction(e ->
        {
            if (tglStartStop.isSelected()) {
                showMissionIsActiveInfo();
            } else {
                showLayer(Navigation.NAVIGATION_DRAWER);
            }
        });

        btnActiveMission.textProperty().bind(new When(service.activeMissionNameProperty().isNull())
                                                                                                   .then(localize("button.selectMission"))
                                                                                                   .otherwise(service.activeMissionNameProperty()));

        btnActiveMission.setOnAction(e -> onActiveMissionSelected());

        tglStartStop.setSelected(false);
        tglStartStop.selectedProperty().addListener(onStartStopSelected());
        tglStartStop.disableProperty().bind(service.activeMissionProperty().isNull());

        btnContinue.setOnAction(e -> showView(ViewRegistry.ACTIVE_MISSION));
    }

    private ChangeListener<? super Boolean> onStartStopSelected() {
        return (obsValue, b, b1) ->
        {
            service.stopMissionProperty().set(!b1);

            if (b1) {
                tglStartStop.setText(localize("button.stop"));
                boxButtons.getChildren().add(btnContinue);
                lblEnd.setText("");
                showView(ViewRegistry.ACTIVE_MISSION);

            } else {
                tglStartStop.setText(localize("button.start"));
                boxButtons.getChildren().remove(btnContinue);

                if (service.getActiveMissionStats() != null && !service.getActiveMissionStats().isFinished()) {
                    lblEnd.setText(formatTime(LocalTime.now()));
                    lblDuration.stop();
                }
            }
        };
    }

    @Override
    protected void initAppBar() {
        setAppBar(btnMenu, localize("view.home.title"));
    }

    @Override
    protected void onShowing() {
        if (!service.isInitialized()) {
            showView(ViewRegistry.START);
        }
    }

    @Override
    protected void onShown() {
        super.onShown();

        lblDate.setText(formatLong(LocalDate.now()));

        Mission mission = service.getActiveMission();
        MissionStat missionStats = service.getActiveMissionStats();

        if (missionStats != null) {
            if (!missionStats.isFinished()) {
                lblDuration.startAt(missionStats.getDuration());
            } else {
                lblEnd.setText(formatTime(missionStats.getEnd()));
                lblDuration.setDuration(missionStats.getDuration());
            }
            lblStart.setText(formatTime(missionStats.getStart()));
            lblDuration.setVisible(true);
            lblPoints.setText(missionStats.getPoints() + " / " + mission.getMaxPoints());
        } else {
            clearFields();
        }
    }

    private void onActiveMissionSelected() {
        if (tglStartStop.isSelected() && !confirmChangeMission()) {
            return;
        }
        service.stopMissionProperty().set(true);
        service.setActiveMissionStat(null);
        tglStartStop.setSelected(false);
        clearFields();

        if (service.getActiveMission() == null) {
            showView(ViewRegistry.MISSIONS);
        } else {
            ((ListViewPresenter<?>) ViewRegistry.MISSIONS.getPresenter()).setAccessType(service.getActiveMission().getAccessType());
            service.setSelectedCityById(service.getActiveMission().getCityId());
            showView(ViewRegistry.MISSIONS);
        }
    }

    private void showMissionIsActiveInfo() {
        Dialogs.ok(localize("view.home.info.navigationUnavailable")).showAndWait();
    }

    private boolean confirmChangeMission() {
        return Dialogs.cancelOkAnswer(localize("view.home.question.changeMission"), localize("button.cancel"), localize("button.change")).isYesOrOk();
    }

    private void clearFields() {
        lblStart.setText("");
        lblEnd.setText("");
        lblDuration.stop();
        lblDuration.setVisible(false);
        lblPoints.setText("");
    }

    @Override
    protected void onHidden() {
        lblDuration.stop();
    }
}
