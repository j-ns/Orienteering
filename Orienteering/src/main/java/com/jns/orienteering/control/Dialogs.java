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

import static com.jns.orienteering.locale.Localization.localize;

import java.util.Optional;

import com.gluonhq.charm.glisten.control.Dialog;
import com.jns.orienteering.util.Calculations;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

public class Dialogs {

    private Dialogs() {
    }

    public static Dialog<ButtonType> ok(Message message) {
        String title = message.getTitle();

        Node lblTitle = title == null ? null : new Label(title);
        Node content = message.getText() == null ? contentPlaceHolder(title) : new Label(message.getText());

        return ok(lblTitle, content);
    }

    public static void showError(String title) {
        ok(title).showAndWait();
    }

    public static void showError(Message message) {
        ok(message).showAndWait();
    }

    public static void showInfo(String title) {
        ok(title).showAndWait();
    }

    public static void showInfo(Message message) {
        ok(message).showAndWait();
    }

    public static Dialog<ButtonType> ok(String title) {
        return ok(new Label(title), contentPlaceHolder(title));
    }

    public static Dialog<ButtonType> ok(String title, String text) {
        Node lblTitle = title == null ? null : new Label(title);
        Node content = text == null ? contentPlaceHolder(title) : new Label(text);
        return ok(lblTitle, content);
    }

    public static Dialog<ButtonType> ok(Node title, Node content) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setContent(content);

        Button ok = new Button("OK");
        ok.setOnAction(e -> dialog.hide());
        dialog.getButtons().add(ok);
        return dialog;
    }

    public static DialogAnswer cancelOkAnswer(String title, String cancelText, String okText) {
        return new DialogAnswer(cancelOk(title, cancelText, okText));
    }

    public static Dialog<ButtonType> cancelOk(String title, String cancelText, String okText) {
        return cancelOk(new Label(title), contentPlaceHolder(title), cancelText, okText);
    }

    public static Dialog<ButtonType> cancelOk(Node title, Node content, String cancelText, String okText) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setContent(content);

        Button btnCancel = new Button(cancelText);
        btnCancel.setOnAction(e ->
        {
            dialog.setResult(ButtonType.CANCEL);
            dialog.hide();
        });

        Button btnOk = new Button(okText);
        btnOk.setOnAction(e ->
        {
            dialog.setResult(ButtonType.OK);
            dialog.hide();
        });

        dialog.getButtons().setAll(btnCancel, btnOk);
        return dialog;
    }

    public static DialogAnswer confirmDeleteAnswer(String title) {
        return new DialogAnswer(confirmDelete(title));
    }

    public static Dialog<ButtonType> confirmDelete(String title) {
        return cancelOk(title, localize("button.cancel"), localize("button.delete"));
    }

    public static boolean answerIsYes(Dialog<ButtonType> dialog) {
        return new DialogAnswer(dialog).isYesOrOk();
    }

    private static Label contentPlaceHolder(String title) {
        Label label = new Label();
        label.setPrefWidth(Math.max(Calculations.textWidth(title), 280));
        label.setMinHeight(16);
        label.setPrefHeight(16);
        return label;
    }

    public static class DialogAnswer {
        private Dialog<ButtonType> dialog;

        public DialogAnswer(Dialog<ButtonType> dialog) {
            this.dialog = dialog;
        }

        public boolean isYesOrOk() {
            Optional<ButtonType> answer = dialog.showAndWait();
            return answer.isPresent() ? answer.get() == ButtonType.YES || answer.get() == ButtonType.OK : false;
        }
    }

}
