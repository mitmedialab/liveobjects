package edu.mit.media.obm.liveobjects.app;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import edu.mit.media.obm.liveobjects.middleware.control.ContentBridge;
import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.liveobjects.storage.wifi.WifiStorageDriver;
import edu.mit.media.obm.shair.liveobjects.R;


/**
 * Created by Valerio Panzica La Manna on 09/01/15.
 * Shows the details of a connected live object and allows to play the content
 */
public class DetailFragment extends Fragment {
    public final static String LIVE_OBJECT_NAME = "liveObjectName";

    private final static String LOG_TAG = DetailFragment.class.getSimpleName();
    private static String ICON_FILE_NAME = "icon.jpg";



    private CircularImageView mIconView;
    private View mLoadingPanel;
    private TextView mObjectTitleTextView;

    private ContentController mContentController;



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

        mContentController = new ContentBridge(getActivity(),null, new WifiStorageDriver(getActivity()));

        return rootView;
    }

    private void setLiveObjectImage() {


        new AsyncTask<CircularImageView,Void,Bitmap>() {
            CircularImageView imageView = null;
            @Override
            protected Bitmap doInBackground(CircularImageView... params) {
                //TODO moving the asynck task in the middleware?
                this.imageView = params[0];

                if (this.imageView == null)
                    Log.e(LOG_TAG, "IMAGE_VIEW NULL");


                try {
                    return  getBipmap(mContentController.getInputStreamContent(ICON_FILE_NAME));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;


            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null ) {
                    this.imageView.setImageBitmap(bitmap);
                }

                mLoadingPanel.setVisibility(View.GONE);
            }


            private Bitmap getBipmap(InputStream inputStream) {
                //TODO REFACTOR
                try {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] byteChunk = new byte[1024];
                    int bytesRead = 0;
                    while ((bytesRead = inputStream.read(byteChunk)) != -1) {
                        byteArrayOutputStream.write(byteChunk, 0, bytesRead);
                    }
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    BitmapFactory.Options bfOptions = new BitmapFactory.Options();
                    bfOptions.inPurgeable = true;
                    Bitmap resultBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, bfOptions);
                    byteArrayOutputStream.close();
                    inputStream.close();
                    return resultBitmap;
                }catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(mIconView);




    }




    private void setLiveObjectDescription() {
        //TODO
//        Bundle bundle = getArguments();
//        if (bundle != null && bundle.containsKey(LiveObjectsManager.EXTRA_LIVE_OBJECT)){
//            LiveObject liveObject = (LiveObject) bundle.getParcelable(LiveObjectsManager.EXTRA_LIVE_OBJECT);
//            String liveObjectName = liveObject.getName();
//            mObjectTitleTextView.setText(liveObjectName);
//        }

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
