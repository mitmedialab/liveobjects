package edu.mit.media.obm.liveobjects.middleware.control;

import android.os.RemoteException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import edu.mit.media.obm.liveobjects.middleware.common.ContentId;

/**
 * This interface defines the basic functionality to get and add content to a live object
 *
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public interface ContentController {

    /**
     * Put a String content in a live object
     * @param contentId content id
     * @param stringContent
     */
    void putStringContent(ContentId contentId, String stringContent);


    /**
     * get the content as InputStream
     * @param contentId
     * @return the content as InputStream
     */
    InputStream getInputStreamContent(ContentId contentId) throws IOException, RemoteException;

    /**
     * Get the list of file names of a given directory
     * @param liveObjectId the id of the live-object
     * @param directoryName the directory path
     * @return the list of file names
     */
    List<String> getFileNamesOfADirectory(String liveObjectId, String directoryName);


    /**
     * Returns the size of a given content
     * @param contentId
     * @return the size of the specified file
     */
    int getContentSize(ContentId contentId) throws IOException, RemoteException;
}
