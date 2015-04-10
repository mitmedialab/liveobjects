package edu.mit.media.obm.liveobjects.app.media;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import edu.mit.media.obm.liveobjects.app.LiveObjectsApplication;
import edu.mit.media.obm.liveobjects.app.data.LObjContentProvider;
import edu.mit.media.obm.liveobjects.app.data.LObjContract;
import edu.mit.media.obm.liveobjects.app.detail.WrapUpActivity;
import edu.mit.media.obm.liveobjects.app.widget.MenuActions;
import edu.mit.media.obm.liveobjects.middleware.common.MiddlewareInterface;
import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.liveobjects.storage.wifi.WifiStorageConfig;
import edu.mit.media.obm.shair.liveobjects.R;



/**
 * Created by Valerio Panzica La Manna on 08/12/14.
 */
public class MediaViewActivity extends ActionBarActivity implements OnMediaViewListener {
    private static final String LOG_TAG = MediaViewActivity.class.getSimpleName();


    public static String EXTRA_LIVE_OBJ_NAME_ID = "live_obj_name_id";

    private MiddlewareInterface mMiddleware;
    private ContentController mContentController;

    private String mContentType;
    private String mFilePath;
    private String mFileName;

    private String mLiveObjNameId;

    private AsyncTask<Void, Integer, Void> mSavingFileTask = null;

    private ProgressDialog mDownloadProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_view);


        mMiddleware = ((LiveObjectsApplication)getApplication()).getMiddleware();
        mContentController = mMiddleware.getContentController();

        mDownloadProgressDialog = new ProgressDialog(this);
        mDownloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDownloadProgressDialog.setTitle("Downloading content ...");
        mDownloadProgressDialog.setMax(100);
        mDownloadProgressDialog.setProgressNumberFormat(null);
        mDownloadProgressDialog.setCancelable(false);

        if (savedInstanceState == null) {
            mLiveObjNameId = getIntent().getStringExtra(EXTRA_LIVE_OBJ_NAME_ID);
            if (mLiveObjNameId != null) {
                initContent(mLiveObjNameId);

                //TODO to improve: for now we first download all the file and the open the fragment
                initSavingFileTask();
                mSavingFileTask.execute();
            }

            getSupportActionBar().setTitle(mLiveObjNameId);
        }
    }


    private void initContent(String liveObjNameId) {
        Cursor cursor = LObjContentProvider.getLocalLiveObject(liveObjNameId, this);
        cursor.moveToFirst();
        mContentType = cursor.getString(cursor.getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_MEDIA_TYPE));
        mFilePath = cursor.getString(cursor.getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_MEDIA_FILEPATH));
        mFileName = mFilePath.substring(mFilePath.lastIndexOf(File.separator) + 1);
        cursor.close();
    }

    private void launchMediaFragment() {
        String fileUrl = "";
        if (isLocallyAvailable(mFilePath, mFileName, "DCIM")) {
            Log.d(LOG_TAG, "media file locally available ");
            fileUrl = mFilePath;
        }
        else {
            Log.d(LOG_TAG, "media file taken from live object ");
            Log.e(LOG_TAG, "filename: " + mFileName);
            Log.e(LOG_TAG, "filePath: " + mFilePath);
            fileUrl = getFileUrl(mFileName);

            Log.e(LOG_TAG, "fileUrl: " + fileUrl);
        }

        if (mContentType.equals(getResources().getString(R.string.content_type_video)) ){

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_media_container, VideoViewFragment.newInstance(fileUrl))
                    .commit();
        }
        else if (mContentType.equals(getResources().getString(R.string.content_type_audio)) ){
            getSupportFragmentManager().beginTransaction().
                    add(R.id.activity_media_container, VideoViewFragment.newInstance(fileUrl)).
                    commit();
        }
        else if (mContentType.equals(getResources().getString(R.string.content_type_gallery)) ){
            //TODO launch gallery
        }

    }



    private String getFileUrl(String filename) {
        String fileUrl;

        try {
            //TODO to change: the app cannot directly talk with the driver
            fileUrl = WifiStorageConfig.getMediaFolderPath(this) + "/" + filename ;
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new RuntimeException("An unrecoverable error was thrown");
        }
        return fileUrl;
    }

    private boolean isLocallyAvailable(String localPath, String remoteFileName, String remoteDirName) {
        File sizeFile = new File(localPath + ".size");

        if (!sizeFile.exists()) {
            Log.v(LOG_TAG, "file doesn't exist in local");
            return false;
        }

        int fileSize = getFileSize(localPath, remoteFileName, remoteDirName);
        File file = new File(localPath);

        Log.v(LOG_TAG, String.format("file size in local (%d), in remote (%d)", file.length(), fileSize));

        return (fileSize == file.length());
    }

    private void storeFileSize(String localPath, String remoteFileName, String remoteDirName) {
        File sizeFile = new File(localPath + ".size");

        if (sizeFile.exists()) {
            return;
        }

        try {
            int fileSize = mContentController.getFileSize(remoteFileName, remoteDirName);
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(sizeFile)));
            Log.v(LOG_TAG, "file_size = " + fileSize);
            writer.print(fileSize);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private int getFileSize(String localPath, String remoteFileName, String remoteDirName) {
        File sizeFile = new File(localPath + ".size");

        int fileSize = -1;

        try {
            if (sizeFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(sizeFile));
                fileSize = Integer.valueOf(reader.readLine());
            } else {
                fileSize = mContentController.getFileSize(remoteFileName, remoteDirName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return fileSize;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "onDestroy()");

        cancelTask();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        cancelTask();
        // when media is completed go to the wrap up activity
        Intent intent = new Intent(this, WrapUpActivity.class);
        intent.putExtra(WrapUpActivity.EXTRA_LIVE_OBJ_NAME_ID, mLiveObjNameId);
        intent.putExtra(WrapUpActivity.EXTRA_SHOW_ADD_COMMENT, true);
        startActivity(intent);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // to handle the error
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {

        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                Toast.makeText(this, "MEDIA_INFO_BUFFERING_END", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_goto_home) {
            MenuActions.goToHome(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cancelTask() {
        if (mSavingFileTask != null) {
            mSavingFileTask.cancel(true);
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Toast.makeText(this, "onBufferingUpdate " + percent, Toast.LENGTH_SHORT).show();
    }

    private void initSavingFileTask() {
        mSavingFileTask = new AsyncTask<Void, Integer, Void>() {
            @Override
            protected void onPreExecute() {
                mDownloadProgressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                final String dirName = "DCIM";

                if (!isLocallyAvailable(mFilePath, mFileName, dirName)) {
                    try {
                        Log.d(LOG_TAG, "starting saving media file " + mFileName + " into " + mFilePath);

                        storeFileSize(mFilePath, mFileName, dirName);
                        int fileSize = getFileSize(mFilePath, mFileName, dirName);

                        InputStream inputStream = mContentController.getInputStreamContent(mFileName, dirName);
                        inputStream.available();
                        File file = new File(mFilePath);
                        OutputStream outputStream = new FileOutputStream(file);
                        byte[] buffer = new byte[8192];
                        int len = inputStream.read(buffer);
                        int totalLen = 0;
                        int progress;
                        while (len != -1) {
                            outputStream.write(buffer, 0, len);
                            len = inputStream.read(buffer);
                            totalLen += len;

                            progress = (int)(100L * totalLen / fileSize);
                            publishProgress(progress);

                            if (isCancelled()) {
                                break;
                            }
                        }
                        progress = (int)(100L * totalLen / fileSize);
                        publishProgress(progress);
                        inputStream.close();
                        outputStream.close();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error saving media file", e);
                    }
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... progress) {
                mDownloadProgressDialog.setProgress(progress[0]);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                mDownloadProgressDialog.dismiss();

                Log.d(LOG_TAG, "file saving completed");
                launchMediaFragment();
            }
        };

    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        Toast.makeText(this, "onLoadComplete", Toast.LENGTH_SHORT).show();
    }
}
