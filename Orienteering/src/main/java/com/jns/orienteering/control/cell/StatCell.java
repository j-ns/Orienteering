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

import java.time.LocalDate;

import com.jns.orienteering.control.DurationDisplay;
import com.jns.orienteering.model.persisted.StatByUser;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public class StatCell extends SelectableListCell<StatByUser> {

    private Content content;

    public StatCell(ObjectProperty<StatByUser> selectedItem) {
        super(selectedItem);
        getStyleClass().add("stat-cell");
        content = new Content();
    }

    @Override
    protected void updateItem(StatByUser item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null) {
            setGraphic(null);
        } else {
            if (getGraphic() == null) {
                setGraphic(content);
            }
            content.lblName.setText(item.getLookupName());
            content.lblDate.setText(LocalDate.ofEpochDay(item.getTimeStamp()).toString());
            content.displayDuration.setDuration(item.getDuration());
        }
    }

    private class Content extends Region {

        private static final double PADDING          = 16;
        private static final int    VERTICAL_SPACING = 8;

        private Label               lblName;
        private DurationDisplay     displayDuration;
        private Label               lblDate;

        public Content() {
            getStyleClass().add("content");
            setPadding(new Insets(PADDING));

            lblName = new Label();
            lblName.getStyleClass().add("title");

            lblDate = new Label();
            lblDate.getStyleClass().add("date");

            displayDuration = new DurationDisplay();

            getChildren().addAll(lblName, lblDate, displayDuration);
        }

        @Override
        protected void layoutChildren() {
            double width = getWidth();

            Insets insets = getInsets();
            double left = insets.getLeft();
            double right = insets.getRight();

            double prefWidthDuration = displayDuration.prefWidth(-1);
            double prefHeightPoints = displayDuration.prefHeight(-1);

            double x = left;
            double currentY = insets.getTop();

            double labelWidth = -left + width - prefWidthDuration - 16 - right;
            lblName.resize(labelWidth, lblName.prefHeight(-1));
            lblName.relocate(x, currentY);

            displayDuration.resize(prefWidthDuration, prefHeightPoints);
            displayDuration.relocate(width - prefWidthDuration - right, currentY);
            currentY += prefHeightPoints + VERTICAL_SPACING;

            lblDate.resize(lblDate.prefWidth(-1), lblDate.prefHeight(-1));
            lblDate.relocate(x, currentY);
        }

        @Override
        protected double computePrefHeight(double width) {
            return getInsets().getTop() + lblName.prefHeight(-1) + VERTICAL_SPACING + lblDate.prefHeight(-1) +
                    getInsets().getBottom();
        }
    }
}
