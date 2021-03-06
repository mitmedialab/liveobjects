package edu.mit.media.obm.liveobjects.middleware.control;

        import java.util.List;
import java.util.Map;

/**
 * This controller provides access to live objects, and their property locally stored in a database.
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public interface DbController extends Controller {

    /**
     * Get the properties of a live object
     * @param liveObjectId live object id
     * @return a map with all properties.
     * @return an empty map if the live object is empty.
     */
    Map<String, Object> getProperties(String liveObjectId);

    /**
     * Get a single property of a live object.
     * Equivalent to getProperties(liveObjectId).get(key)
     * @param liveObjectId the live object id
     * @param key the key of the property
     * @return a property of a live object
     */
    Object getProperty(String liveObjectId, String key);

    /**
     * Put a single property of a live object.
     * @param liveObjectId the live object id
     * @param key the key of the property
     * @param value the value of the property
     *
     */
    void putProperty(String liveObjectId, String key, Object value);

    /**
     * Get all liveObjectsIds
     * @return the list of all live object ids available in the db
     */
    List<String> getLiveObjectsIds();

    /**
     * Get the list of all live objects' properties
     * @return the list of all live objects' properties
     */
    List<Map<String, Object>> getAllLiveObjectsProperties();


    //TODO adding views or query to filter live objects?

    /**
     * Put a live object in the database.
     * If the live object already exists, it is updated with the new properties
     * @param liveObjectId the live object id
     * @param properties properties to insert
     */
    void putLiveObject(String liveObjectId, Map<String, Object> properties);

    /**
     * Is the live object does not contains property
     * @param liveObjectId
     * @return
     */
    boolean isLiveObjectEmpty(String liveObjectId);

}
