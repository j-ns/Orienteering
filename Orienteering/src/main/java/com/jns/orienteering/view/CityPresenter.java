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

import javax.inject.Inject;

import com.gluonhq.connect.GluonObservable;
import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.GluonObservableObject;
import com.jns.orienteering.common.BaseService;
import com.jns.orienteering.common.MultiValidator;
import com.jns.orienteering.control.FloatingTextField;
import com.jns.orienteering.model.common.ListUpdater;
import com.jns.orienteering.model.dynamic.CityHolder;
import com.jns.orienteering.model.persisted.City;
import com.jns.orienteering.model.persisted.LocalCityList;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.CityFBRepo;
import com.jns.orienteering.model.repo.LocalRepo;
import com.jns.orienteering.util.Dialogs;
import com.jns.orienteering.util.GluonObservableHelper;
import com.jns.orienteering.util.Icon;
import com.jns.orienteering.util.Validators;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class CityPresenter extends BasePresenter {

    private static final String            CITIES_UPDATER = "cities_updater";

    @FXML
    private FloatingTextField              txtCityName;
    @Inject
    private BaseService                    service;
    private CityFBRepo                     cloudRepo;
    private LocalRepo<City, LocalCityList> localRepo;
    private City                           city;

    @Override
    protected void initialize() {
        super.initialize();
        initActionBar();

        cloudRepo = service.getRepoService().getCloudRepo(City.class);
        localRepo = service.getRepoService().getLocalRepo(City.class);
    }

    @Override
    protected void initAppBar() {
        setAppBar(createBackButton(), localize("view.city.title"));
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

        city = service.getSelectedCity();
        if (city == null) {
            city = new City();
        }
        txtCityName.setText(city.getCityName());
    }

    private void onSave() {
        if (!validateCityName(txtCityName.getText())) {
            return;
        }

        saveResultReceiver().onSuccess(e -> showPreviousView())
                            .start();
    }

    private void onSaveAndContinue() {
        if (!validateCityName(txtCityName.getText())) {
            return;
        }

        saveResultReceiver().onSuccess(e ->
        {
            city = new City();
            txtCityName.setText("");
        })
                            .start();
    }

    private boolean validateCityName(String name) {
        MultiValidator<String> validator = new MultiValidator<>();

        validator.addCheck(Validators::isNotNullOrEmpty, localize("view.city.info.nameMustNotBeEmpty"));
        validator.addCheck(cityName ->
        {
            if (isEditorModus()) {
                boolean nameChanged = !service.getSelectedCity().getCityName().equals(cityName);
                if (nameChanged) {
                    return !cloudRepo.checkIfNameExists(cityName);
                }
                return false;

            } else {
                return !cloudRepo.checkIfNameExists(cityName);
            }
        },
                           localize("view.city.error.alreadExists"));

        return validator.check(name);
    }

    private AsyncResultReceiver<GluonObservable> saveResultReceiver() {
        GluonObservable obsSuccessful = new GluonObservableObject<>();

        AsyncResultReceiver.create(saveCity(createCity()))
                           .defaultProgressLayer()
                           .onSuccess(result ->
                           {
                               City cityResult = result.get();
                               GluonObservableList<City> cities = service.getCities();
                               ListUpdater<City> listUpdater = service.getListUpdater(CITIES_UPDATER);

                               if (isEditorModus()) {
                                   listUpdater.remove(city);
                                   cities.remove(city);
                                   CityHolder.remove(city);
                               } else {
                                   cities.add(cityResult);
                               }

                               listUpdater.add(cityResult);
                               CityHolder.put(cityResult);
                               localRepo.createOrUpdateListAsync(new LocalCityList(CityHolder.getAll()));

                               GluonObservableHelper.setInitialized(obsSuccessful, true);
                           })
                           .onException(ex -> GluonObservableHelper.setException(obsSuccessful, ex))
                           .start();

        return AsyncResultReceiver.create(obsSuccessful)
                                  .defaultProgressLayer();
    }

    private City createCity() {
        City newCity = new City(txtCityName.getText(), service.getUser().getId());
        if (isEditorModus()) {
            newCity.setId(city.getId());
        }
        return newCity;
    }

    private GluonObservableObject<City> saveCity(City newCity) {
        GluonObservableObject<City> obsCity = null;

        if (isEditorModus()) {
            obsCity = cloudRepo.updateAsync(newCity, city.getCityName());
        } else {
            obsCity = cloudRepo.createCityAsync(newCity);
        }

        return obsCity;
    }

    private void onDelete() {
        if (!confirmDelete()) {
            return;
        }
        if (!cloudRepo.isCityValidForDelete(city.getId())) {
            Dialogs.ok(localize("view.city.error.usedByTaskOrMission")).showAndWait();
            return;
        }

        GluonObservableObject<City> obsTask = cloudRepo.deleteAsync(city);
        AsyncResultReceiver.create(obsTask)
                           .defaultProgressLayer()
                           .onSuccess(result ->
                           {
                               CityHolder.remove(city);
                               localRepo.createOrUpdateListAsync(new LocalCityList(CityHolder.getAll()));

                               service.getListUpdater(CITIES_UPDATER).remove(city);
                               service.getCities().remove(city);

                               showPreviousView();
                           })
                           .start();

    }

    private boolean confirmDelete() {
        return Dialogs.confirmDeleteAnswer(localize("view.cities.question.delete")).isYesOrOk();
    }

    private boolean isEditorModus() {
        return service.getSelectedCity() != null;
    }
}