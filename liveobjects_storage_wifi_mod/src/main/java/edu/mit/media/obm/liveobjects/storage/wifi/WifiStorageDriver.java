package edu.mit.media.obm.liveobjects.storage.wifi;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import edu.mit.media.obm.liveobjects.middleware.storage.RemoteStorageDriver;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class WifiStorageDriver implements RemoteStorageDriver {

    private Context mContext;

    public WifiStorageDriver(Context context) {
        mContext = context;
    }

    @Override
    public void writeNewRawFileFromStream(String fileName, InputStream stream) throws IOException {
        //TODO
        throw  new UnsupportedOperationException();

    }

    @Override
    public InputStream getInputStreamFromFile(String fileName) throws IOException, RemoteException {
        String basePath = WifiStorageConfig.getBasePath(mContext);
        String path = basePath + "/" + fileName;
        Log.v(getClass().getSimpleName(), "base_path = " + basePath + ", fileNAme = " + fileName);
        URL url = new URL(path);
        URLConnection urlCon = url.openConnection();
        urlCon.connect();
        InputStream inputStream = urlCon.getInputStream();
        return inputStream;
    }

    @Override
    public byte[] getByteArrayFromFile(String filename) throws IOException, RemoteException {
        InputStream inputStream = getInputStreamFromFile(filename);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] byteChunk = new byte[1024];
        int bytesRead = 0;
        while( (bytesRead = inputStream.read(byteChunk)) != -1) {
            byteArrayOutputStream.write(byteChunk, 0, bytesRead);
        }

        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public int getNumberOfFiles() {
        //TODO
        throw  new UnsupportedOperationException();


    }

    @Override
    public boolean isFileExisting(String filename) {
        //TODO
        throw  new UnsupportedOperationException();
    }
}
