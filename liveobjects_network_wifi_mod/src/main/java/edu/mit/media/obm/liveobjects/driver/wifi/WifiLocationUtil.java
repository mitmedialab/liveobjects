package edu.mit.media.obm.liveobjects.driver.wifi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.common.MapLocation;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkUtil;

/**
 * Created by arata on 8/5/15.
 */
public enum WifiLocationUtil implements NetworkUtil {
    INSTANCE;

    private String SSID_PREFIX;
    private char SSID_DELIMITER;

    private int LOCATION_COORDINATE_X_LENGTH;
    private int LOCATION_COORDINATE_Y_LENGTH;
    private int LOCATION_MAP_ID_LENGTH;

    private Pattern SSID_PATTERN;

    @Override
    public boolean isLiveObject(String deviceId) {
        Matcher matcher = SSID_PATTERN.matcher(deviceId);
        return matcher.find();
    }

    @Override
    public LiveObject convertDeviceIdToLiveObject(String deviceId) {
        Matcher matcher = SSID_PATTERN.matcher(deviceId);

        if (!matcher.find()) {
            throw new RuntimeException("illegal deviceId '" + deviceId + "'");
        }

        String name = matcher.group(1);
        int locationX = Integer.parseInt(matcher.group(2), 16);
        int locationY = Integer.parseInt(matcher.group(3), 16);
        int mapId = Integer.parseInt(matcher.group(4), 16);
        MapLocation mapLocation = new MapLocation(locationX, locationY, mapId);

        return new LiveObject(name, mapLocation);
    }

    @Override
    public String convertLiveObjectToDeviceId(LiveObject liveObject) {
        MapLocation mapLocation = liveObject.getMapLocation();
        String locationX = Integer.toHexString(mapLocation.getX());
        String locationY = Integer.toHexString(mapLocation.getY());
        String mapId = Integer.toHexString(mapLocation.getId());

        String deviceId = SSID_PREFIX + liveObject.getLiveObjectName() + SSID_DELIMITER +
                locationX + locationY + mapId;
        return deviceId;
    }

    protected final void setSsidFormat(String ssidPrefix, char ssidDelimiter,
            int locationCoordinateXLength, int locationCoordinateYLength, int locationMapIdLength) {
        SSID_PREFIX = ssidPrefix;
        SSID_DELIMITER = ssidDelimiter;
        LOCATION_COORDINATE_X_LENGTH = locationCoordinateXLength;
        LOCATION_COORDINATE_Y_LENGTH = locationCoordinateYLength;
        LOCATION_MAP_ID_LENGTH = locationMapIdLength;

        String patternString = SSID_PREFIX + "(.*)" + SSID_DELIMITER
                + "(\\p{XDigit}{" + LOCATION_COORDINATE_X_LENGTH + "})"
                + "(\\p{XDigit}{" + LOCATION_COORDINATE_Y_LENGTH + "})"
                + "(\\p{XDigit}{" + LOCATION_MAP_ID_LENGTH + "})";
        SSID_PATTERN = Pattern.compile(patternString);
    }
}
