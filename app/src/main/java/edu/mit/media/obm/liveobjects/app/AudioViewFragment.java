package edu.mit.media.obm.liveobjects.app;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

import java.io.IOException;

import edu.mit.media.obm.shair.liveobjects.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link edu.mit.media.obm.liveobjects.app.OnMediaViewListener} interface
 * to handle interaction events.
 * Use the {@link AudioViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AudioViewFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_FILE_URL = "fileurl";



    private String mFileUrl;
    private MediaController mMediaController;


    private OnMediaViewListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param filename name of the audio file to play.
     * @return A new instance of fragment AudioViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AudioViewFragment newInstance(String filename) {
        AudioViewFragment fragment = new AudioViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILE_URL, filename);
        fragment.setArguments(args);
        return fragment;
    }

    public AudioViewFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_audio_view, container, false);
        //TODO using mediaController?
        //mMediaController = (MediaController) rootView.findViewById(R.id.mediaController);

        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                String url = params[0];
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.prepare(); // might take long! (for buffering, etc)
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(mFileUrl);


        return rootView;

    }


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
