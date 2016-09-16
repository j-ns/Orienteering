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

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class StringUtils {

    private static final Function<?, String> defaultStringConverter = t -> t == null ? "" : t.toString();

    private StringUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <T> Function<T, String> getDefaultStringConverter() {
        return (Function<T, String>) defaultStringConverter;
    }

    public static <T> String[] toSortedStringArray(List<T> items, Function<T, String> stringMapper) {
        String[] values = stringMapper == null ? toStringArray(items) : toStringArray(items, stringMapper);
        Arrays.sort(values);
        return values;
    }

    public static <T> String[] toSortedStringArray(List<T> items) {
        String[] values = toStringArray(items);
        Arrays.sort(values);
        return values;
    }

    public static <T> String[] toStringArray(List<T> items) {
        return toStringArray(items, Object::toString);
    }

    /**
     * @param items
     * @param stringMapper
     * @return String[items.size()], or String[0], if items == null || items.isEmpty()
     */
    public static <T> String[] toStringArray(List<T> items, Function<T, String> stringMapper) {
        if (items == null || items.isEmpty()) {
            return new String[0];
        }

        String[] values = new String[items.size()];

        for (int idx = 0; idx < items.size(); idx++) {
            values[idx] = stringMapper.apply(items.get(idx));
        }

        return values;
    }

    /**
     * @param items
     * @param stringMapper
     * @return joined String or empty String if items == null || items.isEmpty()
     */
    public static <T> String joinToString(List<T> items) {
        return joinToString(items, Object::toString);
    }

    /**
     * @param items
     * @param stringMapper
     * @return joined String or empty String if items == null || items.isEmpty()
     */
    public static <T> String joinToString(List<T> items, Function<T, String> stringMapper) {
        String[] values = toStringArray(items, stringMapper);
        return join(values);
    }

    public static String join(String[] values) {
        return join(values, ", ");
    }

    public static String join(String[] values, String separator) {
        if (values.length == 0) {
            return "";
        }

        StringBuilder sb = null;

        for (String value : values) {
            if (sb != null) {
                sb.append(separator);
                sb.append(value);
            } else {
                sb = new StringBuilder().append(value);
            }
        }

        return sb.toString();
    }

}
