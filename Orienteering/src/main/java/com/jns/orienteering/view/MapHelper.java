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
package com.jns.orienteering.view;

import static com.jns.orienteering.util.Validators.isNullOrEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.gluonhq.charm.down.common.Position;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import com.jns.orienteering.control.ScrollListener;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.util.Icon;

import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Button;

public class MapHelper {

    public static final Position     DEFAULT_START_POSITION        = new Position(50.107180, 8.663756);                                                                                                             // Frankfurt

    private static final PseudoClass PSEUDO_CLASS_MARKER_ACTIVE    = PseudoClass.getPseudoClass("active");
    private static final PseudoClass PSEUDO_CLASS_MARKER_COMPLETED = PseudoClass.getPseudoClass("completed");

    private ScrollListener           scrollListener;

    private MapView                  map;
    private LocationLayer            locationLayer                 = new LocationLayer();
    private LocationLayer            currentLocationLayer          = new LocationLayer();

    private Node                     activeMarker;

    public MapHelper(MapView map) {
        this(map, DEFAULT_START_POSITION);
    }

    public MapHelper(MapView map, Position position) {
        this.map = map;
        Position _position = position != null ? position : DEFAULT_START_POSITION;

        map.addLayer(locationLayer);
        map.addLayer(currentLocationLayer);
        map.setCenter(_position.getLatitude(), _position.getLongitude());
        map.setZoom(3);

        scrollListener = new ScrollListener(map);
        scrollListener.activate();
    }

    public void addScrollListener() {
        scrollListener = new ScrollListener(map);
        scrollListener.activate();
    }

    public void addMarker(Task task, boolean previousTaskCompleted) {
        if (activeMarker != null) {
            activeMarker.pseudoClassStateChanged(PSEUDO_CLASS_MARKER_ACTIVE, false);
            activeMarker.pseudoClassStateChanged(PSEUDO_CLASS_MARKER_COMPLETED, previousTaskCompleted);
        }
        if (task != null) {
            activeMarker = locationLayer.addPoint(task.getMapPoint());
            activeMarker.pseudoClassStateChanged(PSEUDO_CLASS_MARKER_ACTIVE, true);
        }
    }

    public void setFirstMarker(Task task) {
        activeMarker = locationLayer.setPoint(task.getMapPoint());
        activeMarker.pseudoClassStateChanged(PSEUDO_CLASS_MARKER_ACTIVE, true);
        centerMap(task.getPosition());
    }

    public void setMarkers(List<Task> tasks) {
        if (isNullOrEmpty(tasks)) {
            clearMarkers();
            return;
        }
        List<MapPoint> points = new ArrayList<>();
        for (Task task : tasks) {
            points.add(task.getMapPoint());
        }
        locationLayer.setPoints(points);
        centerMap(tasks.get(0).getPosition());
    }

    public void removeMarker(Task task) {
        locationLayer.removePoint(task.getMapPoint());
    }

    public void clearMarkers() {
        locationLayer.removeAll();
    }

    public void centerFirstMapPoint() {
        MapPoint point = locationLayer.getFirstPoint();
        if (point != null) {
            centerMap(new Position(point.getLatitude(), point.getLongitude()));
        }
    }

    public void centerMap(Position position) {
        if (position == null) {
            return;
        }
        map.setCenter(position.getLatitude() + .00001, position.getLongitude() + .00001);
        map.setCenter(position.getLatitude(), position.getLongitude());
        map.setZoom(14);
    }

    public void updateCurrentLocation(Position position) {
        currentLocationLayer.setMovingPoint(new MapPoint(position.getLatitude(), position.getLongitude()));
    }

    public Button createCenterLocationButton(Supplier<Position> position) {
        Button btn = Icon.GPS_LOCATION.button("18");
        btn.setOnAction(e -> centerMap(position.get()));
        return btn;
    }

    public Button createCenterMarkerButton(Supplier<Position> position) {
        Button btn = Icon.MAP_MARKER.button("18");
        btn.setOnAction(e -> centerMap(position.get()));
        return btn;
    }
}