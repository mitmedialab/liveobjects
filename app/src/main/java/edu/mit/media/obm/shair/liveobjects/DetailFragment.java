package edu.mit.media.obm.shair.liveobjects;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;


/**
 * Created by Valerio Panzica La Manna on 09/01/15.
 * Shows the details of a connected live object and allows to play the content
 */
public class DetailFragment extends Fragment {
    public final static String LIVE_OBJECT_NAME = "liveObjectName";

    private final static String LOG_TAG = DetailFragment.class.getSimpleName();



    private static String BASE_URL = "http://flashair";
    private static String DIRECTORY = "DCIM";
    private static String ICON_FILE_NAME = "icon.jpg";

    private static String ICON_URL= BASE_URL + "/" + DIRECTORY + "/" + ICON_FILE_NAME;

    private CircularImageView mIconView;
    private View mLoadingPanel;
    private TextView mObjectTitleTextView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_detail, container, false);


        mIconView = (CircularImageView) rootView.findViewById(R.id.object_image_view);
        mLoadingPanel = rootView.findViewById(R.id.loadingPanel);
        mObjectTitleTextView = (TextView) rootView.findViewById(R.id.object_title_textview);








        setLiveObjectImage();
        setLiveObjectDescription();



        mIconView.setOnClickListener(
                new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {

                          // launch the video associated to the object
                          Intent viewIntent = new Intent(getActivity(), VideoViewActivity.class);
                          getActivity().startActivity(viewIntent);
                      }
                  }
        );

        return rootView;
    }

    private void setLiveObjectImage() {


        new AsyncTask<CircularImageView,Void,Bitmap>() {
            CircularImageView imageView = null;
            @Override
            protected Bitmap doInBackground(CircularImageView... params) {
                this.imageView = params[0];

                if (this.imageView == null)
                    Log.e(LOG_TAG, "IMAGE_VIEW NULL");

                return  FlashAirRequest.getBitmap(ICON_URL);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null ) {
                    this.imageView.setImageBitmap(bitmap);



                }

                mLoadingPanel.setVisibility(View.GONE);
            }
        }.execute(mIconView);




    }

    private void setLiveObjectDescription() {
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(LIVE_OBJECT_NAME)){

            mObjectTitleTextView.setText(bundle.getString(LIVE_OBJECT_NAME));
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        mIconView.clearAnimation();
    }

    @Override
    public void onStart() {
        super.onStart();

        final Animation zoomIn = AnimationUtils.loadAnimation(getActivity(), R.anim.zoom_in);
        final Animation zoomOut = AnimationUtils.loadAnimation(getActivity(), R.anim.zoom_out);

        zoomIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIconView.startAnimation(zoomOut);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        zoomOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIconView.startAnimation(zoomIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mIconView.setAnimation(zoomIn);
        mIconView.setAnimation(zoomOut);

        mIconView.startAnimation(zoomIn);
    }
}
