package edu.mit.media.obm.liveobjects.middleware.control;

import java.util.Map;

/**
 * Provides access to properties of a live object
 * At application level, its concrete class will define the schema of a given live object
 * and the methods to access the different properties.
 *
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public abstract class LiveObjectPropertyProvider {
    private Map<String, Object> liveObjectProperties;

    public LiveObjectPropertyProvider(Map<String, Object> liveObjectProperties){
        this.liveObjectProperties = liveObjectProperties;
    }

    public Map<String, Object> getLiveObjectProperties() {
        return liveObjectProperties;
    }
}
