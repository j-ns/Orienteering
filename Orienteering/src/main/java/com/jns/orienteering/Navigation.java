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
package com.jns.orienteering;

import static com.jns.orienteering.locale.Localization.localize;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.Avatar;
import com.gluonhq.charm.glisten.control.NavigationDrawer;
import com.gluonhq.charm.glisten.control.NavigationDrawer.Item;
import com.gluonhq.charm.glisten.mvc.View;
import com.jns.orienteering.util.Icon;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

public class Navigation {

    private static final String  ICON_SIZE = "22";

    private NavigationDrawer     navigationDrawer;
    private Avatar               avatar;
    private Label                lblAlias;
    private ObjectProperty<View> view      = new SimpleObjectProperty<>();

    public Navigation() {
        avatar = new Avatar(32);

        lblAlias = new Label();
        lblAlias.getStyleClass().add("title");

        HBox boxHeader = new HBox(avatar, lblAlias);
        boxHeader.setSpacing(16);
        boxHeader.getStyleClass().add("header");
        boxHeader.setAlignment(Pos.CENTER_LEFT);
        boxHeader.setPadding(new Insets(24, 16, 8, 16));

        boxHeader.setOnMouseClicked(e ->
        {
            MobileApplication.getInstance().hideLayer(OrienteeringApp.NAVIGATION_DRAWER);
            MobileApplication.getInstance().switchView(OrienteeringApp.USER_VIEW);
        });

        Item itmMissions = new NavigationDrawer.Item(localize("navigation.item.missions"), Icon.MAP.icon(ICON_SIZE));
        itmMissions.setUserData(OrienteeringApp.MISSIONS_VIEW);

        Item itmTasks = new NavigationDrawer.Item(localize("navigation.item.tasks"), Icon.MAP_MARKER.icon(ICON_SIZE));
        itmTasks.setUserData(OrienteeringApp.TASKS_VIEW);

        Item itmReport = new NavigationDrawer.Item(localize("navigation.item.stats"), Icon.LINE_CHART.icon(ICON_SIZE));
        itmReport.setUserData(OrienteeringApp.REPORT_VIEW);

        Item itmCities = new NavigationDrawer.Item(localize("navigation.item.cities"), Icon.LINE_CHART.icon(ICON_SIZE));
        itmCities.setUserData(OrienteeringApp.CITIES_VIEW);

//        Item itmSettings = new NavigationDrawer.Item(localize("navigation.item.settings"), Icon.SETTINGS.icon(ICON_SIZE));
//        Item itmHelp = new NavigationDrawer.Item(localize("navigation.item.help"), Icon.HELP.icon(ICON_SIZE));

        navigationDrawer = new NavigationDrawer();
        navigationDrawer.setHeader(boxHeader);
        navigationDrawer.getItems().setAll(itmMissions, itmTasks, itmReport, itmCities);

        navigationDrawer.selectedItemProperty().addListener(selectedItemListener);

        view.addListener((obs, v, v1) ->
        {
            for (Node node : navigationDrawer.getItems()) {
                NavigationDrawer.Item item = (NavigationDrawer.Item) node;
                if (item.getUserData() == null) {
                    continue;
                }

                if (item.getUserData().equals(v1.getName())) {
                    navigationDrawer.selectedItemProperty().removeListener(selectedItemListener);
                    item.setSelected(true);
                    navigationDrawer.selectedItemProperty().addListener(selectedItemListener);
                } else {
                    item.setSelected(false);
                }
            }
        });
    }

    public StringProperty aliasProperty() {
        return lblAlias.textProperty();
    }

    public ObjectProperty<Image> profileImageProperty() {
        return avatar.imageProperty();
    }

    public ObjectProperty<View> viewProperty() {
        return view;
    }

    private ChangeListener<? super Node> selectedItemListener = (obs, t, t1) ->
    {
        if (t1 == null) {
            return;
        }
        String viewName = (String) t1.getUserData();
        if (viewName != null) {
            MobileApplication.getInstance().hideLayer(OrienteeringApp.NAVIGATION_DRAWER);
            MobileApplication.getInstance().switchView(viewName);
        }
        navigationDrawer.setSelectedItem(null);
    };

    public NavigationDrawer getNavigationDrawer() {
        return navigationDrawer;
    }

}
