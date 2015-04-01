package edu.mit.media.obm.liveobjects.app.media;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import edu.mit.media.obm.liveobjects.app.main.MainActivity;
import edu.mit.media.obm.liveobjects.app.detail.WrapUpActivity;
import edu.mit.media.obm.liveobjects.app.widget.MenuActions;
import edu.mit.media.obm.liveobjects.storage.wifi.WifiStorageConfig;
import edu.mit.media.obm.shair.liveobjects.R;



/**
 * Created by Valerio Panzica La Manna on 08/12/14.
 */
public class MediaViewActivity extends ActionBarActivity implements OnMediaViewListener {

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
            fileUrl = WifiStorageConfig.getBaseFolderPath(this) + "/" + filename ;
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
}
