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
package com.jns.orienteering.view.common;

import javax.inject.Inject;

import com.jns.orienteering.common.BaseService;
import com.jns.orienteering.control.GraphicChoiceField;
import com.jns.orienteering.control.ScrollEventFilter;
import com.jns.orienteering.model.common.AccessType;
import com.jns.orienteering.model.common.ListViewExtended;
import com.jns.orienteering.model.persisted.City;
import com.jns.orienteering.util.Icon;

import javafx.beans.binding.When;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;

public abstract class ListViewPresenter<T> extends BasePresenter {

    private static final String        USER_NOT_LOGGED_IN = localize("view.cities.info.userNotLoggedIn");

    protected ToggleButton             tglAccessType      = Icon.Buttons.accessType();
    protected GraphicChoiceField<City> choiceCityFilter   = new GraphicChoiceField<>(Icon.FILTER.button());

    @FXML
    protected ListViewExtended<T>      lview;
    protected Label                    lblPlaceHolder     = new Label();
    protected ScrollEventFilter        scrollEventFiler;

    @Inject
    protected BaseService              service;
    protected City                     cityFilter;
    protected AccessType                   accessType         = AccessType.PRIVATE;

    @Override
    protected void initialize() {
        super.initialize();

        tglAccessType.selectedProperty().addListener((obs, b, b1) ->
        {
            accessType = b1 ? AccessType.PUBLIC : AccessType.PRIVATE;
            populateListView();
        });

        choiceCityFilter.setStringConverter(City::getCityName);
        choiceCityFilter.setItems(service.getCities());
        choiceCityFilter.getSelectionModel().selectedItemProperty().addListener((obsValue, t, t1) ->
        {
            if (t1 != null) {
                service.setSelectedCity(t1);
                populateListView();
            }
        });

        lblPlaceHolder.textProperty()
                      .bind(new When(service.userProperty()
                                            .isNull()
                                            .and(tglAccessType.selectedProperty().not()))
                                                                                         .then(USER_NOT_LOGGED_IN)
                                                                                         .otherwise(getNoDataExistingMessage()));
        lview.setPlaceholder(lblPlaceHolder);
        scrollEventFiler = new ScrollEventFilter(view);
        service.getActivatorDeactivatorService().add(getViewName(), lview);
    }

    protected abstract String getViewName();

    protected abstract String getTitle();

    protected abstract String getNoDataExistingMessage();

    protected abstract void populateListView();

    @Override
    protected void initAppBar() {
        setAppBar(createGoHomeButton(), getTitle(), tglAccessType, choiceCityFilter);
    }

    protected boolean isPrivateAccess() {
        return accessType == AccessType.PRIVATE;
    }

    protected String getCityIdFilter() {
        return service.getSelectedCity() == null ? null : service.getSelectedCity().getId();
    }
}
