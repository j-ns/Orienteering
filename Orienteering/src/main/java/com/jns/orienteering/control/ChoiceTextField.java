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

import static com.jns.orienteering.util.Validations.isNullOrEmpty;

import com.jns.orienteering.util.Calculations;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TextField;

public class ChoiceTextField<T> extends BaseChoiceField<T> {

    private TextField textField;

    private String    textOld;
    private double    prefWidthCache;

    public ChoiceTextField() {
        super();
        textField = new TextField() {
            @Override
            protected double computePrefWidth(double height) {
                return calculatePrefWidth();
            }
        };

        setEditable(false);
    }

    private double calculatePrefWidth() {
        String text = getDisplayText();

        if (text != null && text.equals(textOld)) {
            return prefWidthCache;
        }

        double textWidth = Calculations.textWidth(text, textField.getFont());
        Insets insets = textField.getInsets();
        double prefWidth = insets.getLeft() + textWidth + insets.getRight();

        textOld = text;
        prefWidthCache = prefWidth + 2;
        return prefWidthCache;
    }

    private String getDisplayText() {
        String text = textField.getText();
        if (isNullOrEmpty(text)) {
            text = getHint();
        }
        return text;
    }

    @Override
    public String getHint() {
        return textField.getPromptText();
    }

    @Override
    public void setHint(String text) {
        textField.setPromptText(text);
    }

    public boolean isEditable() {
        return textField.isEditable();
    }

    @Override
    public void setEditable(boolean value) {
        textField.setEditable(value);
        textField.setMouseTransparent(!value);
    }

    @Override
    public void updateDisplay(T item) {
        if (item == null) {
            textField.setText("");
        } else {
            textField.setText(getStringConverter().apply(item));
        }
    }

    @Override
    public Node getDisplayNode() {
        return textField;
    }

}
