package edu.mit.media.obm.liveobjects.middleware.net;

/**
 * This interface provides a set of utilities method each network driver has to implement
 *
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public interface NetworkUtil {

    /**
     * Checks if the network device is a live object
     * @param deviceId the network device id to check
     * @return true if the network device is a live object
     */
    boolean isLiveObject(String deviceId);

    /**
     * Converts a device id into live object name
     * @param deviceId the id to convert
     * @return the live object name
     */
    String convertDeviceIdToLiveObjectName(String deviceId);

    /**
     * Converts a live object name into a device id
     * @param liveObjectName
     * @return
     */
    String convertLiveObjectNameToDeviceId(String liveObjectName);
}
