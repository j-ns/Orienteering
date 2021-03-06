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
/*
* Copyright (c) 2016, Jens Stroh
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

import static com.jns.orienteering.control.Dialogs.showInfo;

import javax.inject.Inject;

import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.GluonObservableObject;
import com.jns.orienteering.common.BaseService;
import com.jns.orienteering.control.ListViewExtended;
import com.jns.orienteering.control.cell.RankingCell;
import com.jns.orienteering.control.cell.StatCell;
import com.jns.orienteering.control.cell.TimeLineCell;
import com.jns.orienteering.model.dynamic.Ranking;
import com.jns.orienteering.model.persisted.MissionStat;
import com.jns.orienteering.model.persisted.StatByUser;
import com.jns.orienteering.model.persisted.TaskStat;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.MissionStatFBRepo;

import javafx.beans.binding.When;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class ReportPresenter extends BasePresenter {

    private static final int             TAB_MISSION_STATS  = 0;
    private static final int             TAB_MISSION_STAT   = 1;
    private static final int             TAB_RANKING        = 2;

    private static final String          USER_NOT_LOGGED_IN = localize("view.report.info.userNotLoggedIn");
    private static final String          NO_STAT_EXISTING   = localize("view.report.info.noStatExisting");

    @FXML
    private TabPane                      tabPane;
    @FXML
    private Tab                          tabMissionStats;
    @FXML
    private Tab                          tabMissionStat;
    @FXML
    private Tab                          tabRanking;
    @FXML
    private ListViewExtended<StatByUser> lviewMissionStats;
    @FXML
    private ListViewExtended<TaskStat>   lviewMissionStat;
    @FXML
    private ListViewExtended<Ranking>    lviewRanking;

    @Inject
    private BaseService                  service;
    private MissionStatFBRepo            missionStatRepo;

    private MissionStat                  missionStat;
    private boolean                      rankingNeedsRefresh;

    @Override
    protected void initialize() {
        super.initialize();

        lviewMissionStat.getPlaceHolder().textProperty().bind(new When(service.userProperty().isNull()).then(USER_NOT_LOGGED_IN)
                                                                                                       .otherwise(NO_STAT_EXISTING));

        lviewMissionStats.setSelectableCellFactory(StatCell::new);
        lviewMissionStat.setCellFactory(listView -> new TimeLineCell());
        lviewRanking.setCellFactory(listView -> new RankingCell());

        lviewMissionStats.setComparator(StatByUser::compareTo);
        lviewMissionStats.setOnSelection(this::onSelectStat);

        service.getActivatorDeactivatorService().add(ViewRegistry.REPORT.getViewName(), lviewMissionStats);
        service.getActivatorDeactivatorService().add(ViewRegistry.REPORT.getViewName(), lviewMissionStat);
        service.getActivatorDeactivatorService().add(ViewRegistry.REPORT.getViewName(), lviewRanking);

        tabPane.getSelectionModel().selectedIndexProperty().addListener((obsValue, idx, idx1) ->
        {
            if (idx1.intValue() == TAB_RANKING && rankingNeedsRefresh) {
                updateRankings();
            }
        });

        missionStatRepo = service.getRepoService().getCloudRepo(MissionStat.class);
    }

    @Override
    protected void initAppBar() {
        setAppBar(createGoHomeButton(), localize("view.report.title"));
    }

    @Override
    protected void onShown() {
        super.onShown();

        lviewMissionStat.setItems(FXCollections.emptyObservableList());
        lviewRanking.setItems(FXCollections.emptyObservableList());
        tabPane.getSelectionModel().select(TAB_MISSION_STATS);

        if (service.getUser() == null) {
            return;
        }

        GluonObservableList<StatByUser> obsStatsByUser = missionStatRepo.getStatsByUserAsync(service.getUserId());
        AsyncResultReceiver.create(obsStatsByUser)
                           .defaultProgressLayer()
                           .onSuccess(lviewMissionStats::setSortableItems)
                           .exceptionMessage(localize("view.report.error.loadStats"))
                           .start();
    }

    private void onSelectStat(StatByUser stat) {
        if (stat == null) {
            return;
        }
        lviewRanking.setItems(FXCollections.emptyObservableList());

        GluonObservableObject<MissionStat> obsMissionStat = missionStatRepo.retrieveObjectAsync(stat.getLookupId());
        AsyncResultReceiver.create(obsMissionStat)
                           .defaultProgressLayer()
                           .onSuccess(result ->
                           {
                               missionStat = result.get();

                               if (missionStat != null) {
                                   lviewMissionStat.setItems(missionStat.getTaskStatsWithSummary());
                                   tabPane.getSelectionModel().select(TAB_MISSION_STAT);
                                   rankingNeedsRefresh = true;

                               } else {
                                   missionStatRepo.deleteStatByUserAsync(service.getUserId(), stat.getMissionId());

                                   showInfo(localize("view.report.info.statDeletedBecauseOfMissionUpdate"));

                                   lviewMissionStats.getListUpdater().remove(stat);
                                   lviewMissionStat.setItems(FXCollections.emptyObservableList());
                                   lviewRanking.setItems(FXCollections.emptyObservableList());
                               }
                           })
                           .onException(() ->
                           {
                               lviewMissionStat.setItems(FXCollections.emptyObservableList());
                               lviewRanking.setItems(FXCollections.emptyObservableList());
                           })
                           .start();
    }

    private void updateRankings() {
        GluonObservableList<MissionStat> obsMissionStats = missionStatRepo.getMissionStatsAsync(missionStat.getMissionId());
        AsyncResultReceiver.create(obsMissionStats)
                           .defaultProgressLayer()
                           .onSuccess(result ->
                           {
                               lviewRanking.setItems(new RankingList(result).get());
                               rankingNeedsRefresh = false;
                           })
                           .onException(() -> lviewRanking.setItems(FXCollections.emptyObservableList()))
                           .start();
    }

    private static class RankingList {

        private ObservableList<Ranking> rankings       = FXCollections.observableArrayList();
        private SortedList<Ranking>     rankingsSorted = new SortedList<>(rankings, Ranking::compareTo);

        private RankingList(ObservableList<MissionStat> missionStats) {
            createRankings(missionStats);
        }

        private void createRankings(ObservableList<MissionStat> missionStats) {
            for (MissionStat missionStat : missionStats) {
                rankings.add(new Ranking(missionStat));
            }

            rankingsSorted.get(0).setReferenceDuration();

            int place = 1;
            for (Ranking candidate : rankingsSorted) {
                candidate.setPlace(place++);
                candidate.calculateTimeDifference();
            }
        }

        private ObservableList<Ranking> get() {
            return rankingsSorted;
        }

    }

}