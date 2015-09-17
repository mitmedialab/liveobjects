package edu.mit.media.obm.liveobjects.middleware.net;

import java.util.IllegalFormatException;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;

/**
 * This interface provides a set of utilities method each network driver has to implement
 *
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public interface DeviceIdTranslator {

    /**
     * Checks if the network device is a live object
     * @param deviceId the network device id to check
     * @return true if the network device is a live object
     */
    boolean isValidSsid(String deviceId);

    /**
     * Checks if the live object instance is a live object
     * @param liveObject the live object instance to check
     * @return true if the live object instance is a live object
     */
    boolean isValidLiveObject(LiveObject liveObject);

    /**
     * Converts a device id into live object name
     * @param deviceId the id to convert
     * @return the live object
     */
    LiveObject translateToLiveObject(String deviceId) throws IllegalFormatException;

    /**
     * Converts a live object into a device id
     * @param liveObject
     * @return
     */
    String translateFromLiveObject(LiveObject liveObject);
}
