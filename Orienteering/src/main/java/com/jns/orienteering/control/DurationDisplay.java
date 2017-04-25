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

import javafx.animation.AnimationTimer;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class DurationDisplay extends Pane {

    private Label          lblMinutes;
    private Label          lblSeparator;
    private Label          lblSeconds;
    private Label          lblUnit;
    private Node           graphic;

    private int            minutes;
    private int            seconds;
    private int            graphicTextGap;

    private AnimationTimer timer;

    public DurationDisplay() {
        setMaxWidth(Region.USE_PREF_SIZE);
        getStyleClass().add("duration-display");

        lblMinutes = new Label("0");
        lblSeparator = new Label(":");
        lblSeconds = new Label("00");
        lblUnit = new Label(" min");

        getChildren().addAll(lblMinutes, lblSeparator, lblSeconds, lblUnit);

        timer = createTimer();
    }

    private AnimationTimer createTimer() {
        return new AnimationTimer() {

            private long lastTime;

            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                } else if (now > lastTime + 1_000_000_000) {
                    lastTime = now;
                    advanceDuration();
                }
            }
        };
    }

    public void setGraphic(Node graphic) {
        this.graphic = graphic;
        getChildren().add(graphic);
    }

    public void setGraphicTextGap(int gap) {
        graphicTextGap = gap;
    }

    /**
     * @param duration
     *            in seconds
     */
    public void startAt(int duration) {
        setDuration(duration);
        startTimer();
    }

    public void setDuration(int duration) {
        minutes = duration / 60;
        seconds = duration % 60;
        updateDisplay();
    }

    public void startTimer() {
        timer.start();
    }

    private void advanceDuration() {
        if (seconds < 59) {
            seconds++;
        } else {
            seconds = 0;
            minutes++;
        }
        updateDisplay();
    }

    private void updateDisplay() {
        lblSeconds.setText(getTimeText(seconds));
        lblMinutes.setText(Integer.toString(minutes));
    }

    private String getTimeText(int number) {
        String text = Integer.toString(number);
        if (number < 10) {
            text = "0" + text;
        }
        return text;
    }

    public void stop() {
        timer.stop();
    }

    @Override
    protected void layoutChildren() {
        double height = getHeight();

        double x = 0;
        double y = 0;

        if (graphic != null) {
            double prefGraphicWidth = graphic.prefWidth(-1);

            layoutInArea(graphic, x, y, prefGraphicWidth, height, 0, HPos.CENTER, VPos.CENTER);
            x = prefGraphicWidth + graphicTextGap;
        }

        double prefWidthMinutes = lblMinutes.prefWidth(-1);
        lblMinutes.resize(prefWidthMinutes, height);
        lblMinutes.relocate(x, y);
        x += prefWidthMinutes;

        double prefWidthSeparator = lblSeparator.prefWidth(-1);
        lblSeparator.resize(prefWidthSeparator, height);
        lblSeparator.relocate(x, y);
        x += prefWidthSeparator;

        double prefWidthSeconds = lblSeconds.prefWidth(-1);
        lblSeconds.resize(prefWidthSeconds, height);
        lblSeconds.relocate(x, y);
        x += prefWidthSeconds;

        lblUnit.resize(lblUnit.prefWidth(-1), height);
        lblUnit.relocate(x, y);
    }

    @Override
    protected double computePrefWidth(double height) {
        double graphicWidth = graphic == null ? 0 : graphic.prefWidth(-1) + graphicTextGap;
        return getInsets().getLeft() + graphicWidth + lblMinutes.prefWidth(-1) + lblSeparator.prefWidth(-1) + lblSeconds.prefWidth(-1) + lblUnit
                                                                                                                                                .prefWidth(-1) +
                getInsets().getRight();
    }

    @Override
    protected double computePrefHeight(double width) {
        return getInsets().getTop() + lblMinutes.prefHeight(-1) + getInsets().getBottom();
    }
}
