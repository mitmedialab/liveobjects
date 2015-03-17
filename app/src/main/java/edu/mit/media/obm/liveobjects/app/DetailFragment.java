package edu.mit.media.obm.liveobjects.app;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import edu.mit.media.obm.liveobjects.middleware.common.MiddlewareInterface;
import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.shair.liveobjects.R;


/**
 * Created by Valerio Panzica La Manna on 09/01/15.
 * Shows the details of a connected live object and allows to play the content
 */
public class DetailFragment extends Fragment {
    public final static String LIVE_OBJECT_NAME = "liveObjectName";

    private final static String LOG_TAG = DetailFragment.class.getSimpleName();
    private String ICON_FILE_NAME;
    private String mediaConfigFileName;



    private CircularImageView mIconView;
    private View mLoadingPanel;
    private TextView mObjectTitleTextView;

    private MiddlewareInterface mMiddleware;
    private ContentController mContentController;

    private JSONObject mJSONConfig;

    OnErrorListener mOnErrorListener = null;

    public interface OnErrorListener {
        abstract public void onError(Exception exception);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mIconView = (CircularImageView) rootView.findViewById(R.id.object_image_view);
        mLoadingPanel = rootView.findViewById(R.id.loadingPanel);
        mObjectTitleTextView = (TextView) rootView.findViewById(R.id.object_title_textview);

        ICON_FILE_NAME = getActivity().getResources().getString(R.string.icon_filename) + ".jpg";
        mediaConfigFileName = getActivity().getResources().getString(R.string.media_config_filename) + ".jso";

        mMiddleware = ((LiveObjectsApplication)getActivity().getApplication()).getMiddleware();
        mContentController = mMiddleware.getContentController();

        setLiveObjectImage();
        setLiveObjectDescription();
        getMediaConfig();


        mIconView.setOnClickListener(
                new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {

                          try {
                              String contentType = mJSONConfig.getJSONObject("media-config").getJSONObject("media").getString("type");
                              String filename = mJSONConfig.getJSONObject("media-config").getJSONObject("media").getString("filename");

                              // launch the media associated to the object
                              Intent viewIntent = new Intent(getActivity(), MediaViewActivity.class);
                              viewIntent.putExtra(MediaViewActivity.CONTENT_TYPE_EXTRA, contentType);
                              viewIntent.putExtra(MediaViewActivity.FILE_NAME_EXTRA, filename);
                              getActivity().startActivity(viewIntent);
                          }catch(JSONException e){
                              //TODO
                              e.printStackTrace();
                              mOnErrorListener.onError(e);
                          }

                      }
                  }
        );



        return rootView;
    }

    private void setLiveObjectImage() {

        //TODO moving the asynck task in the middleware?
        new AsyncTask<CircularImageView,Void,Bitmap>() {
            CircularImageView imageView = null;
            @Override
            protected Bitmap doInBackground(CircularImageView... params) {

                this.imageView = params[0];

                if (this.imageView == null)
                    Log.e(LOG_TAG, "IMAGE_VIEW NULL");


                try {
                    return getBipmap(mContentController.getInputStreamContent(ICON_FILE_NAME));
                } catch (IOException e) {
                    e.printStackTrace();
                    mOnErrorListener.onError(e);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    mOnErrorListener.onError(e);
                }

                return null;


            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null ) {
                    BitmapDrawable background = new BitmapDrawable(bitmap);
                    mLoadingPanel.setBackgroundDrawable(background);
                }

                ProgressBar progressBar = (ProgressBar)
                        DetailFragment.this.getActivity().findViewById(R.id.detail_progress_bar);
                progressBar.setVisibility(View.GONE);
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
                    mOnErrorListener.onError(e);
                }
                return null;
            }
        }.execute(mIconView);

    }


    private void getMediaConfig() {
        StringBuilder builder = new StringBuilder();
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                String mediaFileName = params[0];

                try {
                    InputStream inputStream = mContentController.getInputStreamContent(mediaFileName);
                    StringBuilder builder = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    String jsonConfigString = builder.toString();
                    try {
                        mJSONConfig = new JSONObject(jsonConfigString);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        mOnErrorListener.onError(e);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    mOnErrorListener.onError(e);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    mOnErrorListener.onError(e);
                }
                return null;
            }
        }.execute(mediaConfigFileName);

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

    public void setOnCancelListener(OnErrorListener onCancelListener) {
        mOnErrorListener = onCancelListener;
    }
}
