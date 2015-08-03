package edu.mit.media.obm.liveobjects.middleware.common;

/**
 * This class represent a live object discovered through the network.
 * The class is final to force immutability.
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public final class LiveObject {
    private final String mLiveObjectName;
    private final boolean mIsActive;

    /**
     * Creates a new network device with specified id
     */
    public LiveObject(String liveObjectName, boolean isActive) {
        if (liveObjectName == null) {
            throw new NullPointerException();
        }

        mLiveObjectName = liveObjectName;
        mIsActive = isActive;
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
     * Get a boolean value which indicates if the network device is active or sleeping.
     *
     * @return true if the network device is not sleeping
     */
    public boolean isActive() {
        return mIsActive;
    }


    @Override
    public String toString() {
        return mLiveObjectName;
    }
}
