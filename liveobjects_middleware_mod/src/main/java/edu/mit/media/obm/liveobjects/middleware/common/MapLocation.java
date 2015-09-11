package edu.mit.media.obm.liveobjects.middleware.common;

/**
 * Created by arata on 8/5/15.
 */
public class MapLocation {
    private final int x;
    private final int y;
    private final int id;

    public MapLocation(int x, int y, int id) {
        this.x = x;
        this.y = y;
        this.id = id;
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

