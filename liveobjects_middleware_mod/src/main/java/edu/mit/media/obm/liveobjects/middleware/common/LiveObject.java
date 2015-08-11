package edu.mit.media.obm.liveobjects.middleware.common;

import java.util.Map;

/**
 * This class represent a live object discovered through the network.
 * The class is final to force immutability.
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public final class LiveObject {
    private final String mLiveObjectName;
    private final MapLocation mMapLocation;
    private boolean mIsActive = true;

    /**
     * Creates a new network device with specified id
     */
    public LiveObject(String liveObjectName) {
        if (liveObjectName == null) {
            throw new NullPointerException();
        }

        mLiveObjectName = liveObjectName;
        mMapLocation = null;
    }

    /**
     * Creates a new network device with specified id
     */
    public LiveObject(String liveObjectName, MapLocation mapLocation) {
        if (liveObjectName == null || mapLocation == null) {
            throw new NullPointerException();
        }

        mLiveObjectName = liveObjectName;
        mMapLocation = mapLocation;
    }

    /**
     * Get the id for this network device.
     *
     * @return Device's id
     */
    public String getLiveObjectName() {
        return mLiveObjectName;
    }

    /**
     * Get the location information of this network device.
     *
     * @return Device's location information. It can be null if no location is set.
     */
    public MapLocation getMapLocation() {
        return mMapLocation;
    }

    /**
     * Get a boolean value which indicates if the network device is active or sleeping.
     *
     * @return true if the network device is not sleeping
     */
    public boolean isActive() {
        return mIsActive;
    }

    public void setActive(boolean active) {
        mIsActive = active;
    }

    @Override
    public String toString() {
        return mLiveObjectName;
    }
}
