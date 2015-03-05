package edu.mit.media.obm.liveobjects.app;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

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

        if (contentType.equals(getResources().getString(R.string.content_type_video)) ){

            Bundle bundle = new Bundle();
            bundle.putString(VideoViewFragment.EXTRA_VIDEO_FILE_NAME, filename);
            VideoViewFragment videoViewFragment = new VideoViewFragment();
            videoViewFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_media_container, videoViewFragment)
                    .commit();
        }
        else if (contentType.equals(getResources().getString(R.string.content_type_audio)) ){
            //todo launch the fragment associated with audio

        }
        else if (contentType.equals(getResources().getString(R.string.content_type_gallery)) ){
            //todo launch gallery
        }

    }
}
