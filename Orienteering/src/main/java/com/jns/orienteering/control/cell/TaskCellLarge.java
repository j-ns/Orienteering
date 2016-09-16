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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jns.orienteering.common.ImageHandler;
import com.jns.orienteering.model.persisted.Task;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class TaskCellLarge extends ListCell<Task> {

    private static final Logger  LOGGER          = LoggerFactory.getLogger(TaskCellLarge.class);

    private static final boolean NO_PLACE_HOLDER = false;

    private static final double  BORDER_INSET    = 8;

    private Content              content;

    public TaskCellLarge() {
        getStyleClass().addAll("task-cell-large", "card-cell", "drop-shadow");
        content = new Content();
    }

    @Override
    protected void updateItem(Task task, boolean empty) {
        super.updateItem(task, empty);

        if (task == null || empty) {
            setGraphic(null);
        } else {
            if (getGraphic() == null) {
                setGraphic(content);
            }
            ImageHandler.loadInto(content.imgView, task.getImageUrl(), NO_PLACE_HOLDER);
            content.lblName.setText(task.getTaskName());
            content.lblPoints.setText(task.getPointsString());
            content.lblDescription.setText(task.getDescription());
        }
    }

    @Override
    protected void layoutChildren() {
        double tileContentWidth = -BORDER_INSET + getWidth() - BORDER_INSET;
        double height = getHeight() - BORDER_INSET;

        content.resize(tileContentWidth, height);
        content.relocate(BORDER_INSET, 0);
    }

    @Override
    protected double computePrefWidth(double height) {
        return getListView().getWidth();
    }

    @Override
    protected double computePrefHeight(double width) {
        return content.prefHeight(width) + BORDER_INSET;
    }

    private class Content extends Pane {

        private static final double PADDING_TOP    = 24;
        private static final double PADDING_RIGHT  = 16;
        private static final double PADDING_BOTTOM = 24;
        private static final double PADDING_LEFT   = 16;

        private static final double ASPECT_RATIO   = 16d / 9d;

        private ImageView           imgView;
        private Label               lblName;
        private Label               lblDescription;
        private Label               lblPoints;

        private double              prefHeightName;
        private double              prefHeightDescription;
        private double              prefHeightPoints;
        private double              prefHeightImageView;

        private Content() {
            getStyleClass().add("content");

            imgView = new ImageView();

            lblName = new Label();
            lblName.getStyleClass().add("title");
            lblName.setWrapText(true);

            lblPoints = new Label();
            lblPoints.getStyleClass().add("points");

            lblDescription = new Label();
            lblDescription.getStyleClass().add("description");
            lblDescription.setWrapText(true);

            getChildren().addAll(imgView, lblName, lblPoints, lblDescription);
        }

        @Override
        protected void layoutChildren() {
            double width = getWidth();
            double widthLabels = -PADDING_LEFT + width - PADDING_RIGHT;

            double x = PADDING_LEFT;
            double currentY = 0;

            imgView.resize(width, prefHeightImageView);
            imgView.relocate(0, 0);
            imgView.setFitWidth(width);
            imgView.setFitHeight(prefHeightImageView);
            currentY = PADDING_TOP + prefHeightImageView;

            lblName.resize(widthLabels, prefHeightName);
            lblName.relocate(x, currentY);
            currentY += prefHeightName;

            lblPoints.resize(widthLabels, prefHeightPoints);
            lblPoints.relocate(x, currentY);
            currentY += prefHeightPoints;

            lblDescription.resize(widthLabels, prefHeightDescription);
            lblDescription.relocate(x, currentY);
        }

        @Override
        protected double computePrefHeight(double width) {
            LOGGER.debug("computePrefHeight");

            prefHeightImageView = imgView.getImage() == null ? 0 : getCellWidth() / ASPECT_RATIO;
            prefHeightName = calculatePrefHeightForLabel(lblName);
            prefHeightPoints = lblPoints.prefHeight(-1);
            prefHeightDescription = calculatePrefHeightForLabel(lblDescription);

            return PADDING_TOP + prefHeightImageView + prefHeightName + prefHeightPoints + prefHeightDescription + PADDING_BOTTOM;
        }

        private double calculatePrefHeightForLabel(Label label) {
            double width = PADDING_LEFT + getCellWidth() - PADDING_RIGHT;
            double prefWidthLabel = label.prefWidth(-1);

            int rowCount = (int) (prefWidthLabel / width);
            if (prefWidthLabel % width > 0) {
                rowCount++;
            }
            return label.prefHeight(-1) * rowCount;
        }

        private double getCellWidth() {
            return -BORDER_INSET + getListView().getWidth() - BORDER_INSET;
        }
    }

}
