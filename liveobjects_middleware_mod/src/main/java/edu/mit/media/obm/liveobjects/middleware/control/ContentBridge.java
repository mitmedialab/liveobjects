package edu.mit.media.obm.liveobjects.middleware.control;

import android.content.Context;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    public void putStringContent(ContentId contentId, final String stringContent) {

        final String filePath = contentId.getRelativePath();

        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mRemoteStorageDriver.writeNewRawFileFromString(filePath, stringContent);
                } catch (IOException e) {
                    Log.e(LOG_TAG,"writeNewRawFile", e);
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();

    }



    @Override
    public InputStream getInputStreamContent(ContentId contentId) throws IOException, RemoteException {
        // TODO if the content is locally available return it, otherwise return the remote version
        String filePath = contentId.getRelativePath();
        return mRemoteStorageDriver.getInputStreamFromFile(filePath);
    }

    @Override
    public List<String> getFileNamesOfADirectory(String liveObjectId, String directoryName) {
        // TODO implement the check locally before contacting the remote liveObject
        String directoryPath = liveObjectId + File.pathSeparator + directoryName;
        return mRemoteStorageDriver.getFileNamesOfADirectory(directoryPath);
    }

    @Override
    public int getContentSize(ContentId contentId) throws IOException, RemoteException {
        // TODO implement the check locally before contacting the remote liveObject
        String filePath = contentId.getRelativePath();

        return mRemoteStorageDriver.getFileSize(filePath);
    }

}
