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
package com.jns.orienteering.util;

import java.util.function.Function;

import javafx.collections.ObservableList;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Calculations {

    private Calculations() {
    }

    public static <T> double maxTextWidth(ObservableList<T> items, Font font) {
        return greatestTextWidth(items, defaultStringMapper(), font);
    }

    public static <T> double greatestTextWidth(ObservableList<T> items, Function<T, String> stringMapper, Font font) {
        if (stringMapper == null) {
            stringMapper = defaultStringMapper();
        }

        T result = findItemWithLongestString(items, stringMapper);
        return result == null ? 0 : textWidth(stringMapper.apply(result), font);
    }

    public static <T> T findItemWithLongestString(ObservableList<T> items) {
        return findItemWithLongestString(items, defaultStringMapper());
    }

    public static <T> T findItemWithLongestString(ObservableList<T> items, Function<T, String> stringMapper) {
        int maxLength = 0;
        T result = null;

        if (items == null || items.isEmpty()) {
            return result;
        }

        for (T item : items) {
            int length = stringMapper.apply(item).length();
            if (length > maxLength) {
                maxLength = length;
                result = item;
            }
        }
        return result;
    }

    private static <T> Function<T, String> defaultStringMapper() {
        return t -> t.toString();
    }

    public static double textWidth(String value) {
        return textWidth(value, null);

    }

    public static double textWidth(String value, Font font) {
        Text text = new Text(value);
        if (font != null) {
            text.setFont(font);
        }
        return text.prefWidth(-1);
    }
}
