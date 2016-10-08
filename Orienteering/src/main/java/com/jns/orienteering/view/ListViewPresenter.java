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

import com.jns.orienteering.common.BaseService;
import com.jns.orienteering.control.GraphicChoiceField;
import com.jns.orienteering.control.Icon;
import com.jns.orienteering.control.ListViewExtended;
import com.jns.orienteering.control.ScrollEventFilter;
import com.jns.orienteering.control.SelectState;
import com.jns.orienteering.control.StateButton;
import com.jns.orienteering.model.common.AccessType;
import com.jns.orienteering.model.dynamic.ModelCache;
import com.jns.orienteering.model.persisted.City;

import javafx.beans.binding.When;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public abstract class ListViewPresenter<T> extends BasePresenter {

    private static final String        USER_NOT_LOGGED_IN = localize("view.cities.info.userNotLoggedIn");

    protected StateButton<AccessType>  tglAccessType      = Icon.Buttons.accessType();
    protected GraphicChoiceField<City> choiceCity         = new GraphicChoiceField<>(Icon.FILTER.button());
    protected Button                   btnRefresh;

    @FXML
    protected ListViewExtended<T>      lview;
    protected ScrollEventFilter        scrollEventFilter;

    @Inject
    protected BaseService              service;

    protected City                     selectedCity;
    private boolean                    accessTypeListenerMuted;

    @Override
    protected void initialize() {
        super.initialize();

        btnRefresh = Icon.Buttons.refresh(e ->
        {
            getLocalCache().clearItems(tglAccessType.getSelectState().get());
            populateListView();
        });

        SelectState<AccessType> selectState = new SelectState<AccessType>(AccessType.PRIVATE, AccessType.PUBLIC);
        tglAccessType.setSelectState(selectState);
        tglAccessType.setOnAction(() ->
        {
            if (!accessTypeListenerMuted) {
                populateListView();
            }
        });

        choiceCity.setStringConverter(City::getCityName);
        choiceCity.setMissingDataTitle(localize("dialog.info.noCityOrNoConnection"));
        choiceCity.setItems(service.getCitiesSorted());
        choiceCity.getSelectionModel().selectedItemProperty().addListener((obsValue, c, c1) ->
        {
            if (c1 != null) {
                service.setSelectedCity(c1);
                populateListView();
            }
        });

        lview.getPlaceHolder().textProperty()
             .bind(new When(service.userProperty().isNull()).then(USER_NOT_LOGGED_IN)
                                                            .otherwise(getNoDataExistingMessage()));

        scrollEventFilter = new ScrollEventFilter(lview);
        service.getActivatorDeactivatorService().add(getViewName(), lview);
    }

    protected abstract String getViewName();

    protected abstract String getTitle();

    protected abstract String getNoDataExistingMessage();

    protected abstract ModelCache<?> getLocalCache();

    protected abstract void populateListView();

    @Override
    protected void initAppBar() {
        setAppBar(createGoHomeButton(), getTitle(), btnRefresh, tglAccessType, choiceCity);
    }

    public void setAccessType(AccessType accessType) {
        accessTypeListenerMuted = true;
        tglAccessType.setSelected(accessType == AccessType.PUBLIC);
        accessTypeListenerMuted = false;
    }

    protected boolean isPrivateAccess() {
        return tglAccessType.getSelectState().get() == AccessType.PRIVATE;
    }

    @Override
    protected void onHidden() {
        lview.clearSelection();
    }
}
