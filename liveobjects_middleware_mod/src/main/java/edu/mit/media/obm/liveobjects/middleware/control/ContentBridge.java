package edu.mit.media.obm.liveobjects.middleware.control;

import android.content.Context;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import edu.mit.media.obm.liveobjects.middleware.common.ContentId;
import edu.mit.media.obm.liveobjects.middleware.storage.LocalStorageDriver;
import edu.mit.media.obm.liveobjects.middleware.storage.RemoteStorageDriver;

/**
 * This class implements a ContentController and provides access to the content of a live object.
 * If available, the content is taken from the user device (local storage),
 * otherwise it is taken from the live object.
 *
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class ContentBridge implements ContentController {
    private static final String LOG_TAG = ContentBridge.class.getSimpleName();

    private LocalStorageDriver mLocalStorageDriver; // TODO to implement
    private RemoteStorageDriver mRemoteStorageDriver;
    private Context mContext;


    public ContentBridge (Context context, LocalStorageDriver localStorageDriver, RemoteStorageDriver remoteStorageDriver) {
        mContext = context;
        mLocalStorageDriver = localStorageDriver;
        mRemoteStorageDriver = remoteStorageDriver;

    }

    @Override
    public void putSerializableContent(ContentId contentId, Serializable content) {
        // TODO check if contentId already present?
        // TODO create a file named with contentId
        // TODO add content to the file
        // TODO send the file to the live object

        throw new UnsupportedOperationException();

    }

    @Override
    public void putStringContent(ContentId contentId, final String stringContent) {

        final String directoryPath = contentId.getDirectoryPath();
        final String filename = contentId.getFilename();


        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mRemoteStorageDriver.writeNewRawFileFromString(filename, directoryPath, stringContent);
                } catch (IOException e) {
                    Log.e(LOG_TAG,"writeNewRawFile", e);
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();

    }

    @Override
    public Serializable getSerializableContent(ContentId contentId) {
        //TODO
        throw new UnsupportedOperationException();

    }

    @Override
    public InputStream getInputStreamContent(ContentId contentId) throws IOException, RemoteException {
        // TODO if the content is locally available return it, otherwise return the remote version
        String filename = contentId.getFilename();
        String directoryPath = contentId.getDirectoryPath();
        return mRemoteStorageDriver.getInputStreamFromFile(filename, directoryPath);
    }

    @Override
    public List<String> getFileNamesOfADirectory(String liveObjectId, String directoryName) {
        // TODO implement the check locally before contacting the remote liveObject
        return mRemoteStorageDriver.getFileNamesOfADirectory(directoryName);
    }

    @Override
    public int getContentSize(ContentId contentId) throws IOException, RemoteException {
        // TODO implement the check locally before contacting the remote liveObject
        String filename = contentId.getFilename();
        String directoryPath = contentId.getDirectoryPath();
        return mRemoteStorageDriver.getFileSize(filename, directoryPath);
    }

}
