package com.jns.orienteering.platform;

import com.gluonhq.charm.down.common.Position;

public class PositionHelper {

    private static final String   DEFAULT_POSITION_KEY = "default_position";
    private static final String   START_POSITION_KEY   = "start_position";

    private static final Position FRANKURT_AM_MAIN     = new Position(50.107180, 8.663756);

    public static Position retrieveDefaultPosition() {
        return retrieve(DEFAULT_POSITION_KEY, FRANKURT_AM_MAIN);
    }

    public static void storeDefaultPosition(Position position) {
        store(DEFAULT_POSITION_KEY, position);
    }

    public static void initStartPosition(Position position) {
        if (retrieve(START_POSITION_KEY, null) == null) {
            storeStartPosition(position);
        }
    }

    public static Position retrieveStartPosition() {
        return retrieve(START_POSITION_KEY, FRANKURT_AM_MAIN);
    }

    public static void storeStartPosition(Position position) {
        store(START_POSITION_KEY, position);
    }

    private static Position retrieve(String key, Position defaultValue) {
        String result = PlatformProvider.getPlatform().getSettingService().retrieve(key);
        if (result == null) {
            return defaultValue;
        }
        return toPosition(result);
    }

    private static void store(String key, Position position) {
        PlatformProvider.getPlatform().getSettingService().store(key, toPositionString(position));
    }

    public static Position toPosition(String positionText) {
        String text = positionText.replaceAll("\\s", "");
        String[] split = text.split(",");
        double latitude = Double.valueOf(split[0]);
        double longitude = Double.valueOf(split[1]);
        validateCoordinates(latitude, longitude);
        return new Position(latitude, longitude);
    }

    /**
     * Checkf if the gps coordinates are in valid range
     *
     * @param latitude
     *            in the range of [-90,90]
     * @param longitude
     *            int the range of [-180,180]
     * @throws IllegalArgumentException
     *             if the coordinates are invalid
     *
     */
    public static void validateCoordinates(double latitude, double longitude) {
        boolean isValid = latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
        if (!isValid) {
            throw new IllegalArgumentException("Not a valid location: " + latitude + ", " + longitude);
        }
    }

    public static String toPositionString(Position position) {
        return position.getLatitude() + ", " + position.getLongitude();
    }

}
