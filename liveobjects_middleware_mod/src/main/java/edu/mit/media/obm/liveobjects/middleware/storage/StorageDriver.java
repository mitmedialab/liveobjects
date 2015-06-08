package edu.mit.media.obm.liveobjects.middleware.storage;

import android.os.RemoteException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public interface StorageDriver {


    /**
     * Write a file using the content of the specified string.
     * If the file exists, it is replaced.
     * If the file does not exist, it is created.
     *
     * @param filePath  relative path of the file
     * @param bodyString text to be contained in the file
     * @throws java.io.IOException if there was an error while writing the file
     */
    void writeNewRawFileFromString(String filePath, String bodyString) throws IOException;


    /**
     * Write a file using the content of the specified byte array.
     * If the file exists, it is replaced.
     * If the file does not exist, it is created.
     *
     * @param filePath  relative path of the file
     * @param byteArray content of the file
     * @throws java.io.IOException if there was an error while writing the file
     */
    void writeNewRawFileFromByteArray(String filePath, byte[] byteArray) throws IOException;

    /**
     * Write a file using the content of the specified byte array.
     * If the file exists, it is replaced.
     * If the file does not exist, it is created.
     *
     * @param filePath  relative path of the file
     * @param inputStream content of the file
     * @throws java.io.IOException if there was an error while writing the file
     */
    void writeNewRawFileFromInputStream(String filePath, InputStream inputStream) throws IOException;


    /**
     * Create an input stream associated with the file. It will be used to read
     * the file.
     *
     * @param filePath relative path of the file
     * @return InputStream associated with the file
     * @throws IOException if the file was not found or the stream cannot be created
     */
    InputStream getInputStreamFromFile(String filePath) throws IOException, RemoteException;


    /**
     * Get the file in the form of byte array
     *
     * @param filePath relative path of the file
     * @return the byte array representation of the file
     * @throws IOException
     */
    byte[] getByteArrayFromFile(String filePath) throws IOException, RemoteException;


    /**
     * Checks if the file with @param filePath exists in the storage
     *
     * @return true if the file exists
     */
    boolean isFileExisting(String filePath);

    /**
     * Get the list of file names of a given directory
     *
     * @param directoryPath relative path of the directory
     * @return the list of file names
     */
    List<String> getFileNamesOfADirectory(String directoryPath);

    /**
     * @param filePath relative path of the file
     * @return the size of the specified file
     * @throws IOException
     * @throws RemoteException
     */
    int getFileSize(String filePath) throws IOException, RemoteException;
}
