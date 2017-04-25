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

import java.util.Optional;

import com.jns.orienteering.util.Trigger;

import javafx.scene.Node;
import javafx.scene.control.ToggleButton;

public class StateButton<T> extends ToggleButton {

    private Node                     unselectedIcon;
    private Node                     selectedIcon;

    private Optional<SelectState<T>> selectState;

    public StateButton(Node unselectedIcon, Node selectedIcon) {
        this(unselectedIcon, selectedIcon, null);
    }

    public StateButton(Node unselectedIcon, Node selectedIcon, SelectState<T> selectState) {
        super();
        this.unselectedIcon = unselectedIcon;
        this.selectedIcon = selectedIcon;
        this.selectState = Optional.ofNullable(selectState);
        this.selectState.ifPresent(st -> st.setSelected(false));
        setGraphic(unselectedIcon);
    }

    public SelectState<T> getSelectState() {
        return selectState.orElse(null);
    }

    public void setSelectState(SelectState<T> selectState) {
        this.selectState = Optional.ofNullable(selectState);
        this.selectState.ifPresent(st -> st.setSelected(isSelected()));
        setGraphic(isSelected() ? selectedIcon : unselectedIcon);
    }

    public void setOnAction(Trigger onAction) {
        selectedProperty().addListener((obsValue, b, b1) ->
        {
            setGraphic(b1 ? selectedIcon : unselectedIcon);
            selectState.ifPresent(state -> state.setSelected(b1));
            onAction.start();
        });
    }

    public void setOnAction(Trigger onSelected, Trigger onUnselected) {
        selectedProperty().addListener((obsValue, b, b1) ->
        {
            selectState.ifPresent(state -> state.setSelected(b1));
            if (b1) {
                setGraphic(selectedIcon);
                onSelected.start();
            } else {
                setGraphic(unselectedIcon);
                onUnselected.start();
            }
        });
    }

}
