package edu.mit.media.obm.liveobjects.middleware.common;

/**
 * This class represent a live object discovered through the network.
 * The class is final to force immutability.
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public final class LiveObject {
    private final String mLiveObjectName;

    /**
     * Creates a new network device with specified id
     */
    public LiveObject(String liveObjectName) {
        if (liveObjectName == null)
            throw new NullPointerException();
        mLiveObjectName = liveObjectName;
    }

    /**
     * Get the id for this network device.
     *
     * @return Device's id
     */
    public String getLiveObjectName() {
        return mLiveObjectName;
    }


    @Override
    public String toString() {
        return mLiveObjectName;
    }
}
