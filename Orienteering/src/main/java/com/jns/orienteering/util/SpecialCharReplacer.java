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

import java.util.regex.Pattern;

public class SpecialCharReplacer {

    public static final Pattern INVALID_CHARS_PATTERN = Pattern.compile("[@/./$/[/]#//]+");
    // ., $, #, [, ], /, or ASCII control characters 0-31 or 127.

    private enum Replacements {
        Ae("\u00C4", "Ae"),
        Ue("\u00DC", "Ue"),
        Oe("\u00D6", "Oe"),
        ae("\u00E4", "ae"),
        ue("\u00FC", "ue"),
        oe("\u00F6", "oe"),
        whiteSpace(" ", "@"),
        // sz("\u00DF", "ss"), zu INVALID pattern hinzufügen
        // point(".",""),
        // dollar("$", ""),
        // hash("#", ""),
        // bracketLeft("[", ""),
        // bracketRight("]", ""),
        // slash("/", ""),
        ;

        private String specialChar;
        private String replacement;

        private Replacements(String original, String replacement) {
            specialChar = original;
            this.replacement = replacement;
        }
    }

    public static boolean validateInput(String input) {
        return !INVALID_CHARS_PATTERN.matcher(input).find();
    }

    public static String replaceSpecialChars(String input) {
        if (input == null) {
            return input;
        }

        String tmp = input;
        for (Replacements replacement : Replacements.values()) {
            tmp = tmp.replaceAll(replacement.specialChar, replacement.replacement);
        }
        return tmp;
    }

    public static String replaceWithSpecialChars(String input) {
        if (input == null) {
            return input;
        }

        String tmp = input;
        for (Replacements replacement : Replacements.values()) {
            tmp = tmp.replaceAll(replacement.replacement, replacement.specialChar);
        }
        return tmp;
    }

}
