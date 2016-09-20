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

import java.io.IOException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.connect.GluonObservableList;
import com.jns.orienteering.common.BaseService;
import com.jns.orienteering.common.MultiValidator;
import com.jns.orienteering.control.FloatingTextField;
import com.jns.orienteering.model.common.ListUpdater;
import com.jns.orienteering.model.dynamic.CityHolder;
import com.jns.orienteering.model.persisted.City;
import com.jns.orienteering.model.persisted.LocalCityList;
import com.jns.orienteering.model.repo.CityFBRepo;
import com.jns.orienteering.model.repo.LocalRepo;
import com.jns.orienteering.util.Dialogs;
import com.jns.orienteering.util.Icon;
import com.jns.orienteering.util.Validators;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class CityPresenter extends BasePresenter {

    private static final Logger            LOGGER         = LoggerFactory.getLogger(CityPresenter.class);

    private static final String            CITIES_UPDATER = "cities_updater";

    @FXML
    private FloatingTextField              txtCityName;
    @Inject
    private BaseService                    service;
    private CityFBRepo                       cloudRepo;
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
        if (save()) {
            showPreviousView();
        }
    }

    private void onSaveAndContinue() {
        if (save()) {
            city = new City();
            txtCityName.setText("");
        }
    }

    private boolean save() {
        if (!validateCityName(txtCityName.getText())) {
            return false;
        }

        City newCity = new City(txtCityName.getText(), service.getUser().getId());
        ListUpdater<City> listUpdater = service.getListUpdater(CITIES_UPDATER);
        try {
            if (isEditorModus()) {
                newCity.setId(city.getId());

                cloudRepo.update(newCity, city.getCityName());
                localRepo.createOrUpdateListAsync(new LocalCityList(CityHolder.getAll()));

                listUpdater.replace(city, newCity);

                GluonObservableList<City> cities = service.getCities();
                int idx = cities.indexOf(city);
                cities.set(idx, newCity);

            } else {
                cloudRepo.create(newCity);
                localRepo.createOrUpdateListAsync(new LocalCityList(CityHolder.getAll()));

                listUpdater.add(newCity);
                service.getCities().add(newCity);
            }

        } catch (IOException e) {
            LOGGER.error("Error saving city: '{}'", newCity.getCityName(), e);
            Dialogs.ok(localize("view.city.error.save")).showAndWait();
            return false;
        }
        return true;
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

    private void onDelete() {
        if (!confirmDelete()) {
            return;
        }
        if (!cloudRepo.isCityValidForDelete(city.getId())) {
            Dialogs.ok(localize("view.city.error.usedByTaskOrMission")).showAndWait();
            return;
        }
        try {
            cloudRepo.delete(city);
            localRepo.createOrUpdateListAsync(new LocalCityList(CityHolder.getAll()));

            service.getListUpdater(CITIES_UPDATER).remove(city);
            service.getCities().remove(city);

            showPreviousView();

        } catch (IOException e) {
            LOGGER.error("Error deleting city", e);
            Dialogs.ok(localize("view.cities.error.delete")).showAndWait();
        }
    }

    private boolean confirmDelete() {
        return Dialogs.confirmDeleteAnswer(localize("view.cities.question.delete")).isYesOrOk();
    }

    private boolean isEditorModus() {
        return service.getSelectedCity() != null;
    }
}