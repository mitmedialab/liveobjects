package edu.mit.media.obm.liveobjects.app;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import edu.mit.media.obm.shair.liveobjects.R;

/**
 * Created by Valerio Panzica La Manna on 08/12/14.
 */
public class VideoViewActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_video_container, new VideoViewFragment())
                    .commit();
        }
    }
}
