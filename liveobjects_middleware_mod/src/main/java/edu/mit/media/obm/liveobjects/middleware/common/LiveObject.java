package edu.mit.media.obm.liveobjects.middleware.common;

/**
 * This class represent a live object discovered through the network.
 * The class is final to force immutability.
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public final class LiveObject {
    private final String mName;
    private final MapLocation mMapLocation;
    private int mStatus = STATUS_ACTIVE;
    private boolean mConnectedBefore = false;

    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_SLEEPING = 2;
    public static final int STATUS_LOST = 3;

    /**
     * Creates a new network device with specified id
     */
    public LiveObject(String liveObjectName) {
        if (liveObjectName == null) {
            throw new IllegalArgumentException();
        }

        mName = liveObjectName;
        mMapLocation = null;
    }

    /**
     * Creates a new network device with specified id
     */
    public LiveObject(String liveObjectName, MapLocation mapLocation) {
        if (liveObjectName == null || mapLocation == null) {
            throw new IllegalArgumentException("arguments cannot be null");
        }

        mName = liveObjectName;
        mMapLocation = mapLocation;
    }

    /**
     * Get the id for this network device.
     *
     * @return Device's id
     */
    public String getName() {
        return mName;
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
     * Get the status of this live object
     *
     * @return status (STATUS_XXX)
     */
    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }


    public boolean getConnectedBefore() {
        return mConnectedBefore;
    }

    public void setConnectedBefore(boolean connectedBefore) {
        mConnectedBefore = connectedBefore;
    }

    @Override
    public String toString() {
        String stringExpression = getName();

        if (getMapLocation() != null) {
            stringExpression += " " + getMapLocation();
        }

        return stringExpression;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof LiveObject)) {
            return false;
        }

        LiveObject liveObject = (LiveObject) object;

        if (!getName().equals(liveObject.getName())) {
            return false;
        }

        if (getMapLocation() == null && liveObject.getMapLocation() == null) {
            return true;
        } else if (getMapLocation() != null && liveObject.getMapLocation() != null) {
            return getMapLocation().equals(liveObject.getMapLocation());
        } else {
            return false;
        }
    }
}
