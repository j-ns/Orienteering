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

import java.util.List;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import com.jns.orienteering.util.Icon;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.util.Pair;

public class LocationLayer extends MapLayer {

    private final ObservableList<Pair<MapPoint, Node>> points = FXCollections.observableArrayList();

    public LocationLayer() {
        getStyleClass().add("location-layer");
    }

    public ObservableList<Pair<MapPoint, Node>> getPoints() {
        return points;
    }

    public MapPoint getFirstPoint() {
        return isNullOrEmpty(points) ? null : points.get(0).getKey();
    }

    public Node setPoint(MapPoint point) {
        removeAll();
        return addPoint(point);
    }

    public Node setMovingPoint(MapPoint point) {
        removeAll();
        Node marker = Icon.MAP_MARKER_CIRCLE.icon("24");
        return addPoint(point, marker);
    }

    public void setPoints(List<MapPoint> points) {
        this.points.clear();
        getChildren().clear();
        addPoints(points);
    }

    public Node addPoint(MapPoint point) {
        return addPoint(point, createMarker());
    }

    private Node addPoint(MapPoint point, Node marker) {
        points.add(new Pair<>(point, marker));
        getChildren().add(marker);
        markDirty();
        return marker;
    }

    public void addPoints(List<MapPoint> points) {
        for (MapPoint point : points) {
            Node marker = createMarker();
            this.points.add(new Pair<>(point, marker));
            getChildren().add(marker);
        }
        markDirty();
    }

    public void removePoint(MapPoint point) {
        for (int idx = 0; idx < points.size(); idx++) {
            MapPoint _point = points.get(idx).getKey();

            if (pointsEqual(point, _point)) {
                getChildren().remove(points.get(idx).getValue());
                points.remove(idx);
                markDirty();
                break;
            }
        }
    }

    private boolean pointsEqual(MapPoint point, MapPoint _point) {
        return point.getLongitude() == _point.getLongitude() && point.getLatitude() == _point.getLatitude();
    }

    public void removeAll() {
        points.clear();
        getChildren().clear();
        markDirty();
    }

    private Node createMarker() {
        return Icon.MAP_MARKER.icon("24");
    }

    public void refresh() {
        markDirty();
    }

    @Override
    protected void layoutLayer() {
        for (Pair<MapPoint, Node> candidate : points) {
            MapPoint point = candidate.getKey();
            Node icon = candidate.getValue();
            Point2D mapPoint = baseMap.getMapPoint(point.getLatitude(), point.getLongitude());
            icon.setVisible(true);
            icon.setTranslateX(mapPoint.getX() - icon.getLayoutBounds().getWidth() * 0.5);
            icon.setTranslateY(mapPoint.getY() - icon.getLayoutBounds().getWidth() * 0.5);
        }
    }

}
