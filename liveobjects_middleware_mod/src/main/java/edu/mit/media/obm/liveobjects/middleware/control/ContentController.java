package edu.mit.media.obm.liveobjects.middleware.control;

import android.os.RemoteException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

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
     * @param folder folder name
     * @return the content as InputStream
     */
    InputStream getInputStreamContent(String contentId, String folder) throws IOException, RemoteException;

    /**
     * Get the list of file names of a given directory
     * @param directoryName
     * @return the list of file names
     */
    List<String> getFileNamesOfADirectory(String directoryName);


    /**
     * Get the list of file names of a given directory
     * @param contentId
     * @param folder folder name
     * @return the size of the specified file
     */
    int getFileSize(String contentId, String folder) throws IOException, RemoteException;
}
