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

import static com.jns.orienteering.control.Dialogs.confirmDeleteAnswer;

import java.util.function.Consumer;

import javax.inject.Inject;

import com.gluonhq.charm.glisten.layout.layer.FloatingActionButton;
import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.GluonObservableObject;
import com.jns.orienteering.common.BaseService;
import com.jns.orienteering.control.Dialogs;
import com.jns.orienteering.control.Icon;
import com.jns.orienteering.control.ListViewExtended;
import com.jns.orienteering.control.ScrollEventFilter;
import com.jns.orienteering.control.StateButton;
import com.jns.orienteering.control.cell.CityCell;
import com.jns.orienteering.model.dynamic.CityCache;
import com.jns.orienteering.model.persisted.AccessType;
import com.jns.orienteering.model.persisted.City;
import com.jns.orienteering.model.persisted.LocalCityList;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.CityFBRepo;
import com.jns.orienteering.model.repo.LocalRepo;

import javafx.application.Platform;
import javafx.beans.binding.When;
import javafx.fxml.FXML;

public class CitiesPresenter extends BasePresenter {

    private static final String            USER_NOT_SIGNED_IN = localize("view.cities.info.userNotLoggedIn");
    private static final String            NO_CITY_EXISTING   = localize("view.cities.info.noCityExisting");

    private StateButton<AccessType>        tglAccessType;

    @FXML
    private ListViewExtended<City>         lview;
    private ScrollEventFilter              scrollEventFilter;

    @Inject
    private BaseService                    service;
    private CityFBRepo                     cloudRepo;
    private LocalRepo<City, LocalCityList> localRepo;
    private CityCache                 localCityCache     = CityCache.INSTANCE;

    @Override
    protected void initialize() {
        super.initialize();

        FloatingActionButton fab = addFab(view, e -> onCreateCity());
        fab.visibleProperty().bind(service.userProperty().isNotNull());

        tglAccessType = Icon.Buttons.accessType();
        tglAccessType.setOnAction(() -> populateListView());

        lview.getPlaceHolder().textProperty().bind(new When(service.userProperty()
                                                           .isNull().and(tglAccessType.selectedProperty().not()))
                                                                                                                 .then(USER_NOT_SIGNED_IN)
                                                                                                                 .otherwise(NO_CITY_EXISTING));

        lview.setComparator(City::compareTo);
        lview.setOnSelection(this::onSelect);
        scrollEventFilter = new ScrollEventFilter(lview);
        service.getActivatorDeactivatorService().add(ViewRegistry.CITIES.getViewName(), lview);

        cloudRepo = service.getRepoService().getCloudRepo(City.class);
        localRepo = service.getRepoService().getLocalRepo(City.class);
    }

    @Override
    protected void initAppBar() {
        setAppBar(createBackButton(), localize("view.cities.title"), tglAccessType);
    }

    @Override
    protected void onShown() {
        super.onShown();

        if (ViewRegistry.CITY.equals(service.getPreviousViewName())) {
            service.setSelectedCity(null);
            lview.refresh();
        } else {
            populateListView();
        }
    }

    private void populateListView() {
        updateCellFactory();

        GluonObservableList<City> cities = isPrivateAccess() ? localCityCache.getPrivateCities() : localCityCache.getPublicCities();
        AsyncResultReceiver.create(cities)
                           .defaultProgressLayer()
                           .onSuccess(lview::setSortableItems)
                           .start();
    }

    private void updateCellFactory() {
        Consumer<City> consumerLeft = isPrivateAccess() ? this::onDelete : null;

        lview.setCellFactory(listView -> new CityCell(lview.selectedItemProperty(), consumerLeft, this::onSetDefault, scrollEventFilter
                                                                                                                                       .slidingProperty()));
    }

    private void onCreateCity() {
        service.setSelectedCity(null);
        showView(ViewRegistry.CITY);
    }

    private void onSelect(City city) {
        if (city != null) {
            if (!isPrivateAccess() && !city.getOwnerId().equals(service.getUserId())) {
                Dialogs.ok(localize("view.cities.info.onlyOwnerCanEditCity")).showAndWait();
                return;
            }
            service.setSelectedCity(city);
            showView(ViewRegistry.CITY);
        }
    }

    private void onSetDefault(City city) {
        if (service.getUser() == null) {
            Platform.runLater(() -> Dialogs.ok(localize("view.cities.info.userNotLoggedInCantSetCity")).showAndWait());
            return;
        }
        service.setDefaultCity(city);
        Platform.runLater(() -> platformService().getInfoService()
                                                 .showToast(localize("view.cities.defaultCitySetTo") + city.getCityName()));
    }

    private void onDelete(City city) {
        Platform.runLater(() ->
        {
            if (!confirmDeleteAnswer(localize("view.cities.question.delete")).isYesOrOk()) {
                return;
            }
            if (!cloudRepo.isCityValidForDelete(city.getId())) {
                Dialogs.ok(localize("view.cities.error.cityIsUsedByTaskOrMission")).showAndWait();
                return;
            }

            GluonObservableObject<City> obsCity = cloudRepo.deleteAsync(city);
            AsyncResultReceiver.create(obsCity)
                               .defaultProgressLayer()
                               .onSuccess(e ->
                               {
                                   localRepo.createOrUpdateListAsync(new LocalCityList(CityCache.INSTANCE.getPublicCities()));
                                   localCityCache.remove(city);
                               })
                               .start();
        });
    }

    protected boolean isPrivateAccess() {
        return !tglAccessType.isSelected();
    }

    @Override
    protected void onHidden() {
        lview.clearSelection();
    }
}