package edu.mit.media.obm.liveobjects.app;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import edu.mit.media.obm.shair.liveobjects.R;

/**
 * Created by Valerio Panzica La Manna on 08/12/14.
 */
public class MediaViewActivity extends ActionBarActivity {

    public static String CONTENT_TYPE_EXTRA = "contentType";
    public static String FILE_NAME_EXTRA = "filename";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        if (savedInstanceState == null) {
            String contentType = getIntent().getStringExtra(CONTENT_TYPE_EXTRA);
            String filename = getIntent().getStringExtra(FILE_NAME_EXTRA);
            if (contentType != null && filename!= null) {
                Toast.makeText(this, "contentType: " + contentType, Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "filename: " + filename, Toast.LENGTH_SHORT).show();
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_video_container, new VideoViewFragment())
                    .commit();
        }
    }


    private void launchMediaFragment(String contentType, String filename) {
        
    }
}
