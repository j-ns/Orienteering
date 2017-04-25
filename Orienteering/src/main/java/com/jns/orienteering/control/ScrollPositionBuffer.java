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
package com.jns.orienteering.control;

import com.gluonhq.charm.glisten.control.Dialog;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;

/**
 * Buffers the current vertical position of a ScrollPane and restores the buffered position,
 * when the current position is set to '0' and <code>positionBuffer</code> > 0 .
 * This is helpful when you show a {@link Dialog} on top of a ScrollPane, which would set the
 * vertical position of the ScrollPane to '0' when the Dialog is dismissed.
 *
 */
public class ScrollPositionBuffer implements ActivatableDeactivatable {

    private ScrollPane                           scrollPane;
    private double                               positionBuffer;
    private boolean                              active              = false;

    private final ChangeListener<? super Number> positionListener    = (ov, t, t1) ->
                                                                     {
                                                                         if (Double.doubleToRawLongBits((double) t1) == 0 && positionBuffer > 0) {
                                                                             scrollPane.setVvalue(positionBuffer);
                                                                             positionBuffer = 0;
                                                                         }
                                                                     };

    private final EventHandler<MouseEvent>       mouseClickedHandler = e ->
                                                                     {
                                                                         if (active) {
                                                                             buffer();
                                                                         }
                                                                     };

    /**
     * @param scrollPane
     *            the ScrollPane whose vertical position should be buffered and restored
     */
    public ScrollPositionBuffer(ScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

    /**
     * @param scrollPane
     *            the ScrollPane whose vertical position should be buffered and restored
     * @param triggerNode
     *            the Node which triggers the buffering of the vertical position of <code>scrollPane</code>, when it is clicked
     */
    public ScrollPositionBuffer(ScrollPane scrollPane, Node... triggerNode) {
        this.scrollPane = scrollPane;
        addTriggerNodes(triggerNode);
    }

    /**
     * Adds nodes which trigger the buffering of the ScrollPane position on {@link MouseEvent#MOUSE_CLICKED}
     *
     * @param node
     */
    public void addTriggerNodes(Node... node) {
        for (Node n : node) {
            n.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseClickedHandler);
        }
    }

    /**
     * Buffers the current vertical position of the ScrollPane
     */
    public void buffer() {
        positionBuffer = scrollPane.getVvalue();
    }

    @Override
    public void activate() {
        if (!active) {
            scrollPane.vvalueProperty().addListener(positionListener);
            active = true;
        }
    }

    @Override
    public void deactivate() {
        if (active) {
            scrollPane.vvalueProperty().removeListener(positionListener);
            active = false;
        }
    }
}