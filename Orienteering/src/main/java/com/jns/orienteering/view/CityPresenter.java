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

import javax.inject.Inject;

import com.gluonhq.connect.GluonObservable;
import com.gluonhq.connect.GluonObservableObject;
import com.jns.orienteering.common.BaseService;
import com.jns.orienteering.common.MultiValidator;
import com.jns.orienteering.control.FloatingTextField;
import com.jns.orienteering.control.Icon;
import com.jns.orienteering.model.dynamic.CityCache;
import com.jns.orienteering.model.persisted.City;
import com.jns.orienteering.model.persisted.LocalCityList;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.CityFBRepo;
import com.jns.orienteering.model.repo.LocalRepo;
import com.jns.orienteering.util.SpecialCharReplacer;
import com.jns.orienteering.util.Validations;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class CityPresenter extends BasePresenter {

    @FXML
    private FloatingTextField              txtCityName;
    @Inject
    private BaseService                    service;
    private CityFBRepo                     cloudRepo;
    private LocalRepo<City, LocalCityList> localRepo;
    private CityCache                      cityCache;
    private MultiValidator<String>         cityNameValidator;

    private City                           city;

    @Override
    protected void initialize() {
        super.initialize();
        initActionBar();

        cloudRepo = service.getRepoService().getCloudRepo(City.class);
        localRepo = service.getRepoService().getLocalRepo(City.class);
        cityCache = CityCache.INSTANCE;
    }

    @Override
    protected void initAppBar() {
        setAppBar(createBackButton(), localize("view.city.title"));
    }

    private void initActionBar() {
        Button btnSave = Icon.Buttons.actionBarButton(Icon.DONE, localize("label.save"), e -> onSave());
        Button btnSaveAndContinue = Icon.Buttons.actionBarButton(Icon.DONE_ALL, localize("label.saveNext"), e -> onSaveAndContinue());
        Button btnDelete = Icon.Buttons.actionBarButton(Icon.DELETE, localize("label.delete"), e -> onDelete());
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
        if (!validateCityName()) {
            return;
        }
        saveResultReceiver().onSuccess(e -> showPreviousView())
                            .start();
    }

    private void onSaveAndContinue() {
        if (!validateCityName()) {
            return;
        }
        saveResultReceiver()
                            .onSuccess(e ->
                            {
                                city = new City();
                                txtCityName.setText("");
                            })
                            .start();
    }

    private boolean validateCityName() {
        return getCityNameValidator().check(txtCityName.getText());
    }

    private MultiValidator<String> getCityNameValidator() {
        if (cityNameValidator == null) {
            cityNameValidator = createCityNameValidator();
        }
        return cityNameValidator;
    }

    private MultiValidator<String> createCityNameValidator() {
        MultiValidator<String> validator = new MultiValidator<>();

        validator.addCheck(Validations::isNotNullOrEmpty, localize("view.city.info.nameMustNotBeEmpty"));
        validator.addCheck(SpecialCharReplacer::validateInput, localize("view.error.invalidCharEntered"));
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
        }, localize("view.city.error.alreadExists"));

        return validator;
    }

    private AsyncResultReceiver<GluonObservable> saveResultReceiver() {
        GluonObservable obsSuccessful = new GluonObservableObject<>();

        AsyncResultReceiver.create(saveCity(createCity()))
                           .defaultProgressLayer()
                           .onSuccess(result ->
                           {
                               if (isEditorModus()) {
                                   cityCache.remove(city);
                               }
                               cityCache.put(result.get());

                               localRepo.createOrUpdateListAsync(new LocalCityList(cityCache.getPublicCities()));
                           })
                           .setInitializedOnSuccess(obsSuccessful)
                           .propagateException(obsSuccessful)
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
            obsCity = cloudRepo.createAsync(newCity);
        }
        return obsCity;
    }

    private void onDelete() {
        if (!confirmDeleteAnswer(localize("view.cities.question.delete")).isYesOrOk()) {
            return;
        }
        if (!cloudRepo.isCityValidForDelete(city.getId())) {
            showInfo(localize("view.city.error.usedByTaskOrMission"));
            return;
        }

        GluonObservableObject<City> obsTask = cloudRepo.deleteAsync(city);
        AsyncResultReceiver.create(obsTask)
                           .defaultProgressLayer()
                           .onSuccess(result ->
                           {
                               cityCache.remove(city);
                               localRepo.createOrUpdateListAsync(new LocalCityList(cityCache.getPublicCities()));
                               showPreviousView();
                           })
                           .start();
    }

    private boolean isEditorModus() {
        return service.getSelectedCity() != null;
    }
}