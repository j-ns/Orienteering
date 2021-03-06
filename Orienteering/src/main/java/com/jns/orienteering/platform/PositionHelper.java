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
package com.jns.orienteering.platform;

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.Position;
import com.gluonhq.charm.down.plugins.SettingsService;

public class PositionHelper {

    private static final String          DEFAULT_POSITION_KEY = "default_position";
    private static final String          START_POSITION_KEY   = "start_position";

    private static final Position        FRANKURT_AM_MAIN     = new Position(50.107180, 8.663756);

    private static final SettingsService settingsService;

    static {
        settingsService = Services.get(SettingsService.class).orElseThrow(() -> new IllegalStateException("Failed to get SettingsService"));
    }

    private PositionHelper() {
    }

    public static Position retrieveDefaultPosition() {
        return retrieve(DEFAULT_POSITION_KEY, FRANKURT_AM_MAIN);
    }

    public static void storeDefaultPosition(Position position) {
        store(DEFAULT_POSITION_KEY, position);
    }

    public static void initStartPosition(Position position) {
        if (retrieve(START_POSITION_KEY, null) == null) {
            storeStartPosition(position);
        }
    }

    public static Position retrieveStartPosition() {
        return retrieve(START_POSITION_KEY, FRANKURT_AM_MAIN);
    }

    public static void storeStartPosition(Position position) {
        store(START_POSITION_KEY, position);
    }

    private static Position retrieve(String key, Position defaultValue) {
        String result = settingsService.retrieve(key);
        if (result == null) {
            return defaultValue;
        }
        return toPosition(result);
    }

    private static void store(String key, Position position) {
        settingsService.store(key, toPositionString(position));
    }

    public static Position toPosition(String positionText) {
        String text = positionText.replaceAll("\\s", "");
        String[] split = text.split(",");
        double latitude = Double.valueOf(split[0]);
        double longitude = Double.valueOf(split[1]);
        validateCoordinates(latitude, longitude);
        return new Position(latitude, longitude);
    }

    /**
     * Checkf if the gps coordinates are in valid range
     *
     * @param latitude
     *            in the range of [-90,90]
     * @param longitude
     *            int the range of [-180,180]
     * @throws IllegalArgumentException
     *             if the coordinates are invalid
     *
     */
    public static void validateCoordinates(double latitude, double longitude) {
        boolean isValid = latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
        if (!isValid) {
            throw new IllegalArgumentException("Not a valid location: " + latitude + ", " + longitude);
        }
    }

    public static String toPositionString(Position position) {
        return position.getLatitude() + ", " + position.getLongitude();
    }

}
