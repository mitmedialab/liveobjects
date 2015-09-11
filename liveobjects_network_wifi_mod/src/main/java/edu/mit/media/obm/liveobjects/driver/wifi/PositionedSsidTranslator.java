package edu.mit.media.obm.liveobjects.driver.wifi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.common.MapLocation;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;

/**
 * Created by arata on 8/5/15.
 */
public enum PositionedSsidTranslator implements DeviceIdTranslator {
    INSTANCE;

    private String SSID_PREFIX;
    private char SSID_DELIMITER;

    private Pattern SSID_PATTERN;

    private int LOCATION_X_LENGTH;
    private int LOCATION_Y_LENGTH;
    private int LOCATION_ID_LENGTH;

    @Override
    public boolean isLiveObject(String deviceId) {
        Matcher matcher = SSID_PATTERN.matcher(deviceId);
        return matcher.find();
    }

    @Override
    public LiveObject translateToLiveObject(String deviceId) {
        if (!isLiveObject(deviceId)) {
            throw new IllegalArgumentException("illegal deviceId '" + deviceId + "'");
        }

        Matcher matcher = SSID_PATTERN.matcher(deviceId);
        matcher.find();

        String name = matcher.group(1);
        int locationX = Integer.parseInt(matcher.group(2), 16);
        int locationY = Integer.parseInt(matcher.group(3), 16);
        int mapId = Integer.parseInt(matcher.group(4), 16);
        MapLocation mapLocation = new MapLocation(locationX, locationY, mapId);

        return new LiveObject(name, mapLocation);
    }

    @Override
    public String translateFromLiveObject(LiveObject liveObject) {
        MapLocation mapLocation = liveObject.getMapLocation();

        if (mapLocation == null) {
            throw new IllegalArgumentException("mapLocation cannot be null");
        }

        String formatString = String.format("%%s%%s%%c%%0%dx%%0%dx%%0%dx",
                LOCATION_X_LENGTH, LOCATION_Y_LENGTH, LOCATION_ID_LENGTH);
        String deviceId = String.format(formatString, SSID_PREFIX, liveObject.getLiveObjectName(),
                SSID_DELIMITER, mapLocation.getX(), mapLocation.getY(), mapLocation.getId());

        return deviceId;
    }

    protected final void setSsidFormat(String ssidPrefix, char ssidDelimiter,
            int locationXLength, int locationYLength, int locationIdLength) {
        SSID_PREFIX = ssidPrefix;
        SSID_DELIMITER = ssidDelimiter;
        LOCATION_X_LENGTH = locationXLength;
        LOCATION_Y_LENGTH = locationYLength;
        LOCATION_ID_LENGTH = locationIdLength;

        int maxSsidLength = 32 - (SSID_PREFIX.length() + /* delimiter length */1
                + LOCATION_X_LENGTH + LOCATION_Y_LENGTH + LOCATION_ID_LENGTH);

        String patternString = String.format(
                "^%s(.{1,%d})%c(\\p{XDigit}{%d})(\\p{XDigit}{%d})(\\p{XDigit}{%d})$",
                SSID_PREFIX, maxSsidLength, SSID_DELIMITER, LOCATION_X_LENGTH, LOCATION_Y_LENGTH, LOCATION_ID_LENGTH);
        SSID_PATTERN = Pattern.compile(patternString);
    }
}
