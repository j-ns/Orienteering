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
package com.jns.orienteering.platform;

import com.gluonhq.charm.down.plugins.Position;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Duration;

public class FakeDesktopPositionService implements PositionServiceExtended {

    private ObjectProperty<Position> positionProperty;
    private Timeline                 timeline;

    private final Position           startPosition;

    FakeDesktopPositionService(Position startPosition) {
        this.startPosition = startPosition;
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public ReadOnlyObjectProperty<Position> positionProperty() {
        if (positionProperty == null) {
            positionProperty = new SimpleObjectProperty<Position>(startPosition);

            timeline = new Timeline();
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), e -> movePosition()));
            timeline.setCycleCount(Integer.MAX_VALUE);
            timeline.play();
        }
        return positionProperty;
    }

    private void movePosition() {
        Position position = positionProperty.get();
        Position newPosition = new Position(position.getLatitude() - .0001, position.getLongitude() - .000140);
        positionProperty.set(newPosition);
    }

    @Override
    public float getDistance(Position start, Position end) {
        return 0;
    }

    @Override
    public boolean isInRadius(Position currentPosition, Position targetPosition, double radius) {
        double distanceX = currentPosition.getLatitude() - targetPosition.getLatitude();
        double distanceY = currentPosition.getLongitude() - targetPosition.getLongitude();
        return distanceX < 0.0001 && distanceX > 0 && distanceY < 0.002 && distanceY > 0;
    }

    @Override
    public void activate() {
        timeline.play();
    }

    @Override
    public void deactivate() {
        timeline.stop();
    }


}
