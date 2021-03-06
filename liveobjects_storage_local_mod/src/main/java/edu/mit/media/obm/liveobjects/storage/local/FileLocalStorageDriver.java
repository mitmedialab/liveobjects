package edu.mit.media.obm.liveobjects.storage.local;

import android.content.Context;
import android.os.RemoteException;

import com.noveogroup.android.log.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import edu.mit.media.obm.liveobjects.middleware.storage.LocalStorageDriver;

/**
 * Driver to access and store live-object content in the internal filesystem of the app.
 * The content is accessible only through the app and removed when the app is uninstalled.
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class FileLocalStorageDriver implements LocalStorageDriver {
    private Context mContext;

    public FileLocalStorageDriver(Context context) {
        mContext = context;
    }

    @Override
    public void writeNewRawFileFromString(String filePath, String bodyString) throws IOException {
        writeNewRawFileFromByteArray(filePath, bodyString.getBytes());
    }

    @Override
    public void writeNewRawFileFromInputStream(String filePath, InputStream inputStream) throws IOException {
        //TODO check the size of the buffer
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(30000000);
        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        writeNewRawFileFromByteArray(filePath, buffer.toByteArray());

    }

    @Override
    public void writeNewRawFileFromByteArray(String filePath, byte[] byteArray) throws IOException {
        String fullPath = getFullPath(filePath);
        File file = new File(fullPath);
        createDirectory(file.getParent());
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(byteArray);
        outputStream.close();
    }

    private void createDirectory(String directoryPath) {

        File directory = new File(directoryPath);
        boolean directoryCreated =directory.mkdirs();
        if (directoryCreated) {
            Log.d("directory created: " + directory);
        }

    }

    @Override
    public InputStream getInputStreamFromFile(String filePath) throws IOException, RemoteException {
        String fullPath = getFullPath(filePath);
        return new FileInputStream(fullPath);
    }

    @Override
    public byte[] getByteArrayFromFile(String filePath) throws IOException, RemoteException {
        InputStream inputStream = getInputStreamFromFile(filePath);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] byteChunk = new byte[1024];
        int bytesRead = 0;
        while( (bytesRead = inputStream.read(byteChunk)) != -1) {
            byteArrayOutputStream.write(byteChunk, 0, bytesRead);
        }

        return byteArrayOutputStream.toByteArray();

    }

    @Override
    public boolean isFileExisting(String filePath) {
        String fullPath = getFullPath(filePath);
        File file = new File(fullPath);
        return file.exists();
    }

    @Override
    public List<String> getFileNamesOfADirectory(String directoryPath) {
        //TODO to implement
        throw  new UnsupportedOperationException();
    }

    @Override
    public int getFileSize(String filePath) throws IOException, RemoteException {
        //TODO to implement
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFullPath(String relativePath) {
        String fullPath = mContext.getFilesDir().getAbsolutePath() + "/" + relativePath;
        Log.v("fullPath = %s", fullPath);

        return fullPath;
    }
}
