package edu.mit.media.obm.liveobjects.driver.wifi.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.common.MapLocation;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;

/**
 * Created by arata on 8/5/15.
 */
public class PositionedSsidTranslator implements DeviceIdTranslator {
    private final String ssidPrefix;
    private final char ssidDelimiter;

    private final Pattern ssidPattern;

    private final int locationXLength;
    private final int locationYLength;
    private final int locationIdLength;

    public PositionedSsidTranslator(String ssidPrefix, char ssidDelimiter,
                                    int locationXLength, int locationYLength, int locationIdLength) {
        this.ssidPrefix = ssidPrefix;
        this.ssidDelimiter = ssidDelimiter;
        this.locationXLength = locationXLength;
        this.locationYLength = locationYLength;
        this.locationIdLength = locationIdLength;

        String patternString = String.format(
                "^%s(.{1,%d})%c(\\p{XDigit}{%d})(\\p{XDigit}{%d})(\\p{XDigit}{%d})$",
                this.ssidPrefix, getMaxNameLength(), this.ssidDelimiter, this.locationXLength, this.locationYLength, this.locationIdLength);
        ssidPattern = Pattern.compile(patternString);
    }

    @Override
    public boolean isValidSsid(String deviceId) {
        Matcher matcher = ssidPattern.matcher(deviceId);
        return matcher.find();
    }

    @Override
    public boolean isValidLiveObject(LiveObject liveObject) {
        int nameLength = liveObject.getLiveObjectName().length();

        if (nameLength < 1 || nameLength > getMaxNameLength()) {
            return false;
        }

        MapLocation mapLocation = liveObject.getMapLocation();

        if (mapLocation == null) {
            return true;
        }

        if (!isInHexLengthRange(mapLocation.getX(), locationXLength)
                || !isInHexLengthRange(mapLocation.getY(), locationYLength)
                || !isInHexLengthRange(mapLocation.getId(), locationIdLength)) {
            return false;
        }

        return true;
    }

    private boolean isInHexLengthRange(int value, int hexLength) {
        int maxValue = maxValueFromHexLength(hexLength);

        return (0 <= value && value <= maxValue);
    }

    private int maxValueFromHexLength(int hexLength) {
        return (1 << (hexLength * 4)) - 1;
    }

    @Override
    public LiveObject translateToLiveObject(String deviceId) {
        if (!isValidSsid(deviceId)) {
            throw new IllegalArgumentException("illegal deviceId '" + deviceId + "'");
        }

        Matcher matcher = ssidPattern.matcher(deviceId);
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
        if (!isValidLiveObject(liveObject)) {
            throw new IllegalArgumentException("illegal live object");
        }

        MapLocation mapLocation = liveObject.getMapLocation();
        String formatString = String.format("%%s%%s%%c%%0%dx%%0%dx%%0%dx",
                locationXLength, locationYLength, locationIdLength);
        String ssid = String.format(formatString, ssidPrefix, liveObject.getLiveObjectName(),
                ssidDelimiter, mapLocation.getX(), mapLocation.getY(), mapLocation.getId());

        return ssid;
    }

    private int getMaxNameLength() {
        final int maxSsidLength = 32;

        return maxSsidLength - (this.ssidPrefix.length() + /* delimiter length */1
                + this.locationXLength + this.locationYLength + this.locationIdLength);
    }
}
