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

import static com.jns.orienteering.locale.Localization.localize;

import com.airhacks.afterburner.injection.Injector;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.NavigationDrawer;
import com.gluonhq.charm.glisten.control.NavigationDrawer.Item;
import com.jns.orienteering.util.Icon;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;

public enum ViewRegistry {

    START("start", StartPresenter.class),
    HOME("home", HomePresenter.class),
    USER("user", UserPresenter.class),
    ACCOUNT("account", AccountPresenter.class),
    MISSIONS("missions", MissionsPresenter.class, Icon.MAP.icon(iconSize()), localize("navigation.item.missions")),
    MISSION("mission", MissionPresenter.class),
    ACTIVE_MISSION("active_misison", ActiveMissionPresenter.class),
    TASKS("tasks", TasksPresenter.class, Icon.MAP_MARKER.icon(iconSize()), localize("navigation.item.tasks")),
    TASK("task", TaskPresenter.class),
    CITIES("cities", CitiesPresenter.class, Icon.GLOBE.icon(iconSize()), localize("navigation.item.cities")),
    CITY("city", CityPresenter.class),
    REPORT("report", ReportPresenter.class, Icon.LINE_CHART.icon(iconSize()), localize("navigation.item.stats"));

    private String                         viewId;
    private Class<? extends BasePresenter> presenterClass;
    private Node                           menuGraphic;
    private String                         menuTitle;
    private Item                           menuItem;

    private BasePresenter                  presenter;

    private static Navigation              navigation = new Navigation();

    static {
        Injector.setInstanceSupplier(c ->
        {
            if (BasePresenter.class.isAssignableFrom(c)) {
                return ViewRegistry.getPresenter(c);
            }
            return createInstance(c);
        });
    }

    private static <T> T createInstance(Class<T> c) {
        try {
            return c.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new IllegalStateException("Cannot instantiate class: " + c);
        }
    }

    ViewRegistry(String viewName, Class<? extends BasePresenter> presenterClass) {
        this(viewName, presenterClass, null, null);
    }

    ViewRegistry(String viewId, Class<? extends BasePresenter> presenterClass, Node menuGraphic, String menuTitle) {
        this.viewId = viewId;
        this.presenterClass = presenterClass;
        this.menuGraphic = menuGraphic;
        this.menuTitle = menuTitle;
    }

    public static void registerViews(MobileApplication app) {
        for (ViewRegistry registry : values()) {
            registry.registerView(app);
        }
    }

    public void registerView(MobileApplication app) {
        app.addViewFactory(viewId, () ->
        {
            BaseView view = new BaseView(presenterClass);
            view.getView().setName(viewId);
            return view.getView();
        });
    }

    public String getViewName() {
        return viewId;
    }

    private static BasePresenter getPresenter(Class<?> presenterClass) {
        for (ViewRegistry registry : values()) {
            if (registry.getPresenterClass() == presenterClass) {
                return registry.getPresenter();
            }
        }
        throw new IllegalArgumentException("unknown presenterClass: " + presenterClass);
    }

    public Class<? extends BasePresenter> getPresenterClass() {
        return presenterClass;
    }

    /**
     * Lazy initializes the presenter for the view.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends BasePresenter> T getPresenter() {
        if (presenter == null) {
            presenter = createInstance(presenterClass);
        }
        return (T) presenter;
    }

    public Node getMenuGraphic() {
        return menuGraphic;
    }

    public Item getMenuItem() {
        if (menuItem == null && menuGraphic != null) {
            menuItem = new NavigationDrawer.Item(menuTitle, menuGraphic);
            menuItem.selectedProperty().addListener(selectedItemListener);
        }
        return menuItem;
    }

    private ChangeListener<? super Boolean> selectedItemListener = (obsValue, b, b1) ->
    {
        if (b1) {
            MobileApplication.getInstance().hideLayer(Navigation.NAVIGATION_DRAWER);
            MobileApplication.getInstance().switchView(viewId);
            menuItem.setSelected(false);
        }
    };

    private static String iconSize() {
        return "22";
    }

    public static Navigation getNavigation() {
        return navigation;
    }

    public boolean equals(String viewName) {
        return viewId.equals(viewName);
    }

}
