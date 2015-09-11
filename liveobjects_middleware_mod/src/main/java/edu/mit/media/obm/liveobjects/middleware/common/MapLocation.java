package edu.mit.media.obm.liveobjects.middleware.common;

/**
 * Created by arata on 8/5/15.
 */
public class MapLocation {
    private final int x;
    private final int y;
    private final int id;

    public static final int LENGTH_IN_HEX_X = 2;
    public static final int LENGTH_IN_HEX_Y = 2;
    public static final int LENGTH_IN_HEX_ID = 1;

    public MapLocation(int x, int y, int id) {
        throwIfOutOfRange("x", x, LENGTH_IN_HEX_X);
        throwIfOutOfRange("y", y, LENGTH_IN_HEX_Y);
        throwIfOutOfRange("id", id, LENGTH_IN_HEX_ID);

        this.x = x;
        this.y = y;
        this.id = id;
    }

    private static void throwIfOutOfRange(String fieldName, int value, int lengthInHex) {
        if (!isInRange(value, lengthInHex)) {
            String message = String.format("%s is out of %d bits range", fieldName, lengthInHex * 4);
            throw new IllegalArgumentException(message);
        }
    }

    private static boolean isInRange(int value, int lengthInNibles) {
        int minValue = 0;
        int maxValue = 1 << (lengthInNibles * 4);

        return (minValue <= value && value < maxValue);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d, %d)", getX(), getY(), getId());
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MapLocation)) {
            return false;
        }

        MapLocation mapLocation = (MapLocation) object;

        return (getX() == mapLocation.getX() &&
                getY() == mapLocation.getY() &&
                getId() == mapLocation.getId());
    }
}

