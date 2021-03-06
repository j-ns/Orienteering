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
package com.jns.orienteering;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.jns.orienteering.control.ProgressLayer;
import com.jns.orienteering.platform.PlatformProvider;
import com.jns.orienteering.view.StartView;
import com.jns.orienteering.view.ViewRegistry;

import javafx.scene.Scene;

public class OrienteeringApp extends MobileApplication {

    /*
     * WARNING: To be able to use the functions of the app, the User must have an Internet connection, which may be subjected to
     * costs. Use on your own risk.
     */

    @Override
    public void init() {
        PlatformProvider.getPlatformService().checkPermissions();

        ViewRegistry.registerView(this, SPLASH_VIEW, () -> new StartView());
        ViewRegistry.registerViews(this);
        ViewRegistry.registerNavigation(this);

        addLayerFactory(ProgressLayer.DEFAULT_LAYER_NAME, ProgressLayer::new);

    }

    @Override
    public void postInit(Scene scene) {
        scene.getStylesheets().add(getClass().getResource("/com/jns/orienteering/base.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/com/jns/orienteering/application.css").toExternalForm());
        // Stage stage = (Stage) scene.getWindow();
        // stage.setOnCloseRequest(e -> ImageHandler.shutdownExecutor());
    }
}
