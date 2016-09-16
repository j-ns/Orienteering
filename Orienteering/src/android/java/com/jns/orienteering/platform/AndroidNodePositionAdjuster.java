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
package com.jns.orienteering.platform;

import com.gluonhq.charm.glisten.application.MobileApplication;

import android.graphics.Rect;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafxports.android.FXActivity;

public class AndroidNodePositionAdjuster implements NodePositionAdjuster {

    private static final float     SCALE = FXActivity.getInstance().getResources().getDisplayMetrics().density;

    private Node                   parent;
    private ObservableValue<Node>  focusOwner;

    private ViewGroup              viewGroup;
    private Rect                   currentBounds;
    private DoubleProperty         visibleHeight;

    private boolean                keyboardShowing;

    private OnGlobalLayoutListener layoutListener;

    public AndroidNodePositionAdjuster(Parent parent, ObservableValue<Node> focusOwner) {
        this.parent = parent;
        this.focusOwner = focusOwner;

        initLayoutListener();
        initFocusListener();
    }

    @Override
    public void update(Parent parent, ObservableValue<Node> focusOwner) {
        if (this.parent == parent && this.focusOwner == focusOwner) {
            return;
        }
        this.parent = parent;

        this.focusOwner.removeListener(focusListener);
        this.focusOwner = focusOwner;
        this.focusOwner.addListener(focusListener);
    }

    private void initLayoutListener() {
        double screenHeight = MobileApplication.getInstance().getScreenHeight();

        visibleHeight = new SimpleDoubleProperty(screenHeight);
        visibleHeight.addListener((ov, n, n1) -> onHeightChanged(n, n1));

        layoutListener = layoutListener(visibleHeight);

        viewGroup = FXActivity.getViewGroup();
        viewGroup.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
        currentBounds = new Rect();
    }

    private OnGlobalLayoutListener layoutListener(DoubleProperty height) {
        return () -> height.set(getCurrentHeigt());
    }

    private void initFocusListener() {
        focusOwner.addListener(focusListener);
    }

    private ChangeListener<Node> focusListener = (ov, n, n1) ->
        {
            if (keyboardShowing && maxYPositionIsDifferent(n, n1)) {
                validateHeight(visibleHeight.get());
            }
        };

    private boolean maxYPositionIsDifferent(Node n, Node n1) {
        if (n == null || n1 == null) {
            return true;
        }
        return Double.compare(n.getLayoutBounds().getMaxY(), n1.getLayoutBounds().getMaxY()) != 0;
    }

    private float getCurrentHeigt() {
        FXActivity.getViewGroup().getRootView().getWindowVisibleDisplayFrame(currentBounds);
        return currentBounds.height() / SCALE;
    }

    private void onHeightChanged(Number oldHeight, Number newHeight) {
        double heightDelta = newHeight.doubleValue() - oldHeight.doubleValue();

        if (heightDelta < 0) {
            validateHeight(newHeight.doubleValue());

        } else if (heightDelta > 0) {
            parent.setTranslateY(0);
        }

        keyboardShowing = heightDelta < 0;
    }

    private void validateHeight(double currentHeight) {
        if (focusOwner.getValue() == null) {
            parent.setTranslateY(0);
            return;
        }
        double focusedNodeY = getBoundsInScene(focusOwner.getValue()).getMaxY();
        double result = currentHeight - focusedNodeY;

        if (result < 0) {
            Platform.runLater(() -> parent.setTranslateY(result));
        }
    }

    private Bounds getBoundsInScene(Node node) {
        return node.localToScene(node.getBoundsInLocal());
    }

    @Override
    public void removeListeners() {
        parent.setTranslateY(0);
        viewGroup.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
        focusOwner.removeListener(focusListener);
    }

}