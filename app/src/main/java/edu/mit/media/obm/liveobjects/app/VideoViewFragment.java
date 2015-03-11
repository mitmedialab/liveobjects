package edu.mit.media.obm.liveobjects.app;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import edu.mit.media.obm.shair.liveobjects.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link edu.mit.media.obm.liveobjects.app.OnMediaViewListener} interface
 * to handle interaction events.
 * Use the {@link VideoViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoViewFragment extends Fragment {
    private static final String LOG_TAG = VideoViewFragment.class.getSimpleName();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_FILE_URL = "fileurl";



    private String mFileUrl;

    private VideoView mvideoView;


    private OnMediaViewListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param fileUrl Parameter 1.
     * @return A new instance of fragment VideoViewFragment.
     */
    
    public static VideoViewFragment newInstance(String fileUrl) {
        VideoViewFragment fragment = new VideoViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILE_URL, fileUrl);
        fragment.setArguments(args);
        return fragment;
    }

    public VideoViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFileUrl = getArguments().getString(ARG_FILE_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_video_view, container, false);

        mvideoView = (VideoView) rootView.findViewById(R.id.myVideo);

        MediaController videoControl = new MediaController(getActivity());
        videoControl.setAnchorView(mvideoView);
        mvideoView.setMediaController(videoControl);
        mvideoView.setOnCompletionListener(mListener);
        mvideoView.setOnErrorListener(mListener);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                Uri vidUri = Uri.parse(mFileUrl);
                mvideoView.setVideoURI(vidUri);
                Log.i(LOG_TAG, "setting video: " + vidUri.toString());
                mvideoView.start();
                return null;

            }
        }.execute();
        return rootView;

    }

    //TODO manage the event of completion of the video and call the method of mListener

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnMediaViewListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMediaViewListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


}
