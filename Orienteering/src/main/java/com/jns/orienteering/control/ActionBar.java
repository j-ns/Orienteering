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

import com.gluonhq.charm.glisten.mvc.View;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * An ActionBar is a ToolBar control placed in the bottom of a {@link View}
 */
public class ActionBar extends Pane {

    /**
     * @param node
     *            which should be added to the ActionBar
     *
     */
    public ActionBar(Node... node) {
        this();
        getChildren().addAll(node);
    }

    private ActionBar() {
        getStyleClass().add("action-bar");
        setMinHeight(USE_PREF_SIZE);
        setMaxHeight(USE_PREF_SIZE);
    }

    /**
     * Toggles the visibility of the Node at <code>nodeIdx</code>
     *
     * @param nodeIdx
     *            position of the Node whose visibility should be changed. From left to right
     * @param value
     */
    public void setVisible(int nodeIdx, boolean value) {
        getChildren().get(nodeIdx).setVisible(value);
        if (isVisible()) {
            layoutChildren();
        }
    }

    @Override
    protected void layoutChildren() {
        double totalWidth = getWidth();
        double totalWidthButtons = 0;
        int countOfVisibleChildren = 0;

        for (Node c : getChildren()) {
            if (c.isVisible()) {
                totalWidthButtons += c.prefWidth(-1);
                countOfVisibleChildren++;
            }
        }

        double widthSpacer = (totalWidth - totalWidthButtons) / (countOfVisibleChildren + 1);
        double currentX = widthSpacer;
        double heightBar = getHeight();

        for (Node c : getChildren()) {
            if (c.isVisible()) {
                double widthButton = c.prefWidth(-1);

                layoutInArea(c, currentX, 0, widthButton, heightBar, 0, HPos.CENTER, VPos.CENTER);
                currentX = currentX + widthButton + widthSpacer;
            }
        }
    }

    @Override
    protected double computePrefWidth(double height) {
        return getParent().prefWidth(height);
    }

    @Override
    protected double computePrefHeight(double width) {
        return 48;
    }

}