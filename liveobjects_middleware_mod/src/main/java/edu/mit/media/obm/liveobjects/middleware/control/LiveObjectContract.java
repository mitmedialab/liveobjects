package edu.mit.media.obm.liveobjects.middleware.control;

/**
 * Generic interface used to define the properties of a given live object.
 * A class, at the application level, implementing this interface will contain the keys
 * to access the different properties of a live object managed by DbController.
 * @see DbController
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public interface LiveObjectContract {

    /**
     * The unique ID for a live object
     *
     */
    public static final String _ID = "_id";

}
