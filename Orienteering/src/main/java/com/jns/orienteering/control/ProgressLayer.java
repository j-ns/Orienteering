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
package com.jns.orienteering.control;

import java.util.function.Function;

import com.gluonhq.charm.glisten.animation.FadeInTransition;
import com.gluonhq.charm.glisten.animation.FadeOutTransition;
import com.gluonhq.charm.glisten.application.GlassPane;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.application.MobileApplication.MobileEvent;
import com.gluonhq.charm.glisten.control.ProgressIndicator;
import com.gluonhq.charm.glisten.layout.Layer;
import com.jns.orienteering.util.Trigger;

import javafx.animation.Animation.Status;
import javafx.animation.PauseTransition;
import javafx.animation.Transition;
import javafx.event.EventHandler;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class ProgressLayer extends Layer {

    public static final double DEFAULT_DELAY = 150;

    private GlassPane          glassPane     = MobileApplication.getInstance().getGlassPane();

    private StackPane          root;

    private ShowHideTransition showHideTransition;
    private double             delay         = DEFAULT_DELAY;
    private boolean            fadeLayer;

    public ProgressLayer() {
        this(PauseFadeInFadeOut::new);
    }

    public ProgressLayer(Function<ProgressLayer, ShowHideTransition> transitionProvider) {
        getStyleClass().add("progress-layer");
        setAutoHide(false);

        ProgressIndicator progress = new ProgressIndicator();
        progress.setStyle("-fx-color:#ff9100");

        root = new StackPane(progress);
        getChildren().add(root);

        glassPane.getLayers().add(this);

        showHideTransition = transitionProvider.apply(this);
        showHideTransition.setOnFinished(() -> super.hide());

        showingProperty().addListener((ov, b, b1) ->
        {
            if (b1) {
                showHideTransition.startShow();
            }
        });
    }

    public void setDelay(double millis) {
        delay = millis;
    }

    public void setFadeLayer(boolean value) {
        fadeLayer = value;
    }

    public void setOnBackButtonPressed(EventHandler<MobileEvent> handler) {
        addEventFilter(MobileEvent.BACK_BUTTON_PRESSED, handler);
        getStyleClass().add("cancelable");
    }

    @Override
    public void show() {
        if (!isShowing()) {
            super.show();
        }
    }

    @Override
    public void hide() {
        if (isShowing()) {
            showHideTransition.startHide();
        }
        glassPane.setBackgroundFade(0d);
    }

    @Override
    public void layoutChildren() {
        root.setVisible(isShowing());
        if (!isShowing()) {
            return;
        }

        double size = root.prefWidth(-1);
        resizeRelocate(glassPane.getWidth() / 2, glassPane.getHeight() / 2, size, size);
    }

    public abstract static class ShowHideTransition {

        protected ProgressLayer progressLayer;
        protected Trigger       onFinished;

        public ShowHideTransition(ProgressLayer progressLayer) {
            this.progressLayer = progressLayer;
            initTransitions();
        }

        public void setOnFinished(Trigger onFinished) {
            this.onFinished = onFinished;
        }

        public abstract void initTransitions();

        public abstract void startShow();

        public abstract void startHide();
    }

    public static class PauseFadeInFadeOut extends ShowHideTransition {

        private PauseTransition pauseTransition;
        protected Transition    showTransition;
        private Transition      hideTransition;

        private boolean         running;

        public PauseFadeInFadeOut(ProgressLayer progressLayer) {
            super(progressLayer);
        }

        @Override
        public void initTransitions() {
            showTransition = new FadeInTransition(progressLayer);

            pauseTransition = new PauseTransition(Duration.millis(progressLayer.delay));
            pauseTransition.setOnFinished(e ->
            {
                if (progressLayer.isShowing()) {
                    if (progressLayer.fadeLayer) {
                        progressLayer.glassPane.setBackgroundFade(GlassPane.DEFAULT_BACKGROUND_FADE_LEVEL);
                    }
                    showTransition.playFromStart();
                    running = true;
                }
            });

            hideTransition = new FadeOutTransition(progressLayer);
            hideTransition.setOnFinished(e -> onFinished.start());
        }

        @Override
        public void startShow() {
            progressLayer.setOpacity(0);
            pauseTransition.playFromStart();
        }

        @Override
        public void startHide() {
            if (running) {
                showTransition.jumpTo("end");
                hideTransition.playFromStart();
            } else {
                onFinished.start();
            }
            running = false;
        }
    }

    public static class PauseFadeInHide extends PauseFadeInFadeOut {

        public PauseFadeInHide(ProgressLayer progressIcon) {
            super(progressIcon);
        }

        @Override
        public void startHide() {
            onFinished.start();
        }

    }

    public static class ShowFadeOut extends ShowHideTransition {

        private Transition hideTransition;

        public ShowFadeOut(ProgressLayer progressIcon) {
            super(progressIcon);
        }

        @Override
        public void initTransitions() {
            hideTransition = new FadeOutTransition(progressLayer);
            hideTransition.setOnFinished(e -> onFinished.start());
        }

        @Override
        public void startShow() {
            // no op
        }

        @Override
        public void startHide() {
            if (hideTransition.getStatus() != Status.RUNNING) {
                hideTransition.playFromStart();
            }
        }
    }
}