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

import static com.jns.orienteering.locale.Localization.localize;

import javax.inject.Inject;

import com.airhacks.afterburner.injection.Injector;
import com.gluonhq.charm.glisten.control.ProgressBar;
import com.gluonhq.charm.glisten.mvc.SplashView;
import com.jns.orienteering.common.BaseService;

import javafx.animation.Animation.Status;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Shows a splashscreen at the start of the application until {@link BaseService} is initialized
 */
public class StartView extends SplashView {

    @Inject
    private BaseService service;

    public StartView() {
        Injector.registerExistingAndInject(this);
        initContent();
        setOnShown(e -> onShown());
    }

    private void initContent() {
        ImageView image = new ImageView("/images/compass.png");

        Region spacer = new Region();
        spacer.setPrefHeight(60);

        Label lblTitle = new Label(localize("view.splash.title"));
        lblTitle.setId("startTitle");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setStyle("-fx-color: #89C63B");
        progressBar.setMaxWidth(200);

        VBox boxContent = new VBox(20, spacer, image, lblTitle, progressBar);
        boxContent.setAlignment(Pos.TOP_CENTER);

        setCenter(new StackPane(boxContent));
    }

    protected void onShown() {
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e ->
        {
            if (service.isInitialized()) {
                hideSplashView();
            } else {
                service.initializedProperty().addListener((obsValue, b, b1) ->
                {
                    if (b1) {
                        if (pause.getStatus() != Status.RUNNING) {
                            hideSplashView();
                        }
                    }
                });
            }
        });
        pause.play();

    }

}