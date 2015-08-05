package edu.mit.media.obm.liveobjects.middleware.common;

/**
 * Created by arata on 8/5/15.
 */
public class MapLocation {
    private final int mCoordinateX;
    private final int mCoordinateY;
    private final int mMapId;

    public MapLocation(int coordinateX, int coordinateY, int mapId) {
        mCoordinateX = coordinateX;
        mCoordinateY = coordinateY;
        mMapId = mapId;
    }

    public int getCoordinateX() {
        return mCoordinateX;
    }

    public int getCoordinateY() {
        return mCoordinateY;
    }

    public int getMapId() {
        return mMapId;
    }

}
