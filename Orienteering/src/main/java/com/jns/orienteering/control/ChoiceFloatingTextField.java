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

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class ChoiceFloatingTextField<T> extends BaseChoiceField<T> {

    protected FloatingTextField displayNode;
    private Label               openButton;
    private StackPane           pneOpenButton;

    public ChoiceFloatingTextField() {
        getStyleClass().addAll("choice-text-field");
        setMaxWidth(Double.MAX_VALUE);

        displayNode = new FloatingTextField();
        setEditable(false);

        setShowOpenButton(false);
        initOpenButton();
    }

    private void initOpenButton() {
        openButton = Icon.TRIANGLE_DOWN.label("14");
        pneOpenButton = new StackPane(openButton);
        displayNode.setGraphic(pneOpenButton);
    }

    @Override
    public void setHint(String text) {
        displayNode.setHint(text);
    }

    @Override
    public String getHint() {
        return displayNode.getHint();
    }

    @Override
    public void setEditable(boolean value) {
        displayNode.setEditable(value);
    }

    @Override
    public void updateDisplay(T item) {
        if (item == null) {
            displayNode.setText("");
        } else {
            displayNode.setText(getStringConverter().apply(item));
        }
    }

    @Override
    public Node getDisplayNode() {
        return displayNode;
    }

}