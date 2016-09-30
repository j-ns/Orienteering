package com.jns.orienteering.util;

import com.gluonhq.charm.down.common.PlatformFactory;
import com.gluonhq.charm.down.common.Position;

public class PositionHelper {

    private static final String   DEFAULT_POSITION_KEY = "default_position";
    private static final String   START_POSITION_KEY   = "start_position";

    private static final Position DEFAULT_POSITION     = new Position(50.107180, 8.663756);
    private static final Position START_POSITION       = new Position(52.547495, 13.384301);

    public static Position retrieveDefaultPosition() {
        return retrieve(DEFAULT_POSITION_KEY, DEFAULT_POSITION);
    }

    public static void storeDefaultPosition(Position position) {
        store(DEFAULT_POSITION_KEY, position);
    }

    public static Position retrieveStartPosition() {
        return retrieve(START_POSITION_KEY, START_POSITION);
    }

    public static void storeStartPosition(Position position) {
        store(START_POSITION_KEY, position);
    }

    private static Position retrieve(String key, Position defaultValue) {
        String result = PlatformFactory.getPlatform().getSettingService().retrieve(key);
        if (result == null) {
            return defaultValue;
        }
        return toPosition(result);
    }

    private static void store(String key, Position position) {
        PlatformFactory.getPlatform().getSettingService().store(key, toPositionString(position));
    }

    public static Position toPosition(String positionText) {
        String text = positionText.replaceAll("\\s", "");
        String[] split = text.split(",");
        double latitude = Double.valueOf(split[0]);
        double longitude = Double.valueOf(split[1]);
        return new Position(latitude, longitude);
    }

    public static String toPositionString(Position position) {
        return position.getLatitude() + ", " + position.getLongitude();
    }

}
