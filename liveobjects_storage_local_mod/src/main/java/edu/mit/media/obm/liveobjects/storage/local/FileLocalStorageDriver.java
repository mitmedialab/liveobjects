package edu.mit.media.obm.liveobjects.storage.local;

import android.content.Context;
import android.os.RemoteException;

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
        String fullPath = getFullPath(filePath);
        File file = new File(fullPath);
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(bodyString.getBytes());
        outputStream.close();
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

    private String getFullPath(String relativePath) {
        return mContext.getFilesDir().getAbsolutePath() + File.pathSeparator + relativePath;

    }
}
