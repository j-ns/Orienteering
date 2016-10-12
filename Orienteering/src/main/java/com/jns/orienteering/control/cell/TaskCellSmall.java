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

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.repo.image.ImageHandler;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

public class TaskCellSmall extends SelectableListCell<Task> {

    private static final PseudoClass PSEUDO_CLASS_SLIDING = PseudoClass.getPseudoClass("sliding");

    private static final boolean SHOW_PLACE_HOLDER = true;

    private Content              content;
    private SlidingListTile      slidingTile;
    private Predicate<Task>      partOfMissionCheck;
    private Task                 task;

    public TaskCellSmall(ObjectProperty<Task> selectedItem) {
        this(selectedItem, null, null, null, null);
    }

    public TaskCellSmall(ObjectProperty<Task> selectedItem, Consumer<Task> consumerLeft, BooleanProperty sliding) {
        this(selectedItem, null, consumerLeft, null, sliding);
    }

    public TaskCellSmall(ObjectProperty<Task> selectedItem, Predicate<Task> partOfMissionCheck, Consumer<Task> consumerLeft,
                         BiConsumer<Task, Boolean> consumerRight, BooleanProperty sliding) {
        super(selectedItem);
        getStyleClass().add("task-cell-small");

        content = new Content();

        this.partOfMissionCheck = partOfMissionCheck;
        if (partOfMissionCheck != null) {
            content.showCheckNode(true);
        }

        String textLeft = consumerLeft == null ? null : MaterialDesignIcon.DELETE.text;
        String textRight = consumerRight == null ? null : MaterialDesignIcon.PIN_DROP.text;

        if (textLeft != null || textRight != null) {
            initSlidingTile(consumerLeft, consumerRight, textLeft, textRight);

            if (sliding != null) {
                slidingTile.slidingProperty().addListener((ov, b, b1) -> {
                    sliding.set(b1);
                    pseudoClassStateChanged(PSEUDO_CLASS_SLIDING, b1);
                });
            }
        }
    }

    private void initSlidingTile(Consumer<Task> consumerLeft, BiConsumer<Task, Boolean> consumerRight, String textLeft, String textRight) {
        slidingTile = new SlidingListTile(content, true, textLeft, textRight);

        slidingTile.swipedLeftProperty().addListener((obs, b, b1) ->
        {
            if (b1 && consumerRight != null) {
                boolean isPartOfMission = content.lblCheck.isVisible();
                content.lblCheck.setVisible(!isPartOfMission);
                consumerRight.accept(task, !isPartOfMission);
            }
            slidingTile.resetTilePosition();
        });
        slidingTile.setOnSwipedRight(consumerLeft, this::getTask);
    }

    @Override
    protected void updateItem(Task task, boolean empty) {
        super.updateItem(task, empty);
        this.task = task;

        if (task == null || empty) {
            setGraphic(null);
        } else {
            if (getGraphic() == null) {
                setGraphic(slidingTile != null ? slidingTile : content);
            }
            content.lblName.setText(task.getTaskName());
            content.lblDescription.setText(task.getDescription());
            content.lblPoints.setText(task.getPointsString());

            ImageHandler.loadInto(content.imgView, task.getImageUrl(), SHOW_PLACE_HOLDER);

            if (partOfMissionCheck != null) {
                content.lblCheck.setVisible(partOfMissionCheck.test(task));
            }
        }
    }

    private Task getTask() {
        return task;
    }

    private class Content extends Region {

        private static final double IMAGE_WIDTH       = 112;
        private static final double IMAGE_HEIGHT      = 112;
        private static final double PADDING_IMAGE_TOP = 16;

        private static final double PADDING_TOP       = 24;
        private static final double PADDING_RIGHT     = 16;
        private static final double PADDING_BOTTOM    = 16;
        private static final double PADDING_LEFT      = 16;

        private Label               lblName;
        private Label               lblPoints;
        private Label               lblDescription;
        private Label               lblCheck;
        private ImageView           imgView;

        public Content() {
            getStyleClass().add("content");

            setMinHeight(Region.USE_PREF_SIZE);
            setMaxHeight(Region.USE_PREF_SIZE);
            setPadding(new Insets(PADDING_TOP, PADDING_RIGHT, PADDING_BOTTOM, PADDING_LEFT));

            lblCheck = new Label(MaterialDesignIcon.CHECK.text);
            lblCheck.getStyleClass().addAll("icon-text", "check");
            lblCheck.setVisible(true);

            lblName = new Label();
            lblName.getStyleClass().add("title");

            lblName.setContentDisplay(ContentDisplay.RIGHT);

            lblPoints = new Label();
            lblPoints.getStyleClass().add("points");

            lblDescription = new Label();
            lblDescription.getStyleClass().add("description");

            imgView = new ImageView();
            imgView.setFitWidth(IMAGE_WIDTH);
            imgView.setFitHeight(IMAGE_HEIGHT);

            getChildren().addAll(lblName, lblPoints, lblDescription, imgView);
        }

        private void showCheckNode(boolean value) {
            if (value) {
                lblName.setGraphic(lblCheck);
            } else {
                lblName.setGraphic(null);
            }
        }

        @Override
        protected void layoutChildren() {
            Insets insets = getInsets();
            double left = insets.getLeft();
            double right = insets.getRight();

            double width = getWidth();
            double widthLabels = -left + width - right - IMAGE_WIDTH - right;
            double currentY = insets.getTop();

            double prefHeightName = lblName.prefHeight(-1);
            double prefHeightPoints = lblPoints.prefHeight(-1);
            double prefHeightDescription = lblDescription.prefHeight(-1);

            lblName.resize(widthLabels, prefHeightName);
            lblName.relocate(left, currentY);
            currentY += prefHeightName;

            lblPoints.resize(widthLabels, prefHeightPoints);
            lblPoints.relocate(left, currentY);
            currentY += prefHeightPoints;

            lblDescription.resize(widthLabels, prefHeightDescription);
            lblDescription.relocate(left, currentY);

            imgView.resize(IMAGE_WIDTH, IMAGE_HEIGHT);
            imgView.relocate(width - IMAGE_WIDTH - right, PADDING_IMAGE_TOP);
        }

        @Override
        protected double computePrefHeight(double width) {
            return PADDING_IMAGE_TOP + IMAGE_HEIGHT + getInsets().getBottom();
        }

        @Override
        protected double computePrefWidth(double height) {
            return -getInsets().getLeft() + getListView().getWidth() - getInsets().getRight();
        }
    }

}
