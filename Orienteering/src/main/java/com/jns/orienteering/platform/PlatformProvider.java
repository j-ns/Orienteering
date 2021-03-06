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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.down.Platform;

public class PlatformProvider {
    private static final Logger    LOGGER          = LoggerFactory.getLogger(PlatformProvider.class);

    private static final String    JAVAFX_PLATFORM = "javafx.platform";

    private static PlatformService platformService;

    private PlatformProvider() {
    }

    public static PlatformService getPlatformService(String platform) {
        switch (platform) {
            case "android":
                System.setProperty(JAVAFX_PLATFORM, "android");
                break;
            case "desktop":
                System.setProperty(JAVAFX_PLATFORM, "desktop");
                break;
            case "ios":
                System.setProperty(JAVAFX_PLATFORM, "ios");
                break;
            default:
                throw new IllegalArgumentException("Unknown platform");

        }
        return getPlatformService();
    }

    public static PlatformService getPlatformService() {
        if (platformService == null) {
            try {
                platformService = (PlatformService) Class.forName(getPlatformClassName()).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                LOGGER.error("platformprovider error: ", e);
            }
        }

        return platformService;
    }

    private static String getPlatformClassName() {
        if (Platform.isDesktop()) {
            return "com.jns.orienteering.platform.DesktopPlatform";
        } else if (Platform.isAndroid()) {
            return "com.jns.orienteering.platform.AndroidPlatform";
        } else if (Platform.isIOS()) {
            throw new UnsupportedOperationException("IOS platform not implemented yet");
        }
        throw new UnsupportedOperationException("unsupported platform");
    }
}
