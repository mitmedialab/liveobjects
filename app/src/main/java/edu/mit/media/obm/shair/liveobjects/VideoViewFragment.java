package edu.mit.media.obm.shair.liveobjects;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 *  Created by Valerio Panzica La Manna on 08/12/14.
 *  Shows a video file from FlashAir
 */
public class VideoViewFragment extends Fragment {
    public final static String LOG_TAG = VideoViewFragment.class.getSimpleName();
    private static String BASE_URL = "http://flashair";
    private static String DIRECTORY = "DCIM";
    private static String FILE_NAME = "en.mp4";

    private static String FILE_URL= BASE_URL + "/" + DIRECTORY + "/" + FILE_NAME;

    private VideoView mvideoView;
    public VideoViewFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_video_view, container, false);

        mvideoView = (VideoView) rootView.findViewById(R.id.myVideo);

        MediaController videoControl = new MediaController(getActivity());
        videoControl.setAnchorView(mvideoView);
        mvideoView.setMediaController(videoControl);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                Uri vidUri = Uri.parse(FILE_URL);
                mvideoView.setVideoURI(vidUri);
                Log.i(LOG_TAG, "setting video: " +  vidUri.toString());
                mvideoView.start();
                return null;

            }
        }.execute();
        return rootView;
    }
}
