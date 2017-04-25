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
package com.gluonhq.charm.down.plugins.android;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.KeyboardService;
import com.gluonhq.charm.down.plugins.LifecycleEvent;
import com.gluonhq.charm.down.plugins.LifecycleService;
import com.gluonhq.charm.glisten.application.MobileApplication;

import android.graphics.Rect;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafxports.android.FXActivity;

public class AndroidKeyboardService implements KeyboardService {

    private static final float       SCALE = FXActivity.getInstance().getResources().getDisplayMetrics().density;

    private final InputMethodManager imm;

    private Rect                     currentBounds;

    private OnGlobalLayoutListener   layoutListener;

    private boolean                  keyboardVisible;

    public AndroidKeyboardService() {
        imm = (InputMethodManager) FXActivity.getInstance().getSystemService(FXActivity.INPUT_METHOD_SERVICE);
        initLayoutListener();
    }

    private void initLayoutListener() {
        double screenHeight = MobileApplication.getInstance().getScreenHeight();

        SimpleDoubleProperty visibleHeight = new SimpleDoubleProperty(screenHeight);
        visibleHeight.addListener((ov, n, n1) -> onHeightChanged(n, n1));

        currentBounds = new Rect();
        layoutListener = layoutListener(visibleHeight);

        FXActivity.getViewGroup().getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);

        Services.get(LifecycleService.class).ifPresent(l ->
        {
            l.addListener(LifecycleEvent.RESUME, () -> FXActivity.getViewGroup().getViewTreeObserver().addOnGlobalLayoutListener(layoutListener));
            l.addListener(LifecycleEvent.PAUSE, () -> FXActivity.getViewGroup().getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener));
        });
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AndroidKeyboardService.class);

    private OnGlobalLayoutListener layoutListener(DoubleProperty height) {
        return () -> height.set(getCurrentHeight());
    }

    private float getCurrentHeight() {
        FXActivity.getViewGroup().getWindowVisibleDisplayFrame(currentBounds);
        return currentBounds.height() / SCALE;
    }

    private void onHeightChanged(Number oldHeight, Number newHeight) {
        double heightDelta = newHeight.doubleValue() - oldHeight.doubleValue();
        keyboardVisible = heightDelta < 0;

        LOGGER.debug("previousHeight: {} currentHeight: {} visible: {}", oldHeight, newHeight, keyboardVisible);
    }

    @Override
    public boolean isVisible() {
        return keyboardVisible;
    }

    @Override
    public void show() {
        if (!keyboardVisible) {
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }
    }

    @Override
    public void hide() {
        if (keyboardVisible) {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }
}