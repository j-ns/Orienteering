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
package com.jns.orienteering.control.cell;

import java.util.function.Consumer;

import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.jns.orienteering.model.persisted.City;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public class CityCell extends SelectableListCell<City> {

    private Content         content;
    private SlidingListTile slidingTile;
    private City            city;

    public CityCell(ObjectProperty<City> selectedItem, Consumer<City> consumerLeft, Consumer<City> consumerRight, BooleanProperty sliding) {
        super(selectedItem);
        getStyleClass().add("city-cell");
        setPrefWidth(100);

        content = new Content();

        String textLeft = consumerLeft == null ? null : MaterialDesignIcon.DELETE.text;
        String textRight = consumerRight == null ? null : MaterialDesignIcon.EXPLORE.text;

        slidingTile = new SlidingListTile(content, true, textLeft, textRight);
        slidingTile.setOnSwipedLeft(consumerRight, this::getCity);
        slidingTile.setOnSwipedRight(consumerLeft, this::getCity);
        slidingTile.slidingProperty().addListener((ov, b, b1) -> sliding.set(b1));
    }

    @Override
    protected void updateItem(City city, boolean empty) {
        super.updateItem(city, empty);
        this.city = city;

        if (city == null || empty) {
            setGraphic(null);
            setText(null);
        } else {
            if (getGraphic() == null) {
                setGraphic(slidingTile);
            }
            content.lblName.setText(city.getCityName());
        }
    }

    private City getCity() {
        return city;
    }

    private class Content extends Region {
        private static final double PADDING = 16;

        private Label               lblName;

        private Content() {
            getStyleClass().add("content");
            setPadding(new Insets(PADDING));
            lblName = new Label();
            getChildren().add(lblName);
        }

        @Override
        protected void layoutChildren() {
            double left = getInsets().getLeft();
            content.lblName.resize(-left + getWidth() - getInsets().getRight(), getHeight());
            content.lblName.relocate(left, 0);
        }

    }

}
