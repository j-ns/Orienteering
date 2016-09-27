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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * Consumes {@link MouseEvent#MOUSE_CLICKED} which occurs when a Node was dragged.
 * This is useful in combination with a ListView, where scrolling the ListView would be interpreted as a MOUSE_CLICKED Event
 * which would select the ListCell under the mouse position otherwise.
 *
 */
public class ScrollListener implements ActivatableDeactivatable {

    private final Node                       observableNode;

    private BooleanProperty                  scrolling          = new ReadOnlyBooleanWrapper(false);

    private EventHandler<? super MouseEvent> dragDetectedFilter = e -> scrolling.set(true);

    private EventHandler<? super MouseEvent> mouseClickedFilter = evt ->
                                                                {
                                                                    if (scrolling.get()) {
                                                                        scrolling.set(false);
                                                                        evt.consume();
                                                                    }
                                                                };

    private EventHandler<? super MouseEvent> mouseExitedHandler = e -> scrolling.set(false);

    private boolean                          listenersEnabled;

    public ScrollListener(Node observableNode) {
        this.observableNode = observableNode;
    }

    @Override
    public void activate() {
        if (!listenersEnabled) {
            observableNode.addEventFilter(MouseEvent.DRAG_DETECTED, dragDetectedFilter);
            observableNode.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseClickedFilter);
            observableNode.addEventHandler(MouseEvent.MOUSE_EXITED, mouseExitedHandler);
        }
    }

    @Override
    public void deactivate() {
        if (listenersEnabled) {
            observableNode.removeEventFilter(MouseEvent.DRAG_DETECTED, dragDetectedFilter);
            observableNode.removeEventFilter(MouseEvent.MOUSE_CLICKED, mouseClickedFilter);
            observableNode.removeEventHandler(MouseEvent.MOUSE_EXITED, mouseExitedHandler);
        }
    }

    public ReadOnlyBooleanProperty scrollingProperty() {
        return scrolling;
    }

    public boolean isScrolling() {
        return scrolling.get();
    }

}