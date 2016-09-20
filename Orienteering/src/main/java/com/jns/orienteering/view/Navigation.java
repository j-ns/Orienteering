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

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.Avatar;
import com.gluonhq.charm.glisten.control.NavigationDrawer;
import com.gluonhq.charm.glisten.control.NavigationDrawer.Item;
import com.gluonhq.charm.glisten.mvc.View;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

public class Navigation {

    public static final String NAVIGATION_DRAWER = "navigation_drawer";

    private NavigationDrawer     navigationDrawer;
    private Avatar               avatar;
    private Label                lblAlias;
    private ObjectProperty<View> view = new SimpleObjectProperty<>();

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
            MobileApplication.getInstance().hideLayer(NAVIGATION_DRAWER);
            MobileApplication.getInstance().switchView(ViewRegistry.USER.getViewName());
        });

        navigationDrawer = new NavigationDrawer();
        navigationDrawer.setHeader(boxHeader);
        navigationDrawer.selectedItemProperty().addListener((obs, t, t1) ->
        {
            if (t1 != null) {
                navigationDrawer.setSelectedItem(null);
            }
        });

        for (ViewRegistry registry : ViewRegistry.values()) {
            Item menuItem = registry.getMenuItem();
            if (menuItem != null) {
                navigationDrawer.getItems().add(menuItem);
            }
        }
    }

    public ObjectProperty<View> viewProperty() {
        return view;
    }

    public StringProperty aliasProperty() {
        return lblAlias.textProperty();
    }

    public ObjectProperty<Image> profileImageProperty() {
        return avatar.imageProperty();
    }

    public NavigationDrawer getNavigationDrawer() {
        return navigationDrawer;
    }

}
