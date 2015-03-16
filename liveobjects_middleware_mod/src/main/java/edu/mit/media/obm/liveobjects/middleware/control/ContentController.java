package edu.mit.media.obm.liveobjects.middleware.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * This interface defines the basic functionality to access and add to a live object
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public interface ContentController {

    /**
     * Putting serializable content in a live object
     * @param contentId
     * @param content
     */
    void putSerializableContent(String contentId, Serializable content);

    void putStringContent(String contentId, String folder, String stringContent);


    /**
     * Get serializable content from the live-object
     * @param contentId
     * @return the content as Serializable
     */
    Serializable getSerializableContent(String contentId);


    /**
     * get the content as InputStream
     * @param contentId
     * @return the content as InputStream
     */
    InputStream getInputStreamContent(String contentId) throws IOException;


//TODO extending the interface with more functionality?





}
