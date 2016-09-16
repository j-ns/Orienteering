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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.function.Supplier;

public class DateTimeFormatters {

    public static final DateTimeFormatter yearFormatter   = DateTimeFormatter.ofPattern("yyyy");
    public static final DateTimeFormatter monthFormatter  = DateTimeFormatter.ofPattern("MMM");
    public static final DateTimeFormatter dayFormatter    = DateTimeFormatter.ofPattern("dd");
    public static final DateTimeFormatter mediumFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
    public static final DateTimeFormatter longFormatter   = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
    public static final DateTimeFormatter timeFormatter   = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter timeStamp       = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private DateTimeFormatters() {
    }

    /**
     * @param date
     * @return date String for pattern = "yyyy"
     */
    public static String formatYear(LocalDate date) {
        return formatSafe(date, () -> yearFormatter);
    }

    /**
     * @param date
     * @return date String for pattern ="MMM"
     */
    public static String formatMonth(LocalDate date) {
        return formatSafe(date, () -> monthFormatter);
    }

    /**
     * @param date
     * @return date String for pattern = "dd"
     */
    public static String formatDay(LocalDate date) {
        return formatSafe(date, () -> dayFormatter);
    }

    public static String formatTime(long secondsOfDay) {
        return timeFormatter.format(LocalTime.ofSecondOfDay(secondsOfDay));
    }

    public static String formatTime(LocalTime time) {
        return timeFormatter.format(time);
    }

    public static String formatTimeStamp(LocalDateTime time) {
        return timeStamp.format(time);
    }

    /**
     * @param date
     * @return date String for pattern = "dd.MM.yyyy"
     */
    public static String formatMedium(LocalDate date) {
        return formatSafe(date, () -> mediumFormatter);
    }

    public static String formatLong(LocalDate date) {
        return formatSafe(date, () -> longFormatter);
    }

    private static String formatSafe(LocalDate date, Supplier<DateTimeFormatter> formatter) {
        return date == null ? "" : date.format(formatter.get());
    }

    public static String createTimeStamp() {
        return formatTimeStamp(LocalDateTime.now());
    }
}
