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

import com.jns.orienteering.control.DurationDisplay;
import com.jns.orienteering.model.Ranking;
import com.jns.orienteering.util.Icon;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class RankingCell extends ListCell<Ranking> {

    private Content content;

    public RankingCell() {
        getStyleClass().add("ranking-cell");
        content = new Content();
    }

    @Override
    protected void updateItem(Ranking ranking, boolean empty) {
        super.updateItem(ranking, empty);

        if (ranking == null || empty) {
            setGraphic(null);
        } else {
            if (getGraphic() == null) {
                setGraphic(content);
            }
            content.lblPlace.setText(ranking.getPlaceText());
            content.lblName.setText(ranking.getMissionStat().getUserId());
            content.displayDuration.setDuration(ranking.getMissionStat().getDuration());

            if (ranking.getPlace() > 1) {
                content.lblTimeDifference.setDuration(ranking.getTimeDifference());
                content.lblTimeDifference.setVisible(true);
            } else {
                content.lblTimeDifference.setVisible(false);
            }
            content.lblDistance.setText(ranking.getMissionStat().getDistanceText());
        }
    }

    private class Content extends Pane {

        private static final double PADDING           = 16;
        private static final double PADDING_TEXT_LEFT = 72;
        private static final int    V_SPACING         = 4;

        private static final String ICON_SIZE         = "16";

        private Region              circlePlace;
        private Label               lblPlace;
        private Label               lblName;
        private DurationDisplay     displayDuration;
        private Label               lblDistance;
        private DurationDisplay     lblTimeDifference;

        public Content() {
            getStyleClass().add("content");
            setPadding(new Insets(PADDING));

            circlePlace = new Region();
            circlePlace.setPrefWidth(36);
            circlePlace.setPrefHeight(36);
            circlePlace.setMaxHeight(Region.USE_PREF_SIZE);
            circlePlace.getStyleClass().add("circle");

            lblPlace = new Label();
            lblPlace.getStyleClass().add("place");

            lblName = new Label();
            lblName.getStyleClass().add("title");

            displayDuration = new DurationDisplay();
            displayDuration.setGraphic(Icon.TIMELAPSE.icon(ICON_SIZE));
            displayDuration.setGraphicTextGap(4);

            lblDistance = new Label();
            lblDistance.setGraphic(Icon.DISTANCE.icon(ICON_SIZE));
            lblDistance.getStyleClass().add("distance");

            lblTimeDifference = new DurationDisplay();
            lblTimeDifference.getStyleClass().add("timeDifference");

            getChildren().addAll(circlePlace, lblPlace, lblName, displayDuration, lblDistance, lblTimeDifference);
        }

        @Override
        protected void layoutChildren() {
            double height = getHeight();
            double width = getWidth();

            double left = getInsets().getLeft();
            double right = getInsets().getRight();

            double prefHeightName = lblName.prefHeight(-1);
            double prefHeightDuration = displayDuration.prefHeight(-1);
            double prefWidthTimeDifference = lblTimeDifference.prefWidth(-1);

            double x = PADDING_TEXT_LEFT;
            double currentY = getInsets().getTop();

            layoutInArea(circlePlace, left, 0, 52 - left, height, 0, HPos.CENTER, VPos.CENTER);
            layoutInArea(lblPlace, left, 0, 52 - left, height, 0, HPos.CENTER, VPos.CENTER);

            lblName.resize(-PADDING_TEXT_LEFT + width - prefWidthTimeDifference - 16 - right, prefHeightName);
            lblName.relocate(x, currentY);

            lblTimeDifference.resize(prefWidthTimeDifference, prefHeightName);
            lblTimeDifference.relocate(width - prefWidthTimeDifference - right, currentY);
            currentY += prefHeightName + V_SPACING;

            displayDuration.resize(displayDuration.prefWidth(-1), prefHeightDuration);
            displayDuration.relocate(x, currentY);
            currentY += prefHeightDuration + V_SPACING;

            lblDistance.resize(lblDistance.prefWidth(-1), prefHeightName);
            lblDistance.relocate(x, currentY);
        }

        @Override
        protected double computePrefHeight(double width) {
            return getInsets().getTop() + lblName.prefHeight(-1) + displayDuration.prefHeight(-1) + lblTimeDifference.prefHeight(-1) + V_SPACING * 2 +
                    getInsets().getBottom();
        }

    }
}
