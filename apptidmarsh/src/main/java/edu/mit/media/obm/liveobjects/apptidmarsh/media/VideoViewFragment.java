package edu.mit.media.obm.liveobjects.apptidmarsh.media;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.noveogroup.android.log.Log;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;
import edu.mit.media.obm.shair.liveobjects.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnMediaViewListener} interface
 * to handle interaction events.
 * Use the {@link VideoViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoViewFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    @BindString(R.string.arg_file_url) String ARG_FILE_URL;
    @BindString(R.string.state_play_position) String STATE_PLAY_POSITION;

    private String mFileUrl;

    @Bind(R.id.myVideo) VideoView mVideoView;
    @Inject MediaController mVideoControl;

    private Integer mPlayPosition;

    private OnMediaViewListener mListener;

    public VideoViewFragment() {
        mPlayPosition = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_video_view, container, false);
        ButterKnife.bind(this, rootView);

        DependencyInjector.inject(this, getActivity());

        if (savedInstanceState != null) {
            mPlayPosition = savedInstanceState.getInt(STATE_PLAY_POSITION);
        }

        if (getArguments() != null) {
            mFileUrl = getArguments().getString(ARG_FILE_URL);
        }

        mVideoControl.setAnchorView(mVideoView);
        mVideoView.setMediaController(mVideoControl);
        mVideoView.setOnCompletionListener(mListener);
        mVideoView.setOnErrorListener(mListener);

        Uri vidUri = Uri.parse(mFileUrl);
        mVideoView.setVideoURI(vidUri);
        Log.i("setting video: %s", vidUri.toString());

        if (mPlayPosition != null) {
            mVideoView.seekTo(mPlayPosition);
        }

        mVideoView.start();

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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_PLAY_POSITION, mVideoView.getCurrentPosition());
    }
}
