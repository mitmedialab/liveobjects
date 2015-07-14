package edu.mit.media.obm.liveobjects.apptidmarsh.detail;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import edu.mit.media.obm.liveobjects.apptidmarsh.LiveObjectsApplication;
import edu.mit.media.obm.liveobjects.apptidmarsh.data.MLProjectContract;
import edu.mit.media.obm.liveobjects.apptidmarsh.data.MLProjectPropertyProvider;
import edu.mit.media.obm.liveobjects.apptidmarsh.media.MediaViewActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.Util;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.BitmapEditor;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.ZoomInOutAnimation;
import edu.mit.media.obm.liveobjects.middleware.common.ContentId;
import edu.mit.media.obm.liveobjects.middleware.common.MiddlewareInterface;
import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;
import edu.mit.media.obm.liveobjects.middleware.util.JSONUtil;
import edu.mit.media.obm.shair.liveobjects.R;


/**
 * Created by Valerio Panzica La Manna on 09/01/15.
 * Shows the details of a connected live object and allows to play the content
 */
public class DetailFragment extends Fragment {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    //TODO make the directory name parametrizable
    private static final String DIRECTORY_NAME = "DCIM";

    private static final String ARG_LIVE_OBJ_NAME_ID = "live_obj_name_id";

    private String mLiveObjectNameID;

    private View mRootView;
    private ImageView mIconView;
    private TextView mObjectTitleTextView;
    private TextView mObjectGroupTextView;
    private TextView mObjectDescriptionTextView;
    private ProgressBar mProgressBar;
    private LinearLayout mDetailInfoLayout;

    private MiddlewareInterface mMiddleware;
    private ContentController mContentController;
    private DbController mDbController;

    private OnErrorListener mOnErrorListener = null;

    private AsyncTask<String, Void, InputStream> mSetBackgroundImageTask = null;
    private AsyncTask<String, Void, JSONObject> mSetPropertiesTask = null;


    public interface OnErrorListener {
        void onError(Exception exception);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameter.
     *
     * @param liveObjectNameID the name_id of the live object obtained during discovery.
     * @return A new instance of fragment DetailFragment
     */
    public static DetailFragment newInstance(String liveObjectNameID) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LIVE_OBJ_NAME_ID, liveObjectNameID);
        fragment.setArguments(args);
        return fragment;

    }

    // Required empty constructor
    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLiveObjectNameID = getArguments().getString(ARG_LIVE_OBJ_NAME_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        mRootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mMiddleware = ((LiveObjectsApplication) getActivity().getApplication()).getMiddleware();
        mContentController = mMiddleware.getContentController();
        mDbController = mMiddleware.getDbController();

        initUIObjects(mRootView);

        Map<String, Object> liveObjectProperties = getLiveObjectProperties(mLiveObjectNameID);

        setUIContent(liveObjectProperties);
        setUIListeners();

        return mRootView;
    }

    private void initUIObjects(View rootView) {
        mIconView = (ImageView) rootView.findViewById(R.id.object_image_view);
        mObjectTitleTextView = (TextView) rootView.findViewById(R.id.object_title_textview);
        mObjectGroupTextView = (TextView) rootView.findViewById(R.id.object_group_textview);
        mObjectDescriptionTextView = (TextView) rootView.findViewById(R.id.object_description_textview);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.detail_progress_bar);
        mDetailInfoLayout = (LinearLayout) rootView.findViewById(R.id.detail_info_layout);
    }

    private Map<String, Object> getLiveObjectProperties(String liveObjectId) {
        Map<String, Object> properties = null;

        if (mDbController.isLiveObjectEmpty(liveObjectId)) {
            // live object empty, fill it with properties
            properties = fetchProperties(liveObjectId);
            storeProperties(liveObjectId, properties);
        } else {
            properties = mDbController.getProperties(liveObjectId);
        }

        return properties;
    }

    private Map<String, Object> fetchProperties(final String liveObjectId) {
        String mediaConfigFileName = getActivity().getResources().getString(R.string.media_config_filename) + ".jso";

        mSetPropertiesTask =
                new AsyncTask<String, Void, JSONObject>() {
                    @Override
                    protected JSONObject doInBackground(String... params) {
                        String configFileName = params[0];

                        InputStream inputStream = null;
                        try {

                            ContentId configFileContentId = new ContentId(liveObjectId, DIRECTORY_NAME, configFileName);
                            // retrieve JSON Object
                            inputStream = mContentController.getInputStreamContent(configFileContentId);

                            JSONObject jsonConfig = JSONUtil.getJSONFromInputStream(inputStream);
                            inputStream.close();
                            return jsonConfig;

                        } catch (Exception e) {
                            e.printStackTrace();
                            mOnErrorListener.onError(e);
                        }
                        return null;
                    }
                }.execute(mediaConfigFileName);


        Map<String, Object> properties = null;
        try {
            JSONObject jsonProperties = mSetPropertiesTask.get();
            properties = JSONUtil.jsonToMap(jsonProperties);

            // add the isFavorite property, which is not present in the remote live-object,
            // and initialize it to false
            properties.put(MLProjectContract.IS_FAVORITE, MLProjectContract.IS_FAVORITE_FALSE);
        } catch (Exception e) {
            mOnErrorListener.onError(e);
        }

        return properties;

    }

    private void storeProperties(String liveObjectId, Map<String, Object> properties) {
        Log.d(LOG_TAG, "storing properties " + properties);
        mDbController.putLiveObject(liveObjectId, properties);
    }


    private void setUIContent(Map<String, Object> liveObjectProperties) {
        Log.d(LOG_TAG, liveObjectProperties.toString());

        MLProjectPropertyProvider provider = new MLProjectPropertyProvider(liveObjectProperties);

        String title = provider.getProjectTitle();
        String group = provider.getProjectGroup();
        String description = provider.getProjectDescription();

        mObjectTitleTextView.setText(title);
        mObjectGroupTextView.setText(group);
        mObjectDescriptionTextView.setText(description);

        String imageFileName = provider.getIconFileName();
        setBackgroundImage(imageFileName);

    }

    private void setBackgroundImage(String imageFileName) {
        mSetBackgroundImageTask = new AsyncTask<String, Void, InputStream>() {
            @Override
            protected InputStream doInBackground(String... params) {
                String imageFileName = params[0];
                ContentId imageContentId = new ContentId(mLiveObjectNameID, DIRECTORY_NAME, imageFileName);

                try {
                    InputStream imageInputStream = mContentController.getInputStreamContent(imageContentId);
                    return imageInputStream;
                } catch (Exception e) {
                    e.printStackTrace();
                    mOnErrorListener.onError(e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(InputStream imageInputStream) {

                try {
                    Bitmap bitmap = Util.getBitmap(imageInputStream);
                    setBackgroundImage(bitmap);
                    imageInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    mOnErrorListener.onError(e);
                }
            }
        }.execute(imageFileName);

    }

    private void setBackgroundImage(Bitmap bitmap) {

        Activity activity = DetailFragment.this.getActivity();

        BitmapEditor bitmapEditor = new BitmapEditor(activity);

        if (bitmap != null) {
            Bitmap croppedBitmap = bitmapEditor.cropToDisplayAspectRatio(bitmap, activity.getWindowManager());
            bitmapEditor.blurBitmap(croppedBitmap, 2);
            BitmapDrawable background = new BitmapDrawable(croppedBitmap);
            mRootView.setBackgroundDrawable(background);

        }
        mProgressBar.setVisibility(View.GONE);
        mDetailInfoLayout.setVisibility(View.VISIBLE);
    }

    private void setUIListeners() {
        mIconView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // wait asynchronous tasks finish before starting another activity
                        cancelAsyncTasks();
                        // launch the media associated to the object
                        Intent viewIntent = new Intent(getActivity(), MediaViewActivity.class);
                        viewIntent.putExtra(MediaViewActivity.EXTRA_LIVE_OBJ_NAME_ID, mLiveObjectNameID);
                        getActivity().startActivity(viewIntent);
                    }
                }
        );
    }

    protected void cancelAsyncTasks() {

        if (mSetBackgroundImageTask != null) {
            mSetBackgroundImageTask.cancel(true);
        }
        if (mSetPropertiesTask != null) {
            mSetPropertiesTask.cancel(true);
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
        ZoomInOutAnimation zoomInOutAnimation = new ZoomInOutAnimation(mIconView, getActivity());
        zoomInOutAnimation.startAnimation();
    }

    public void setOnCancelListener(OnErrorListener onCancelListener) {
        mOnErrorListener = onCancelListener;
    }
}
