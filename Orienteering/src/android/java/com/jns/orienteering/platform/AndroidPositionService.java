/*
 * Copyright (c) 2015, 2016, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jns.orienteering.platform;

import java.util.List;

import com.gluonhq.charm.down.plugins.Position;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafxports.android.FXActivity;

public class AndroidPositionService implements PositionServiceExtended, LocationListener {

    private static final long              MIN_TIME         = 1000;                                                                                                 // 1
                                                                                                                                                                    // sec.
    private static final float             MIN_DISTANCE     = 5.0f;

    private final LocationManager          locationManager;
    private final String                   locationProvider;
    private AndroidLooperTask              looperTask;

    private final ObjectProperty<Position> positionProperty = new SimpleObjectProperty<>();

    public AndroidPositionService() {
        Context activityContext = FXActivity.getInstance();

        locationManager = (LocationManager) activityContext.getSystemService(FXActivity.LOCATION_SERVICE);

        List<String> locationProviders = locationManager.getAllProviders();
        if (locationProviders == null || locationProviders.isEmpty()) {
            locationProvider = LocationManager.GPS_PROVIDER;
        } else {
            if (locationProviders.contains(LocationManager.GPS_PROVIDER)) {
                locationProvider = LocationManager.GPS_PROVIDER;
            } else if (locationProviders.contains(LocationManager.NETWORK_PROVIDER)) {
                locationProvider = LocationManager.NETWORK_PROVIDER;
            } else if (locationProviders.contains(LocationManager.PASSIVE_PROVIDER)) {
                locationProvider = LocationManager.PASSIVE_PROVIDER;
            } else {
                locationProvider = locationProviders.get(0);
            }
        }

        boolean locationProviderEnabled = locationManager.isProviderEnabled(locationProvider);
        if (!locationProviderEnabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            activityContext.startActivity(intent);
        }

        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        if (lastKnownLocation != null) {
            Platform.runLater(() ->
            {
                Position lastKnownPosition = new Position(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                positionProperty.set(lastKnownPosition);
            });
        }
        createLooperTask();
    }

    private void createLooperTask() {
        looperTask = new AndroidLooperTask() {

            @Override
            public void execute() {
                locationManager.requestLocationUpdates(locationProvider, MIN_TIME, MIN_DISTANCE, AndroidPositionService.this);
            }
        };

        Thread thread = new Thread(looperTask);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (provider.equals(locationProvider) && looperTask == null) {
            createLooperTask();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(locationProvider) && looperTask != null) {
            looperTask.quit();
            looperTask = null;
        }
    }

    @Override
    public void activate() {
        if (looperTask == null) {
            createLooperTask();
        }
    }

    @Override
    public void deactivate() {
        if (looperTask != null) {
            locationManager.removeUpdates(this);
            looperTask.quitFromDifferentThread();
            looperTask = null;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Platform.runLater(() -> positionProperty.set(new Position(location.getLatitude(), location.getLongitude())));
        }
    }

    @Override
    public ReadOnlyObjectProperty<Position> positionProperty() {
        return positionProperty;
    }

    @Override
    public Position getPosition() {
        return positionProperty.get();
    }

    @Override
    public boolean isInRadius(Position currentPosition, Position targetPosition, double radius) {
        return getDistance(currentPosition, targetPosition) <= radius;
    }

    @Override
    public float getDistance(Position start, Position end) {
        return convertToLocation(start).distanceTo(convertToLocation(end));
    }

    private Location convertToLocation(Position position) {
        Location result = new Location(locationProvider);
        result.setLatitude(position.getLatitude());
        result.setLongitude(position.getLongitude());
        return result;
    }

}
