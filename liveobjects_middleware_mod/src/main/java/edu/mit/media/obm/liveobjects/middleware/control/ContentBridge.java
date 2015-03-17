package edu.mit.media.obm.liveobjects.middleware.control;

import android.content.Context;
import android.os.RemoteException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import edu.mit.media.obm.liveobjects.middleware.storage.LocalStorageDriver;
import edu.mit.media.obm.liveobjects.middleware.storage.RemoteStorageDriver;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class ContentBridge implements ContentController {

    private LocalStorageDriver mLocalStorageDriver; // TODO to implement
    private RemoteStorageDriver mRemoteStorageDriver;
    private Context mContext;


    public ContentBridge (Context context, LocalStorageDriver localStorageDriver, RemoteStorageDriver remoteStorageDriver) {
        mContext = mContext;
        mLocalStorageDriver = localStorageDriver;
        mRemoteStorageDriver = remoteStorageDriver;

    }

    @Override
    public void putSerializableContent(String contentId, Serializable content) {
        // TODO
        throw  new UnsupportedOperationException();
    }

    @Override
    public Serializable getSerializableContent(String contentId) {
        //TODO
        throw new UnsupportedOperationException();

    }

    @Override
    public InputStream getInputStreamContent(String contentId) throws IOException, RemoteException {
        return mRemoteStorageDriver.getInputStreamFromFile(contentId);
    }
}
