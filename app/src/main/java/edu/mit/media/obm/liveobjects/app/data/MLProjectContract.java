package edu.mit.media.obm.liveobjects.app.data;

import edu.mit.media.obm.liveobjects.middleware.control.LiveObjectContract;

/**
 * Contract that defines the properties of a live object representing a Media Lab project
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class MLProjectContract implements LiveObjectContract {

    public static final String CONFIG = "media-config";

    public static final String PROJECT_TITLE = "title";
    public static final String GROUP = "group";
    public static final String PROJECT_URL = "website";
    public static final String PROJECT_DESCRIPTION = "description";
    public static final String ICON = "icon";

    public static final String MEDIA = "media";
    public static final String MEDIA_TYPE = "type";
    public static final String MEDIA_FILENAME = "filename";


}
