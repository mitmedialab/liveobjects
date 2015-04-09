package edu.mit.media.obm.liveobjects.middleware.storage;

import android.os.RemoteException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public interface StorageDriver {

    /**
     * Write a file using the content of the specified stream. If the file
     * exists, it is replaced. If the file does not exist, it is created.
     *
     * @param fileName Name of the file
     * @param stream   Stream to be put into the file
     * @throws java.io.IOException if there was an error while writing the file
     */
    void writeNewRawFileFromStream(String fileName, OutputStream stream) throws IOException;

    /**
     * Write a file using the content of the specified string.
     * If the file exists, it is replaced.
     * If the file does not exist, it is created.
     *
     * @param fileName   name of the file
     * @param folderName name of the folder to be put into the file
     * @param bodyString text to be contained in the file
     * @throws java.io.IOException if there was an error while writing the file
     */
    void writeNewRawFileFromString(String fileName, String folderName, String bodyString) throws IOException;


    /**
     * Create an input stream associated with the file. It will be used to read
     * the file.
     *
     * @param fileName Name of the file
     * @param folder   the folder where file is stored
     * @return InputStream associated with the file
     * @throws IOException if the file was not found or the stream cannot be created
     */
    InputStream getInputStreamFromFile(String fileName, String folder) throws IOException, RemoteException;


    /**
     * Get the file in the form of byte array
     *
     * @param filename
     * @return the byte array representation of the file
     * @throws IOException
     */
    byte[] getByteArrayFromFile(String filename) throws IOException, RemoteException;

    /**
     * Get the number of files in a certain storage
     *
     * @return the number of files
     */
    int getNumberOfFiles();

    /**
     * Checks if the file with @param filename exists in the storage
     *
     * @return true if the file exists
     */
    boolean isFileExisting(String filename);

    /**
     * Get the list of file names of a given directory
     *
     * @param directoryName
     * @return the list of file names
     */
    List<String> getFileNamesOfADirectory(String directoryName);

    /**
     * @param fileName Name of the file
     * @param folder   the folder where file is stored
     * @return the size of the specified size
     * @throws IOException
     * @throws RemoteException
     */
    int getFileSize(String fileName, String folder) throws IOException, RemoteException;
}
