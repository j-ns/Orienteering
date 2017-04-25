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
package com.jns.orienteering.control;

import java.util.function.Supplier;

import com.jns.orienteering.model.persisted.AccessType;

import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.utils.MaterialDesignIconFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import de.jensd.fx.glyphs.octicons.OctIcon;
import de.jensd.fx.glyphs.octicons.utils.OctIconFactory;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public enum Icon {
    ADD(MaterialIcon.ADD, MaterialIconFactory::get),
    ARROW_UP(MaterialDesignIcon.ARROW_UP_BOLD, MaterialDesignIconFactory::get),
    BACK(FontAwesomeIcon.ARROW_LEFT, FontAwesomeIconFactory::get),
    BACKUP_RESTORE(MaterialDesignIcon.BACKUP_RESTORE, MaterialDesignIconFactory::get),
    BARCODE(FontAwesomeIcon.QRCODE, FontAwesomeIconFactory::get),
    BOXES(FontAwesomeIcon.TH, FontAwesomeIconFactory::get),
    CALCULATOR(FontAwesomeIcon.CALCULATOR, FontAwesomeIconFactory::get),
    CALENDAR(MaterialDesignIcon.CALENDAR, MaterialDesignIconFactory::get),
    CALENDAR_BLANK(MaterialDesignIcon.CALENDAR_BLANK, MaterialDesignIconFactory::get),
    CAMERA(MaterialDesignIcon.CAMERA_IRIS, MaterialDesignIconFactory::get),
    CLOSE(MaterialDesignIcon.CLOSE, FontAwesomeIconFactory::get),
    CLOSE_CROSS(FontAwesomeIcon.TIMES_CIRCLE, FontAwesomeIconFactory::get),
    CLOCK(MaterialDesignIcon.CLOCK, MaterialDesignIconFactory::get),
    CLOUD_UPLOAD(FontAwesomeIcon.CLOUD_UPLOAD, FontAwesomeIconFactory::get),
    CLOUD_DOWNLOAD(FontAwesomeIcon.CLOUD_DOWNLOAD, FontAwesomeIconFactory::get),
    CLOUD_OFFLINE(MaterialDesignIcon.CLOUD_OUTLINE_OFF, MaterialDesignIconFactory::get),
    COUNTER(FontAwesomeIcon.SERVER, FontAwesomeIconFactory::get),
    DATABASE(OctIcon.DATABASE, OctIconFactory::get),
    DONE(MaterialIcon.DONE, MaterialDesignIconFactory::get),
    DONE_ALL(MaterialIcon.DONE_ALL, MaterialDesignIconFactory::get),
    DOWN(MaterialDesignIcon.CHEVRON_DOWN, MaterialDesignIconFactory::get),
    DELETE(MaterialIcon.DELETE, MaterialDesignIconFactory::get),
    DISTANCE(MaterialIcon.GESTURE, MaterialDesignIconFactory::get),
    GLASS(FontAwesomeIcon.GLASS, FontAwesomeIconFactory::get),
    EDIT(FontAwesomeIcon.PENCIL, FontAwesomeIconFactory::get),
    ELECTRICITY(OctIcon.PLUG, OctIconFactory::get),
    EXCLAMATION_CIRCLE(FontAwesomeIcon.EXCLAMATION_CIRCLE, FontAwesomeIconFactory::get),
    EXIT(FontAwesomeIcon.SIGN_OUT, FontAwesomeIconFactory::get),
    EYE(FontAwesomeIcon.EYE, FontAwesomeIconFactory::get),
    FILE(MaterialDesignIcon.FILE, MaterialDesignIconFactory::get),
    FILE_DOWNLOAD(MaterialDesignIcon.DOWNLOAD, MaterialDesignIconFactory::get),
    FILE_TEXT(OctIcon.FILE_TEXT, OctIconFactory::get),
    FILE_UPLOAD(MaterialDesignIcon.UPLOAD, MaterialDesignIconFactory::get),
    FILTER(FontAwesomeIcon.EYE, FontAwesomeIconFactory::get),
    FOLDER(MaterialDesignIcon.FOLDER, MaterialDesignIconFactory::get),
    FOLDER_DOWNLOAD(MaterialDesignIcon.FOLDER_DOWNLOAD, MaterialDesignIconFactory::get),
    FOLDER_OPEN(FontAwesomeIcon.FOLDER_OPEN, FontAwesomeIconFactory::get),
    FORWARD(MaterialDesignIcon.FORWARD, MaterialDesignIconFactory::get),
    FLAME(OctIcon.FLAME, OctIconFactory::get),
    FLASH_LIGHT(MaterialDesignIcon.FLASHLIGHT, MaterialDesignIconFactory::get),
    FLASH_LIGHT_OFF(MaterialDesignIcon.FLASHLIGHT_OFF, MaterialDesignIconFactory::get),
    FLASK(FontAwesomeIcon.FLASK, FontAwesomeIconFactory::get),
    GLOBE(FontAwesomeIcon.GLOBE, FontAwesomeIconFactory::get),
    GPS_LOCATION(MaterialDesignIcon.CROSSHAIRS_GPS, MaterialDesignIconFactory::get),
    HELP(MaterialDesignIcon.HELP, MaterialDesignIconFactory::get),
    IMAGE_FILTER_VINTAGE(MaterialDesignIcon.IMAGE_FILTER_VINTAGE, MaterialDesignIconFactory::get),
    INFO(FontAwesomeIcon.INFO, FontAwesomeIconFactory::get),
    LEAF(MaterialDesignIcon.LEAF, MaterialDesignIconFactory::get),
    LINE_CHART(FontAwesomeIcon.LINE_CHART, FontAwesomeIconFactory::get),
    LIST(FontAwesomeIcon.LIST_UL, FontAwesomeIconFactory::get),
    LIST_NUMBERED(FontAwesomeIcon.LIST_OL, FontAwesomeIconFactory::get),
    LOCAL_DRINK(MaterialIcon.LOCAL_DRINK, MaterialIconFactory::get),
    LOCK(FontAwesomeIcon.LOCK, FontAwesomeIconFactory::get),
    UNLOCK(FontAwesomeIcon.UNLOCK, FontAwesomeIconFactory::get),
    MAP(FontAwesomeIcon.MAP_ALT, FontAwesomeIconFactory::get),
    MAP_PIN(FontAwesomeIcon.MAP_PIN, FontAwesomeIconFactory::get),
    MAP_MARKER(MaterialDesignIcon.MAP_MARKER, MaterialDesignIconFactory::get),
    MAP_MARKER_OFF(MaterialDesignIcon.MAP_MARKER_OFF, MaterialDesignIconFactory::get),
    MAP_MARKER_CIRCLE(MaterialDesignIcon.MAP_MARKER_RADIUS, MaterialDesignIconFactory::get),
    MARTINI(MaterialDesignIcon.MARTINI, MaterialDesignIconFactory::get),
    MINUS(FontAwesomeIcon.MINUS, FontAwesomeIconFactory::get),
    PENCIL(FontAwesomeIcon.PENCIL, FontAwesomeIconFactory::get),
    PICTURES(MaterialIcon.COLLECTIONS, MaterialIconFactory::get),
    PLUS(FontAwesomeIcon.PLUS, FontAwesomeIconFactory::get),
    POT(MaterialDesignIcon.POT, MaterialDesignIconFactory::get),
    RECYCLE(FontAwesomeIcon.RECYCLE, FontAwesomeIconFactory::get),
    REFRESH(FontAwesomeIcon.REFRESH, FontAwesomeIconFactory::get),
    SEARCH(MaterialDesignIcon.MAGNIFY, MaterialDesignIconFactory::get),
    SETTINGS(FontAwesomeIcon.COG, FontAwesomeIconFactory::get),
    SKIP_FORWARD(MaterialDesignIcon.SKIP_NEXT, MaterialDesignIconFactory::get),
    SHARE(MaterialDesignIcon.SHARE, MaterialDesignIconFactory::get),
    SPOON(MaterialDesignIcon.SILVERWARE_SPOON, MaterialDesignIconFactory::get),
    STAR(MaterialDesignIcon.STAR, MaterialDesignIconFactory::get),
    STEP_BACKWARD(MaterialDesignIcon.STEP_BACKWARD, MaterialDesignIconFactory::get),
    TABLE(FontAwesomeIcon.TABLE, FontAwesomeIconFactory::get),
    TAGS(FontAwesomeIcon.TAGS, FontAwesomeIconFactory::get),
    TEAM(FontAwesomeIcon.GROUP, FontAwesomeIconFactory::get),
    TIMELAPSE(MaterialDesignIcon.TIMELAPSE, MaterialDesignIconFactory::get),
    THUMP_UP(FontAwesomeIcon.THUMBS_UP, FontAwesomeIconFactory::get),
    TRAINING(FontAwesomeIcon.CLONE, FontAwesomeIconFactory::get),
    TRAINING2(MaterialDesignIcon.BOOK_OPEN_PAGE_VARIANT, MaterialDesignIconFactory::get),
    TRIANGLE_DOWN(OctIcon.TRIANGLE_DOWN, OctIconFactory::get),
    WATER(FontAwesomeIcon.TINT, FontAwesomeIconFactory::get);

    public static final String      DEFAULT_FONT_SIZE = "22";

    private GlyphIcons              glyphIcon;
    private Supplier<GlyphsFactory> factorySupplier;

    private Icon(GlyphIcons icon, Supplier<GlyphsFactory> factorySupplier) {
        glyphIcon = icon;
        this.factorySupplier = factorySupplier;
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
        return button(DEFAULT_FONT_SIZE);
    }

    public Button button(String size) {
        Text label = factorySupplier.get().createIcon(glyphIcon, size);
        Button button = new Button();
        button.setGraphic(label);
        return button;
    }

    public Button button(String text, String iconSize, String fontSize, ContentDisplay display) {
        return factorySupplier.get().createIconButton(glyphIcon, text, iconSize, fontSize, display);
    }

    public Text icon() {
        return factorySupplier.get().createIcon(glyphIcon);
    }

    public Text icon(String size) {
        return factorySupplier.get().createIcon(glyphIcon, size);
    }

    public StackPane circleIcon() {
        StackPane pneIcon = new StackPane(icon("26"));
        pneIcon.getStyleClass().add("icon-pane");
        return pneIcon;
    }

    public static class Buttons {

        private Buttons() {
        }

        public static Button add(EventHandler<ActionEvent> onAction) {
            return Icon.ADD.button(onAction);
        }

        public static Button edit(EventHandler<ActionEvent> onAction) {
            return Icon.EDIT.button(onAction);
        }

        public static Button save(EventHandler<ActionEvent> onAction) {
            return DONE.button(onAction);
        }

        public static Button saveAndContinue(EventHandler<ActionEvent> onAction) {
            return DONE_ALL.button(onAction);
        }

        public static Button delete(EventHandler<ActionEvent> onAction) {
            return DELETE.button(onAction);
        }

        public static Button back(EventHandler<ActionEvent> onAction) {
            Button button = new Button(com.gluonhq.charm.glisten.visual.MaterialDesignIcon.ARROW_BACK.text);
            button.setOnAction(onAction);
            return button;
        }

        public static Button filter(EventHandler<ActionEvent> onAction) {
            return FILTER.button(onAction);
        }

        public static Button refresh(EventHandler<ActionEvent> onAction) {
            Button button = Icon.REFRESH.button("20");
            button.setOnAction(onAction);
            return button;
        }

        public static Button actionBarButton(Icon icon, String text, EventHandler<ActionEvent> onAction) {
            Button btn = icon.button(text, "24", "12", ContentDisplay.TOP);
            btn.setGraphicTextGap(0);
            btn.setOnAction(onAction);
            return btn;
        }

        public static Button labeledCircleButton(Icon icon, String text, EventHandler<ActionEvent> onAction) {
            Button btn = new Button(text, icon.circleIcon());
            btn.getStyleClass().add("main-button");
            btn.setContentDisplay(ContentDisplay.TOP);
            btn.setGraphicTextGap(0);
            btn.setWrapText(true);
            btn.setOnAction(onAction);
            return btn;
        }

        public static StateButton<Void> search() {
            Node search = SEARCH.icon("20");
            Node clear = CLOSE.icon("20");
            return new StateButton<>(search, clear);
        }

        public static StateButton<AccessType> accessType() {
            Node locked = LOCK.icon(DEFAULT_FONT_SIZE);
            Node unlocked = UNLOCK.icon(DEFAULT_FONT_SIZE);
            return new StateButton<>(locked, unlocked);
        }

    }

}