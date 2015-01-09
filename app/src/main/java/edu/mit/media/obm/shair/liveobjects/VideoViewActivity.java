package edu.mit.media.obm.shair.liveobjects;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

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
