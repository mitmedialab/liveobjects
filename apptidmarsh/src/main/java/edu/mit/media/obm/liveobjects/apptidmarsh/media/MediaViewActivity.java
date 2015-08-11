package edu.mit.media.obm.liveobjects.apptidmarsh.media;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.ButterKnife;
import edu.mit.media.obm.liveobjects.apptidmarsh.LiveObjectsApplication;
import edu.mit.media.obm.liveobjects.apptidmarsh.data.MLProjectPropertyProvider;
import edu.mit.media.obm.liveobjects.apptidmarsh.detail.DetailActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.MenuActions;
import edu.mit.media.obm.liveobjects.middleware.common.ContentId;
import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;
import edu.mit.media.obm.liveobjects.storage.wifi.WifiStorageConfig;
import edu.mit.media.obm.shair.liveobjects.R;


/**
 * Created by Valerio Panzica La Manna on 08/12/14.
 */
public class MediaViewActivity extends ActionBarActivity implements OnMediaViewListener {
    private static final String LOG_TAG = MediaViewActivity.class.getSimpleName();

    @BindString(R.string.arg_live_object_name_id) String EXTRA_LIVE_OBJ_NAME_ID;
    @BindString(R.string.state_live_object_name_id) String STATE_LIVE_OBJ_NAME_ID;
    @BindString(R.string.dir_contents) String MEDIA_DIRECTORY_NAME;

    private String mContentType;
    private String mFileName;

    private String mLiveObjNameId;

    private AsyncTask<Void, Integer, Void> mSavingFileTask = null;

    private ProgressDialog mDownloadProgressDialog;

    @BindString(R.string.content_type_video) String mContentTypeVideo;
    @BindString(R.string.content_type_audio) String mContentTypeAudio;
    @BindString(R.string.content_type_gallery) String mContentTypeGallery;

    @Inject ContentController mContentController;
    @Inject DbController mDbController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_view);
        ButterKnife.bind(this);

        DependencyInjector.inject(this, this);

        mDownloadProgressDialog = new ProgressDialog(this);
// TODO to reintroduce
//        initProgressDialog(mDownloadProgressDialog);

        if (savedInstanceState == null) {
            mLiveObjNameId = getIntent().getStringExtra(EXTRA_LIVE_OBJ_NAME_ID);
            if (mLiveObjNameId != null) {
                initContent(mLiveObjNameId);
                launchMediaFragment();

//                //TODO to improve: for now we first download all the file and the open the fragment
//                initSavingFileTask();
//                mSavingFileTask.execute();
            }

            getSupportActionBar().setTitle(mLiveObjNameId);
        } else {
            mLiveObjNameId = savedInstanceState.getString(STATE_LIVE_OBJ_NAME_ID);
        }
    }

    private void initContent(String liveObjectId) {
        Map<String, Object> properties = mDbController.getProperties(liveObjectId);
        MLProjectPropertyProvider propertyProvider = new MLProjectPropertyProvider(properties);
        mContentType = propertyProvider.getMediaType();
        mFileName = propertyProvider.getMediaFileName();
    }

    private void launchMediaFragment() {
        ContentId mediaContentId = new ContentId(mLiveObjNameId, MEDIA_DIRECTORY_NAME, mFileName);
        String fileUrl;
        try {
            fileUrl = mContentController.getFileUrl(mediaContentId);
        } catch (IOException e) {
            Log.e(LOG_TAG, e.toString());
            throw new IllegalStateException();
        } catch (RemoteException e) {
            Log.e(LOG_TAG, e.toString());
            throw new IllegalStateException();
        }

        Fragment fragment;
        if (mContentType.equals(mContentTypeVideo)) {
            fragment = VideoViewFragment.newInstance(this, fileUrl);
        } else if (mContentType.equals(mContentTypeAudio)) {
            fragment = VideoViewFragment.newInstance(this, fileUrl);
        } else if (mContentType.equals(mContentTypeGallery)) {
            //TODO launch gallery
            throw new IllegalStateException("Unimplemented content type");
        } else {
            throw new IllegalStateException("invalid content type");
        }

        getSupportFragmentManager().beginTransaction()
                .add(R.id.activity_media_container, fragment)
                .commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "onDestroy()");

        cancelTask();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_LIVE_OBJ_NAME_ID, mLiveObjNameId);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        cancelTask();
        finish();
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

    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        Toast.makeText(this, "onLoadComplete", Toast.LENGTH_SHORT).show();
    }
}
