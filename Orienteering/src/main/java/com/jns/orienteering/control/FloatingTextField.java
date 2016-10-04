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

import java.text.NumberFormat;
import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jns.orienteering.control.skin.FloatingTextFieldSkin;

import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;

public class FloatingTextField extends Control {

    private static final Logger     LOGGER               = LoggerFactory.getLogger(FloatingTextField.class);

    public static final PseudoClass ACTIVE_PSEUDO_CLASS  = PseudoClass.getPseudoClass("active");
    public static final PseudoClass FOCUSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("text-field-focused");

    private NumberFormat            numberFormat;

    private TextField               textField;
    private StringProperty          hint;
    private StringProperty          text;

    private BooleanProperty         active;
    private BooleanProperty         empty;
    private BooleanProperty         editable;

    ObjectProperty<Node>            graphicNode;

    private boolean                 animationEnabled     = true;

    public FloatingTextField(@NamedArg("validator") TextFieldValidator validator) {
        this();
        textField.setTextFormatter(validator.getFormatter());
    }

    public FloatingTextField() {
        setMinHeight(Region.USE_PREF_SIZE);
        setMaxHeight(Region.USE_PREF_SIZE);

        hint = new SimpleStringProperty();
        text = new SimpleStringProperty();
        textField = new TextField();
        textField.textProperty().bindBidirectional(text);
    }

    public void maskInput() {
        textField.textProperty().unbind();
        textField = new PasswordField();
        textField.textProperty().bindBidirectional(text);
    }

    public StringProperty hintProperty() {
        return hint;
    }

    public String getHint() {
        return hint.get();
    }

    public void setHint(String text) {
        hint.set(text);
    }

    public final StringProperty textProperty() {
        return text;
    }

    public String getText() {
        return text.get();
    }

    public void setText(String value) {
        text.set(value);
    }

    public void setDoubleAsText(double number) {
        try {
            String text = numberFormat().format(number);
            setText(text);
        } catch (ArithmeticException e) {
            LOGGER.error("can not format number as string: '{}'", number, e);
            setText("");
        }
    }

    public double getTextAsDouble() {
        try {
            Number parse = numberFormat().parse(getText().replaceAll("[^\\d\\.,]", ""));
            return parse.doubleValue();

        } catch (ParseException e) {
            LOGGER.error("failed to parse text: {}, return '0' instead", getText(), e);
            return 0;
        }
    }

    private NumberFormat numberFormat() {
        if (numberFormat == null) {
            numberFormat = NumberFormat.getInstance();
            numberFormat.setGroupingUsed(false);
        }
        return numberFormat;
    }

    public TextField getTextField() {
        return textField;
    }

    public ObjectProperty<Node> graphicProperty() {
        if (graphicNode == null) {
            graphicNode = new SimpleObjectProperty<>();
        }
        return graphicNode;
    }

    public Node getGraphic() {
        return graphicNode == null ? null : graphicNode.get();
    }

    public void setGraphic(Node graphic) {
        graphicProperty().set(graphic);
    }

    public BooleanProperty activeProperty() {
        if (active == null) {
            active = new BooleanPropertyBase() {

                @Override
                protected void invalidated() {
                    pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, get());
                }

                @Override
                public String getName() {
                    return "atTop";
                }

                @Override
                public Object getBean() {
                    return FloatingTextField.this;
                }
            };
        }
        return active;
    }

    public boolean isActive() {
        return activeProperty().get();
    }

    public void setActive(boolean active) {
        activeProperty().set(active);
    }

    public BooleanProperty editableProperty() {
        if (editable == null) {
            editable = new SimpleBooleanProperty(true);
            editableProperty().addListener((obsValue, b, b1) ->
                {
                    textField.setEditable(b1);
                    setMouseTransparent(!b1);
                });
        }
        return editable;
    }

    public void setEditable(boolean value) {
        editableProperty().set(value);
    }

    public boolean isEditable() {
        return textField.isEditable();
    }

    public ReadOnlyBooleanProperty emptyProperty() {
        if (empty == null) {
            empty = new ReadOnlyBooleanWrapper();
            empty.bind(textField.textProperty().isEmpty());
        }
        return empty;
    }

    public boolean isEmpty() {
        return emptyProperty().get();
    }

    public void setAnimationEnabled(boolean enabled) {
        animationEnabled = enabled;
    }

    public boolean isAnimationEnabled() {
        return animationEnabled;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new FloatingTextFieldSkin(this);
    }

}
