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

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.application.ViewStackPolicy;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.layout.Layer;
import com.gluonhq.charm.glisten.layout.MobileLayoutPane;
import com.gluonhq.charm.glisten.layout.layer.FloatingActionButton;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.jns.orienteering.control.ActionBar;
import com.jns.orienteering.control.Icon;
import com.jns.orienteering.locale.Localization;
import com.jns.orienteering.platform.PlatformProvider;
import com.jns.orienteering.platform.PlatformService;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;

/**
 * Base class for ViewPresenters, which provides some convenience methods.
 *
 */
public abstract class BasePresenter {

    private static final MobileApplication APPLICATION      = MobileApplication.getInstance();
    private static final PlatformService   PLATFORM_SERVICE = PlatformProvider.getPlatformService();

    @FXML
    protected View                         view;
    private AppBar                         appBar;
    private Node                           actionBar;

    @FXML
    protected void initialize() {
        view.setOnShowing(e -> onShowing());
        view.setOnShown(e -> onShown());
        view.setOnHidden(e -> onHidden());
    }

    public MobileApplication getApplication() {
        return APPLICATION;
    }

    /**
     * Called from {@link #onShown()}. Default implementation is empty
     */
    protected void initAppBar() {
    }

    /**
     * @return
     *         the {@link AppBar} instance that is a part of this application.
     */
    protected AppBar getAppBar() {
        if (appBar == null) {
            appBar = APPLICATION.getAppBar();
        }
        return appBar;
    }

    /**
     * Toggles the visibility of the {@link AppBar}
     *
     */
    protected void showAppBar(boolean visible) {
        APPLICATION.getAppBar().setVisible(visible);
    }

    /**
     * Sets the current title of {@link AppBar}
     *
     * @param
     */
    protected void setTitle(String value) {
        getAppBar().setTitleText(value);
    }

    protected void setAppBar(Node navButton, String title) {
        getAppBar().setNavIcon(navButton);
        getAppBar().setTitleText(title);
    }

    protected void setAppBar(Node navButton, String title, Node... actions) {
        setAppBar(navButton, actions);
        getAppBar().setTitleText(title);
    }

    private void setAppBar(Node navButton, Node... actions) {
        getAppBar().setNavIcon(navButton);
        getAppBar().getActionItems().addAll(actions);
    }

    protected void setActionBar(Node... actions) {
        actionBar = new ActionBar(actions);
        view.setBottom(actionBar);
    }

    protected void setActionBarVisible(boolean visible) {
        if (actionBar != null) {
            if (visible) {
                view.setBottom(actionBar);
            } else {
                view.setBottom(null);
            }
        }
    }

    protected FloatingActionButton addFab(MobileLayoutPane pane, String text, EventHandler<ActionEvent> handler) {
        FloatingActionButton fab = new FloatingActionButton(text, handler);
        pane.getLayers().add(fab);
        return fab;
    }

    protected FloatingActionButton addFab(MobileLayoutPane pane, EventHandler<ActionEvent> handler) {
        FloatingActionButton fab = new FloatingActionButton();
        fab.setOnAction(handler);
        pane.getLayers().add(fab);
        return fab;
    }

    protected Button createBackButton() {
        return Icon.Buttons.back(e -> showPreviousView());
    }

    protected Button createGoHomeButton() {
        return Icon.Buttons.back(e -> APPLICATION.goHome());
    }

    protected Button createMenuButton() {
        return MaterialDesignIcon.MENU.button();
    }

    protected void showView(ViewRegistry viewRegistry) {
        APPLICATION.switchView(viewRegistry.getViewName());
    }

    protected void showView(ViewRegistry view, ViewStackPolicy policy) {
        APPLICATION.switchView(view.getViewName(), policy);
    }

    protected void showPreviousView() {
        boolean viewSwitched = APPLICATION.switchToPreviousView();
        if (!viewSwitched) {
            APPLICATION.goHome();
        }
    }

    protected void showHomeView() {
        APPLICATION.goHome();
    }

    protected void addLayer(String name, Layer layer) {
        APPLICATION.addLayerFactory(name, () -> layer);
    }

    protected void showLayer(String name) {
        APPLICATION.showLayer(name);
    }

    protected void hideLayer(String name) {
        APPLICATION.hideLayer(name);
    }

    protected void onShowing() {
    }

    /**
     * Called after the {@link View} is shown. Calls {@link #initAppBar()}
     */
    protected void onShown() {
        initAppBar();
    }

    /**
     * Called just after the View has been hidden.
     */
    protected void onHidden() {
    }

    protected static String localize(String key) {
        return Localization.getString(key);
    }

    protected String localize(Enum<?> type) {
        return Localization.localize(type);
    }

    protected PlatformService platformService() {
        return PLATFORM_SERVICE;
    }

}
