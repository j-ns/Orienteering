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

import static com.jns.orienteering.locale.Localization.localize;

import java.util.function.Consumer;

import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.jns.orienteering.model.dynamic.LocalCityCache;
import com.jns.orienteering.model.persisted.Mission;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public class MissionCell extends SelectableListCell<Mission> {

    private Content         content;
    private SlidingListTile slidingTile;

    private Mission         mission;

    public MissionCell(ObjectProperty<Mission> selectedItem, Consumer<Mission> consumerLeft, Consumer<Mission> consumerRight,
                       BooleanProperty sliding) {
        super(selectedItem);
        getStyleClass().add("mission-cell");

        content = new Content();

        slidingTile = new SlidingListTile(content, true, MaterialDesignIcon.DELETE.text, MaterialDesignIcon.EXPLORE.text);
        slidingTile.setOnSwipedLeft(consumerRight, this::getMission);
        slidingTile.setOnSwipedRight(consumerLeft, this::getMission);
        slidingTile.slidingProperty().addListener((ov, b, b1) -> sliding.set(b1));
    }

    @Override
    protected double computePrefWidth(double height) {
        return -getInsets().getLeft() + getListView().getWidth() - getInsets().getRight();
    }

    @Override
    protected void updateItem(Mission mission, boolean empty) {
        super.updateItem(mission, empty);
        this.mission = mission;

        if (mission == null || empty) {
            setGraphic(null);
        } else {
            if (getGraphic() == null) {
                setGraphic(slidingTile);
            }
            content.lblName.setText(mission.getMissionName());
            content.lblDistance.setText(mission.getDistanceText() + " " + localize("unit.miles"));
            content.lblCity.setText(LocalCityCache.INSTANCE.get(mission.getCityId()).getCityName());
            content.lblPoints.setText(Integer.toString(mission.getMaxPoints()) + " " + localize("label.points"));
        }
    }

    private Mission getMission() {
        return mission;
    }

    private class Content extends Region {

        private static final double PADDING          = 16;
        private static final int    VERTICAL_SPACING = 4;

        private Label               lblName;
        private Label               lblDistance;
        private Label               lblCity;
        private Label               lblPoints;

        public Content() {
            getStyleClass().add("content");
            setPadding(new Insets(PADDING));

            lblName = new Label();
            lblName.getStyleClass().add("title");
            lblName.setPrefHeight(24);

            lblDistance = new Label();
            lblDistance.getStyleClass().add("distance");

            lblCity = new Label();
            lblCity.getStyleClass().add("city");

            lblPoints = new Label();
            lblPoints.getStyleClass().add("points");

            getChildren().addAll(lblName, lblDistance, lblCity, lblPoints);
        }

        @Override
        protected void layoutChildren() {
            double width = getWidth();

            double prefHeightName = lblName.prefHeight(-1);
            double prefHeightCity = lblCity.prefHeight(-1);
            Insets insets = getInsets();
            double left = insets.getLeft();
            double right = insets.getRight();

            double labelWidth = -left + width - lblDistance.prefWidth(-1) - 16 - right;

            double x = left;
            double currentY = insets.getTop();

            lblName.resize(labelWidth, prefHeightName);
            lblName.relocate(x, currentY);

            lblDistance.resize(lblDistance.prefWidth(-1), prefHeightName);
            lblDistance.relocate(width - lblDistance.prefWidth(-1) - right, currentY);
            currentY += prefHeightName + VERTICAL_SPACING;

            lblCity.resize(labelWidth, prefHeightCity);
            lblCity.relocate(x, currentY);
            currentY += prefHeightCity + VERTICAL_SPACING;

            lblPoints.resize(labelWidth, lblPoints.prefHeight(-1));
            lblPoints.relocate(x, currentY);
        }

        @Override
        protected double computePrefHeight(double width) {
            return getInsets().getTop() + lblName.prefHeight(-1) + lblCity.prefHeight(-1) + lblPoints.prefHeight(-1) +
                    +VERTICAL_SPACING * 2 + getInsets().getBottom();
        }
    }

}
