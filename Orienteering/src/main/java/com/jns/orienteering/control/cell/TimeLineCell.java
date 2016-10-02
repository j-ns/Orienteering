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
package com.jns.orienteering.control.cell;

import static com.jns.orienteering.util.DateTimeFormatters.formatTime;

import com.jns.orienteering.control.DurationDisplay;
import com.jns.orienteering.locale.Localization;
import com.jns.orienteering.model.persisted.TaskStat;
import com.jns.orienteering.util.Icon;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;

public class TimeLineCell extends ListCell<TaskStat> {

    private Content content;

    public TimeLineCell() {
        getStyleClass().add("time-line-cell");
        content = new Content();
    }

    @Override
    protected void updateItem(TaskStat stat, boolean empty) {
        super.updateItem(stat, empty);

        if (stat == null || empty) {
            setGraphic(null);
        } else {
            if (getGraphic() == null) {
                setGraphic(content);
            }
            content.lblName.setText(stat.getTaskName());
            content.lblDuration.setDuration(stat.getDuration());

            content.lblStart.setText(formatTime(stat.getStart()));
            content.lblEnd.setText(formatTime(stat.getEnd()));
            content.lblDistance.setText(stat.getDistanceText() + " " + Localization.localize("unit.miles"));

            content.missionFinishedState.set(stat.isMissionFinished());
            content.pseudoClassStateChanged(Content.PSEUDO_CLASS_TASK_SKIPPED, !stat.isCompleted());
        }
    }

    private static class Content extends Pane {

        private static final PseudoClass PSEUDO_CLASS_MISSION_FINISHED = PseudoClass.getPseudoClass("mission-finished");
        private static final PseudoClass PSEUDO_CLASS_TASK_SKIPPED     = PseudoClass.getPseudoClass("task-skipped");

        private static final double      PADDING_TOP                   = 16;
        private static final double      PADDING_BOTTOM                = 16;
        private static final double      PADDING_ICON_LEFT             = 32;
        private static final double      PADDING_ITEM_LEFT             = 80;
        private static final double      PADDING_RIGHT                 = 20;
        private static final int         SPACING                       = 4;

        private static final String      ICON_SIZE                     = "16";
        private static final double      CIRCLE_WIDTH                  = 24;

        private Line                     line;
        private Region                   circle;

        private Label                    lblName;
        private DurationDisplay          lblDuration;
        private Label                    lblStart;
        private Label                    lblEnd;
        private Label                    lblDistance;

        public Content() {
            getStyleClass().addAll("time-line-content");

            circle = new Region();
            circle.getStyleClass().add("circle");
            circle.setPrefHeight(CIRCLE_WIDTH);
            circle.setPrefWidth(CIRCLE_WIDTH);

            double lineX = PADDING_ICON_LEFT + CIRCLE_WIDTH / 2;
            line = new Line(lineX, 0, lineX, 112);
            line.getStyleClass().add("line");

            lblName = new Label();
            lblName.getStyleClass().add("title");
            lblName.setPrefHeight(24);

            lblDuration = new DurationDisplay();

            lblStart = new Label();
            lblStart.setGraphic(Icon.CLOCK.icon(ICON_SIZE));

            lblEnd = new Label();
            lblEnd.setGraphic(Icon.MAP_MARKER_CIRCLE.icon(ICON_SIZE));

            lblDistance = new Label();
            lblDistance.setGraphic(Icon.DISTANCE.icon(ICON_SIZE));

            getChildren().addAll(line, circle, lblName, lblDuration, lblStart, lblEnd, lblDistance);
        }

        private BooleanProperty missionFinishedState = new BooleanPropertyBase() {

            @Override
            protected void invalidated() {
                pseudoClassStateChanged(PSEUDO_CLASS_MISSION_FINISHED, get());

                if (get()) {
                    line.setEndY(16);
                    circle.setPrefHeight(32);
                    circle.setPrefWidth(32);
                    lblName.setPrefHeight(32);
                } else {
                    line.setEndY(112);
                    circle.setPrefHeight(CIRCLE_WIDTH);
                    circle.setPrefWidth(CIRCLE_WIDTH);
                    lblName.setPrefHeight(Region.USE_COMPUTED_SIZE);
                }
            }

            @Override
            public Object getBean() {
                return this;
            }

            @Override
            public String getName() {
                return "mission-finished";
            }
        };

        @Override
        protected void layoutChildren() {
            double width = getWidth();

            double prefHeightCircle = circle.prefHeight(-1);
            double prefHeightName = lblName.prefHeight(-1);
            double prefHeightStart = lblStart.prefHeight(-1);
            double prefHeightEnd = lblEnd.prefHeight(-1);
            double prefWidthDuration = lblDuration.prefWidth(-1);

            double labelWidth = -PADDING_ITEM_LEFT + width - prefWidthDuration - PADDING_RIGHT;

            double circleX = PADDING_ICON_LEFT - (missionFinishedState.get() ? SPACING : 0);
            double x = PADDING_ITEM_LEFT;
            double currentY = PADDING_TOP;

            circle.resize(prefHeightCircle, prefHeightCircle);
            circle.relocate(circleX, currentY);

            lblName.resize(labelWidth - 16, prefHeightName);
            lblName.relocate(x, currentY);

            lblDuration.resize(prefWidthDuration, prefHeightName);
            lblDuration.relocate(width - prefWidthDuration - PADDING_RIGHT, currentY);
            currentY += prefHeightName + SPACING;

            lblStart.resize(labelWidth, prefHeightStart);
            lblStart.relocate(x, currentY);
            currentY += prefHeightStart + SPACING;

            lblEnd.resize(labelWidth, prefHeightEnd);
            lblEnd.relocate(x, currentY);
            currentY += prefHeightEnd + SPACING;

            lblDistance.resize(labelWidth, lblDistance.prefHeight(-1));
            lblDistance.relocate(x, currentY);
        }

        @Override
        protected double computePrefHeight(double width) {
            return PADDING_TOP + lblName.prefHeight(-1) + lblStart.prefHeight(-1) + lblEnd.prefHeight(-1) + lblDistance.prefHeight(-1) +
                    PADDING_BOTTOM;
        }
    }

}
