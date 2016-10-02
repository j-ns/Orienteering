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

import com.jns.orienteering.model.common.AccessType;

import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.octicons.OctIcon;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public enum Icon {
    ARROW_UP(MaterialDesignIcon.ARROW_UP_BOLD),
    BACK(FontAwesomeIcon.ARROW_LEFT),
    BARCODE(FontAwesomeIcon.QRCODE),
    CALCULATOR(FontAwesomeIcon.CALCULATOR),
    CALENDAR(MaterialDesignIcon.CALENDAR),
    CALENDAR_BLANK(MaterialDesignIcon.CALENDAR_BLANK),
    CAMERA(MaterialDesignIcon.CAMERA_IRIS),
    CLOSE_CROSS(FontAwesomeIcon.TIMES_CIRCLE),
    CLOCK(MaterialDesignIcon.CLOCK),
    COUNTER(FontAwesomeIcon.SERVER),
    DONE(MaterialIcon.DONE),
    DONE_ALL(MaterialIcon.DONE_ALL),
    DELETE(MaterialIcon.DELETE),
    DATABASE(OctIcon.DATABASE),
    DISTANCE(MaterialIcon.GESTURE),
    EDIT(FontAwesomeIcon.PENCIL),
    ELECTRICITY(OctIcon.PLUG),
    EXCLAMATION_CIRCLE(FontAwesomeIcon.EXCLAMATION_CIRCLE),
    EYE(FontAwesomeIcon.EYE),
    FILE(MaterialDesignIcon.FILE),
    FILE_DOWNLOAD(MaterialDesignIcon.DOWNLOAD),
    FILE_TEXT(OctIcon.FILE_TEXT),
    FILE_UPLOAD(MaterialDesignIcon.UPLOAD),
    FILTER(FontAwesomeIcon.EYE),
    FOLDER(MaterialDesignIcon.FOLDER),
    FOLDER_DOWNLOAD(MaterialDesignIcon.FOLDER_DOWNLOAD),
    FOLDER_OPEN(FontAwesomeIcon.FOLDER_OPEN),
    FORWARD(MaterialDesignIcon.FORWARD),
    FLASH_LIGHT(MaterialDesignIcon.FLASHLIGHT),
    FLASH_LIGHT_OFF(MaterialDesignIcon.FLASHLIGHT_OFF),
    GAS(OctIcon.FLAME),
    GLOBE(FontAwesomeIcon.GLOBE),
    GPS_LOCATION(MaterialDesignIcon.CROSSHAIRS_GPS),
    HELP(MaterialDesignIcon.HELP),
    INFO(FontAwesomeIcon.INFO),
    LINE_CHART(FontAwesomeIcon.LINE_CHART),
    LIST(FontAwesomeIcon.LIST_UL),
    LIST_NUMBERED(FontAwesomeIcon.LIST_OL),
    LOCK(FontAwesomeIcon.LOCK),
    UNLOCK(FontAwesomeIcon.UNLOCK),
    MAP(FontAwesomeIcon.MAP_ALT),
    MAP_PIN(FontAwesomeIcon.MAP_PIN),
    MAP_MARKER(MaterialDesignIcon.MAP_MARKER),
    MAP_MARKER_OFF(MaterialDesignIcon.MAP_MARKER_OFF),
    MAP_MARKER_CIRCLE(MaterialDesignIcon.MAP_MARKER_RADIUS),
    MINUS(FontAwesomeIcon.MINUS),
    PICTURES(MaterialIcon.COLLECTIONS),
    PLUS(FontAwesomeIcon.PLUS),
    RECYCLE(FontAwesomeIcon.RECYCLE),
    REFRESH(FontAwesomeIcon.REFRESH),
    SETTINGS(FontAwesomeIcon.COG),
    SKIP_FORWARD(MaterialDesignIcon.SKIP_NEXT),
    SHARE(MaterialDesignIcon.SHARE),
    STAR(MaterialDesignIcon.STAR),
    TABLE(FontAwesomeIcon.TABLE),
    TEAM(FontAwesomeIcon.GROUP),
    TIMELAPSE(MaterialDesignIcon.TIMELAPSE),
    THUMP_UP(FontAwesomeIcon.THUMBS_UP),
    TRIANGLE_DOWN(OctIcon.TRIANGLE_DOWN),
    WATER(FontAwesomeIcon.TINT);

    public static final String   DEFAULT_ICON_SIZE = "24";

    private transient GlyphIcons glypIcon;

    private Icon(GlyphIcons icon) {
        glypIcon = icon;
    }

    public Label label() {
        return new Label(null, icon());
    }

    public Label label(String size) {
        Label lblIcon = new Label(null, icon(size));
        lblIcon.setAlignment(Pos.CENTER);
        lblIcon.setPrefWidth(24);
        return lblIcon;
    }

    public Button button(EventHandler<ActionEvent> evt) {
        Button button = button();
        button.setOnAction(evt);
        return button;
    }

    public Button button() {
        return new Button(null, icon(DEFAULT_ICON_SIZE));
    }

    public Button button(String size) {
        return new Button(null, icon(size));
    }

    public Node icon() {
        return GlyphsDude.createIcon(glypIcon);
    }

    public Node icon(String size) {
        return GlyphsDude.createIcon(glypIcon, size);
    }

    public static class Buttons {

        private Buttons() {
        }

        public static Button save(EventHandler<ActionEvent> onAction) {
            return Icon.DONE.button(onAction);
        }

        public static Button saveAndContinue(EventHandler<ActionEvent> onAction) {
            return Icon.DONE_ALL.button(onAction);
        }

        public static Button delete(EventHandler<ActionEvent> onAction) {
            return Icon.DELETE.button(onAction);
        }

        public static Button back(EventHandler<ActionEvent> onAction) {
            Button button = new Button(com.gluonhq.charm.glisten.visual.MaterialDesignIcon.ARROW_BACK.text);
            button.setOnAction(onAction);
            return button;
        }

        public static Button filter(EventHandler<ActionEvent> onAction) {
            return Icon.FILTER.button(onAction);
        }

        public static Button refresh(EventHandler<ActionEvent> onAction) {
            Button button = Icon.REFRESH.button("22");
            button.setOnAction(onAction);
            return button;
        }

        public static StateButton<AccessType> accessType() {
            Node locked = LOCK.icon(DEFAULT_ICON_SIZE);
            Node unlocked = UNLOCK.icon(DEFAULT_ICON_SIZE);
            return new StateButton<>(locked, unlocked);
        }

    }

}