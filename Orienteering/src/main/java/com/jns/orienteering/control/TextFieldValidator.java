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

import java.text.DecimalFormatSymbols;
import java.util.regex.Pattern;

import javafx.beans.NamedArg;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;

public class TextFieldValidator {

    private static final String CURRENCY_SYMBOL   = DecimalFormatSymbols.getInstance().getCurrencySymbol();
    private static final char   DECIMAL_SEPARATOR = DecimalFormatSymbols.getInstance().getDecimalSeparator();

    private final Pattern       INPUT_PATTERN;

    public TextFieldValidator(@NamedArg("modus") ValidationModus modus, @NamedArg("countOf") int countOf) {
        this(modus.createPattern(countOf));
    }

    public TextFieldValidator(@NamedArg("regex") String regex) {
        this(Pattern.compile(regex));
    }

    public TextFieldValidator(Pattern inputPattern) {
        INPUT_PATTERN = inputPattern;
    }

    public static TextFieldValidator maxFractionDigits(int countOf) {
        return new TextFieldValidator(maxFractionPattern(countOf));
    }

    public static TextFieldValidator maxIntegers(int countOf) {
        return new TextFieldValidator(maxIntegerPattern(countOf));
    }

    public static TextFieldValidator integersOnly() {
        return new TextFieldValidator(integersOnlyPattern());
    }

    public TextFormatter<Object> getFormatter() {
        return new TextFormatter<>(this::validateChange);
    }

    private Change validateChange(Change c) {
        if (validate(c.getControlNewText())) {
            return c;
        }
        return null;
    }

    public boolean validate(String input) {
        return INPUT_PATTERN.matcher(input).matches();
    }

    private static Pattern maxFractionPattern(int countOf) {
        return Pattern.compile("\\d*(\\" + DECIMAL_SEPARATOR + "\\d{0," + countOf + "})?");
    }

    private static Pattern maxCurrencyFractionPattern(int countOf) {
        return Pattern.compile("^\\" + CURRENCY_SYMBOL + "?\\s?\\d*(\\" + DECIMAL_SEPARATOR + "\\d{0," + countOf + "})?\\s?\\" +
                CURRENCY_SYMBOL + "?");
    }

    // "^[0-9]*\\.?[0-9]*$"
    private static Pattern maxIntegerPattern(int countOf) {
        return Pattern.compile("\\d{0," + countOf + "}");
    }

    private static Pattern integersOnlyPattern() {
        return Pattern.compile("\\d*");
    }

    public enum ValidationModus {

        MAX_CURRENCY_FRACTION_DIGITS {
            @Override
            public Pattern createPattern(int countOf) {
                return maxCurrencyFractionPattern(countOf);
            }
        },

        MAX_FRACTION_DIGITS {
            @Override
            public Pattern createPattern(int countOf) {
                return maxFractionPattern(countOf);
            }
        },
        MAX_INTEGERS {
            @Override
            public Pattern createPattern(int countOf) {
                return maxIntegerPattern(countOf);
            }
        },

        INTEGERS_ONLY {
            @Override
            public Pattern createPattern(int countOf) {
                return integersOnlyPattern();
            }
        };

        public abstract Pattern createPattern(int countOf);
    }

}