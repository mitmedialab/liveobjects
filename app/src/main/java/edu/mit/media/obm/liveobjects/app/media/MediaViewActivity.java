package edu.mit.media.obm.liveobjects.app.media;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

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

    private AsyncTask<Void, Void, Void> mSavingFileTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_view);


        mMiddleware = ((LiveObjectsApplication)getApplication()).getMiddleware();
        mContentController = mMiddleware.getContentController();

        if (savedInstanceState == null) {
            mLiveObjNameId = getIntent().getStringExtra(EXTRA_LIVE_OBJ_NAME_ID);
            if (mLiveObjNameId != null) {
                initContent(mLiveObjNameId);


                //TODO to improve: for now we first download all the file and the open the fragment
                initSavingFileTask();
                mSavingFileTask.execute();



            }
        }


    }


    private void initContent(String liveObjNameId) {
        Cursor cursor = LObjContentProvider.getLocalLiveObject(liveObjNameId, this);
        cursor.moveToFirst();
        mContentType = cursor.getString(cursor.getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_MEDIA_TYPE));
        mFilePath = cursor.getString(cursor.getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_MEDIA_FILEPATH));
        mFileName = mFilePath.substring(mFilePath.lastIndexOf(":") + 1);
        cursor.close();
    }

    private void launchMediaFragment() {
        String fileUrl = "";
        if (isLocallyAvailable(mFilePath)) {
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
            fileUrl = WifiStorageConfig.getBaseFolderPath(this) + "/" + filename ;
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new RuntimeException("An unrecoverable error was thrown");
        }
        return fileUrl;
    }

    private boolean isLocallyAvailable(String filePath) {
        File f = new File(filePath);
        return f.exists();
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
        mSavingFileTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (!isLocallyAvailable(mFilePath)) {
                    try {
                        Log.d(LOG_TAG, "starting saving media file " + mFileName + "into " + mFilePath);
                        InputStream inputStream = mContentController.getInputStreamContent(mFileName);
                        File file = new File(mFilePath);
                        OutputStream outputStream = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        int len = inputStream.read(buffer);
                        while (len != -1) {
                            outputStream.write(buffer, 0, len);
                            len = inputStream.read(buffer);
                        }
                        inputStream.close();
                        outputStream.close();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error saving media file", e);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
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
