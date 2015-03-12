package edu.mit.media.obm.liveobjects.app;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;

import edu.mit.media.obm.liveobjects.storage.wifi.WifiStorageConfig;
import edu.mit.media.obm.shair.liveobjects.R;



/**
 * Created by Valerio Panzica La Manna on 08/12/14.
 */
public class MediaViewActivity extends ActionBarActivity implements OnMediaViewListener{

    public static String CONTENT_TYPE_EXTRA = "contentType";
    public static String FILE_NAME_EXTRA = "filename";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_view);
        if (savedInstanceState == null) {
            String contentType = getIntent().getStringExtra(CONTENT_TYPE_EXTRA);
            String filename = getIntent().getStringExtra(FILE_NAME_EXTRA);


            if (contentType != null && filename!= null) {
                launchMediaFragment(contentType, filename);

            }

        }
    }


    private void launchMediaFragment(String contentType, String filename) {
        String fileUrl = getFileUrl(filename);

        if (contentType.equals(getResources().getString(R.string.content_type_video)) ){


            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_media_container, VideoViewFragment.newInstance(fileUrl))
                    .commit();
        }
        else if (contentType.equals(getResources().getString(R.string.content_type_audio)) ){
            getSupportFragmentManager().beginTransaction().
                    add(R.id.activity_media_container, VideoViewFragment.newInstance(fileUrl)).
                    commit();
        }
        else if (contentType.equals(getResources().getString(R.string.content_type_gallery)) ){
            //todo launch gallery
        }

    }



    private String getFileUrl(String filename) {
        String fileUrl;

        try {
            //TODO to change: the app cannot directly talk with the driver
            fileUrl = WifiStorageConfig.getBasePath(this) + "/" + filename ;
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new RuntimeException("An unrecoverable error was thrown");
        }
        return fileUrl;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // when media is completed go to the wrap up activity
        Intent intent = new Intent(this, WrapUpActivity.class);
        startActivity(intent);

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // to handle the error
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }
}
