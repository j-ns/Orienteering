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
package com.jns.orienteering.control.cell;

import java.util.function.Function;

import com.jns.orienteering.control.ChoiceMenu;
import com.jns.orienteering.control.SelectionModelBase;

import javafx.scene.control.ListCell;
import javafx.scene.input.MouseEvent;

public class ChoiceCell<T> extends ListCell<T> {

    private SelectionModelBase<T> selectionModel;
    private Function<T, String>   stringConverter;

    public ChoiceCell(ChoiceMenu<T> choiceMenu) {
        selectionModel = choiceMenu.getSelectionModel();
        stringConverter = choiceMenu.getStringConverter();

        addEventHandler(MouseEvent.MOUSE_CLICKED, e -> selectItem());
    }

    private void selectItem() {
        T item = getItem();
        T previousItem = selectionModel.getSelectedItem();

        if (item != null && previousItem == item) {
            lviewSelectionModel().clearSelection();
            selectionModel.clearSelection();
            return;
        }

        selectionModel.select(item);
    }

    private javafx.scene.control.MultipleSelectionModel<T> lviewSelectionModel() {
        return getListView().getSelectionModel();
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null && !empty) {
            setText(stringConverter.apply(item));
        } else {
            setText(null);
        }
    }

}
