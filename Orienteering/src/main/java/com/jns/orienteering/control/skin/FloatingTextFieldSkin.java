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
package com.jns.orienteering.control.skin;

import java.util.function.Consumer;

import com.jns.orienteering.control.FloatingTextField;

import javafx.animation.Transition;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

public class FloatingTextFieldSkin extends SkinBase<FloatingTextField> {

    protected TextField    textField;
    protected Label        lblHint;
    protected HintPosition hintPosition;

    private final MoveHint MOVE_HINT_UP;
    private final MoveHint MOVE_HINT_DOWN;
    private MoveHint       moveHint;

    public FloatingTextFieldSkin(FloatingTextField control) {
        super(control);
        control.getStyleClass().add("floating-text-field");

        textField = control.getTextField();
        getChildren().add(textField);

        MOVE_HINT_UP = new MoveHintUp();
        MOVE_HINT_DOWN = new MoveHintDown();
        initHint();

        textField.focusedProperty().addListener((ov, b, b1) -> onFocusChanged(b1));

        control.emptyProperty().addListener((ov, b, b1) -> onTextEmptyChanged());
        control.graphicProperty().addListener((ov, t, t1) -> updateGraphic(t, t1));

        updateGraphic(null, control.getGraphic());
    }

    private void initHint() {
        lblHint = new Label();
        lblHint.getStyleClass().add("hint");
        lblHint.textProperty().bind(getSkinnable().hintProperty());
        lblHint.setMouseTransparent(true);
        getChildren().add(lblHint);

        hintPosition = new HintPosition();
        moveHint = MOVE_HINT_UP;
        moveHint.position();
    }

    protected void onTextEmptyChanged() {
        moveHint.position();
    }

    protected void onFocusChanged(boolean focused) {
        moveHint.animate();
        lblHint.pseudoClassStateChanged(FloatingTextField.ACTIVE_PSEUDO_CLASS, focused);
        pseudoClassStateChanged(FloatingTextField.FOCUSED_PSEUDO_CLASS, focused);
    }

    private void setMoveHint(MoveHint moveHint) {
        this.moveHint = moveHint;
    }

    private void updateGraphic(Node oldGraphic, Node newGraphic) {
        if (oldGraphic != null) {
            getChildren().remove(oldGraphic);
        }
        if (newGraphic != null) {
            getChildren().add(newGraphic);
        }
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        double textFieldHeight = textField.prefHeight(-1);

        textField.resize(w - x, textFieldHeight);
        textField.relocate(x, y + h - textFieldHeight);

        lblHint.resize(lblHint.prefWidth(-1), lblHint.prefHeight(-1));
        lblHint.relocate(x, y + h - lblHint.prefHeight(-1));

        Node graphic = getSkinnable().getGraphic();
        if (graphic != null) {
            double graphicWidth = graphic.prefWidth(-1);

            graphic.resize(graphicWidth, textFieldHeight);
            graphic.relocate(x + w - graphicWidth, y + h - textFieldHeight);
        }
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        double tfWidth = textField.prefWidth(-1);
        double graphicWidth = !containsGraphic() ? 0 : getSkinnable().getGraphic().prefWidth(-1);
        return leftInset + tfWidth + graphicWidth + rightInset;
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return topInset + textField.prefHeight(-1) + lblHint.prefHeight(-1) + bottomInset;
    }

    private boolean containsGraphic() {
        return getSkinnable().getGraphic() != null;
    }

    private class HintPosition extends Transition {

        private static final int    DEFAULT_ANIMATION_DURATION = 200;

        private static final double DISTANCE_Y                 = 20;
        private static final double TOP_Y                      = -DISTANCE_Y;
        private static final double DEFAULT_SCALE              = 1.0;
        private static final double SHRINK_SCALE               = 0.8;

        private final Scale         hintScale                  = new Scale(1, 1);

        private Direction           direction;

        private HintPosition() {
            lblHint.getTransforms().add(hintScale);

            setCycleDuration(Duration.millis(DEFAULT_ANIMATION_DURATION));
        }

        void set(Direction direction) {
            this.direction = direction;

            if (direction == Direction.UP) {
                move(-DISTANCE_Y, SHRINK_SCALE);
            } else {
                move(0, DEFAULT_SCALE);
            }
        }

        void animate(Direction direction) {
            this.direction = direction;
            play();
        }

        @Override
        protected void interpolate(double frac) {
            double translateY;
            double scaleXY;

            if (direction == Direction.UP) {
                translateY = frac * -DISTANCE_Y;
                scaleXY = DEFAULT_SCALE - frac * .2;
            } else {
                translateY = TOP_Y + frac * DISTANCE_Y;
                scaleXY = SHRINK_SCALE + frac * .2;
            }

            move(translateY, scaleXY);
        }

        private void move(double translateY, double scaleXY) {
            lblHint.setTranslateY(translateY);
            hintScale.setX(scaleXY);
            hintScale.setY(scaleXY);
        }
    }

    private abstract class MoveHint {

        private final Direction DIRECTION;

        private MoveHint(Direction direction) {
            DIRECTION = direction;
        }

        private void position() {
            execute(h -> h.set(DIRECTION));
        }

        protected void animate() {
            execute(h -> h.animate(DIRECTION));
        }

        abstract void execute(Consumer<HintPosition> action);
    }

    private class MoveHintUp extends MoveHint {

        private MoveHintUp() {
            super(Direction.UP);
        }

        @Override
        void execute(Consumer<HintPosition> action) {
            if (!getSkinnable().isAnimationEnabled()) {
                return;
            }

            if (textField.isFocused() || !getSkinnable().isEmpty()) {
                action.accept(hintPosition);
                setMoveHint(MOVE_HINT_DOWN);
            }
        }
    }

    private class MoveHintDown extends MoveHint {
        private MoveHintDown() {
            super(Direction.DOWN);
        }

        @Override
        void execute(Consumer<HintPosition> action) {
            if (!getSkinnable().isAnimationEnabled()) {
                return;
            }

            if (!textField.isFocused() && getSkinnable().isEmpty()) {
                action.accept(hintPosition);
                setMoveHint(MOVE_HINT_UP);
            }
        }
    }

    enum Direction {
        UP, DOWN;
    }

}