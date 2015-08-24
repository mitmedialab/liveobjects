package edu.mit.media.obm.liveobjects.middleware.control;

import android.content.Context;
import android.os.AsyncTask;
import android.os.RemoteException;

import com.noveogroup.android.log.Log;

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
    private LocalStorageDriver mLocalStorageDriver;
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
                    Log.e("writeNewRawFile", e);
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();

    }



    @Override
    public InputStream getInputStreamContent(ContentId contentId) throws IOException, RemoteException {
        // if the content is locally available return it,
        // otherwise return the remote version and save it locally
        InputStream inputStream;
        String filePath = contentId.getRelativePath();
        if (mLocalStorageDriver.isFileExisting(filePath)) {
            Log.d("accessing content locally, filePath: " + filePath);
            inputStream = mLocalStorageDriver.getInputStreamFromFile(filePath);
        }
        else {
            Log.d("accessing content remotely, filePath: " + filePath);
            inputStream = mRemoteStorageDriver.getInputStreamFromFile(filePath);
            mLocalStorageDriver.writeNewRawFileFromInputStream(filePath, inputStream);
            inputStream.close();
            inputStream = mLocalStorageDriver.getInputStreamFromFile(filePath);
        }
        return inputStream;
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

    @Override
    public String getFileUrl(final ContentId contentId) throws IOException, RemoteException {
        String filePath = contentId.getRelativePath();
        if (mLocalStorageDriver.isFileExisting(filePath)) {
            Log.d("returning local file path, filePath: " + filePath);
            return mLocalStorageDriver.getFullPath(filePath);
        }
        else {
            Log.d("returning remote file path, filePath: " + filePath);
            new AsyncTask<String, Void, Void>() {
                @Override
                protected Void doInBackground(String... params) {
                    String filePath = params[0];
                    // get remotely
                    InputStream inputStream = null;
                    try {
                        inputStream = getInputStreamContent(contentId);
                        // save it locally
                        mLocalStorageDriver.writeNewRawFileFromInputStream(filePath, inputStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute(filePath);

            return mRemoteStorageDriver.getFullPath(filePath);

        }
    }

    @Override
    public boolean isContentLocallyAvailable(ContentId contentId) {
        String filePath = contentId.getRelativePath();
        return mLocalStorageDriver.isFileExisting(filePath);
    }
}
