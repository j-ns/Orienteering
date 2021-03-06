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
package com.jns.orienteering.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Validations {

    private static final Predicate<Character> IS_DIGIT = c -> c >= '0' && c <= '9';

    private Validations() {
    }

    public static boolean isValidLength(String text, int validLength) {
        return text.length() == validLength;
    }

    public static boolean isDigit(String input) {
        if (input.length() > 1) {
            throw new IllegalArgumentException("valid input length = 0 || 1");
        }
        return isDigit(input, 0);
    }

    public static boolean isDigit(String input, int idx) {
        if (isNullOrEmpty(input)) {
            return false;
        }
        return isDigit(input.charAt(idx));
    }

    public static boolean isDigit(char input) {
        return IS_DIGIT.test(input);
    }

    public static boolean isZero(String text, int idx) {
        return text.charAt(idx) == '0';
    }

    public static boolean isNotNullOrEmpty(String text) {
        return !isNullOrEmpty(text);
    }

    public static <T> boolean isObjectNullOrEmpty(T input) {
        if (input instanceof String) {
            return isNullOrEmpty((String) input);
        }
        return input == null;
    }

    public static boolean isNullOrEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static <T> Function<T, Boolean> isNullOrEmpty(Function<T, String> mappingFunction) {
        return t -> t == null || isNullOrEmpty(mappingFunction.apply(t));
    }

    public static boolean isNullOrEmpty(Collection<?> items) {
        return items == null || items.isEmpty();
    }

    public static <E> void ifNullOrEmpty(Collection<E> items, Consumer<Collection<E>> action) {
        if (isNullOrEmpty(items)) {
            action.accept(items);
        }
    }

    public static <E> void ifNotNullOrEmpty(Collection<E> items, Consumer<Collection<E>> action) {
        if (!isNullOrEmpty(items)) {
            action.accept(items);
        }
    }

    public static <T> boolean sizeIsGreaterThan(Collection<T> items, int size) {
        return isNullOrEmpty(items) ? false : items.size() > size;
    }

    public static void ensureCountOfDigits(List<Integer> sourceList, int countOfDigits) {
        int size = sourceList.size();

        if (size < countOfDigits) {
            sourceList.addAll(0, Collections.nCopies(countOfDigits - size, 0));

        } else if (size > countOfDigits) {
            throw new IllegalArgumentException("sourceList size is greater than countOfDigits");
        }
    }
}