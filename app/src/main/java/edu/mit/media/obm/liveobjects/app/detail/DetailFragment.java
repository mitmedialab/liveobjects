package edu.mit.media.obm.liveobjects.app.detail;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import edu.mit.media.obm.liveobjects.app.LiveObjectsApplication;
import edu.mit.media.obm.liveobjects.app.data.LObjContentProvider;
import edu.mit.media.obm.liveobjects.app.data.LObjContract;
import edu.mit.media.obm.liveobjects.app.media.MediaViewActivity;
import edu.mit.media.obm.liveobjects.app.utils.Util;
import edu.mit.media.obm.liveobjects.app.widget.BitmapEditor;
import edu.mit.media.obm.liveobjects.app.widget.ZoomInOutAnimation;
import edu.mit.media.obm.liveobjects.middleware.common.MiddlewareInterface;
import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.shair.liveobjects.R;


/**
 * Created by Valerio Panzica La Manna on 09/01/15.
 * Shows the details of a connected live object and allows to play the content
 */
public class DetailFragment extends Fragment {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final String ARG_LIVE_OBJ_NAME_ID = "live_obj_name_id";

    private String mLiveObjectNameID;



    private ImageView mIconView;
    private View mLoadingPanel;
    private TextView mObjectTitleTextView;
    private TextView mObjectDescriptionTextView;
    private ProgressBar mProgressBar;

    private MiddlewareInterface mMiddleware;
    private ContentController mContentController;

    private JSONObject mJSONConfig;



    private OnErrorListener mOnErrorListener = null;

    private AsyncTask<String,Void,Void> mSetRemoteContentTask = null;


    private Uri mLiveObjectUri;

    public interface OnErrorListener {
        abstract public void onError(Exception exception);
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
    public DetailFragment() {    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLiveObjectNameID = getArguments().getString(ARG_LIVE_OBJ_NAME_ID);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        final View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mMiddleware = ((LiveObjectsApplication)getActivity().getApplication()).getMiddleware();
        mContentController = mMiddleware.getContentController();

        initUIObjects(rootView);

        setUIContent();
        setUIListeners();

        return rootView;
    }

    private void setUIContent() {
        Cursor cursor = LObjContentProvider.getLocalLiveObject(mLiveObjectNameID, getActivity());
        if (isLocallyAvailable(cursor)) {
            Log.d(LOG_TAG, "getting content from local storage");
            setLocalContent(cursor);
        }
        else {
            Log.d(LOG_TAG, "getting content from live object");
            setRemoteContent();
        }
    }

    private void initUIObjects(View rootView) {
        mIconView = (ImageView) rootView.findViewById(R.id.object_image_view);
        mLoadingPanel = rootView.findViewById(R.id.loadingPanel);
        mObjectTitleTextView = (TextView) rootView.findViewById(R.id.object_title_textview);
        mObjectDescriptionTextView = (TextView) rootView.findViewById(R.id.object_description_textview);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.detail_progress_bar);
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





    private boolean isLocallyAvailable(Cursor cursor){
        return cursor.getCount() > 0 ;
    }

    private void setLocalContent(Cursor cursor) {
        cursor.moveToFirst();
        setLocalTitle(cursor);
        setLocalBackgroundImage(cursor);
        mProgressBar.setVisibility(View.GONE);
    }

    private void setLocalTitle(Cursor cursor){
        Log.d(LOG_TAG, "cursor " + cursor);
        String title = cursor.getString(cursor.
                getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_TITLE));
        String description = cursor.getString(cursor.
                getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_DESCRIPTION));

        mObjectTitleTextView.setText(title);
        mObjectDescriptionTextView.setText(description);
    }

    private void setLocalBackgroundImage(Cursor cursor) {
        String imagePath = cursor.getString(cursor.
                getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_ICON_FILEPATH));
        try {
            File file = new File(imagePath);
            FileInputStream inputStream = new FileInputStream(file);
            Bitmap bitmap = Util.getBitmap(inputStream);
            setBackgroundImage(bitmap);
            inputStream.close();

        } catch (Exception e) {
            Log.e(LOG_TAG, "error opening file: " + imagePath, e);
        }
    }

    private void setBackgroundImage(Bitmap bitmap){

        Activity activity = DetailFragment.this.getActivity();

        BitmapEditor bitmapEditor = new BitmapEditor(activity);
        Bitmap croppedBitmap = bitmapEditor.cropToDisplayAspectRatio(bitmap, activity.getWindowManager());
        bitmapEditor.blurBitmap(croppedBitmap, 6);

        if (croppedBitmap != null ) {
            final BitmapDrawable background = new BitmapDrawable(croppedBitmap);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLoadingPanel.setBackgroundDrawable(background);
                }
            });

        }

    }

    private void setRemoteContent() {

        String mediaConfigFileName = getActivity().getResources().getString(R.string.media_config_filename) + ".jso";

        mSetRemoteContentTask = new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                String configFileName = params[0];

                InputStream inputStream = null;
                try {
                    if (mContentController == null) {
                        Log.e(LOG_TAG, "mContentController Null");
                    }

                    // retrieve JSON Object from remote
                    inputStream = mContentController.getInputStreamContent(configFileName);
                    if (inputStream == null) {
                        Log.e(LOG_TAG, "inputstream Null");

                    }

                    mJSONConfig = Util.getJSON(inputStream);
                    inputStream.close();

                    //set UI elements
                    String imageFileName = getFromJSON(mJSONConfig, "icon", null);
                    final String title = getFromJSON(mJSONConfig, "title", null);
                    final String description = getFromJSON(mJSONConfig, "description", null);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mObjectTitleTextView.setText(title);
                            mObjectDescriptionTextView.setText(description);
                        }
                    });

                    InputStream imageInputStream = mContentController.getInputStreamContent(imageFileName);
                    Bitmap bitmap = Util.getBitmap(imageInputStream);
                    setBackgroundImage(bitmap);
                    saveData(mJSONConfig, imageFileName, bitmap);
                    imageInputStream.close();



                } catch (Exception e) {
                    e.printStackTrace();
                    mOnErrorListener.onError(e);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mProgressBar.setVisibility(View.GONE);
            }
        }.execute(mediaConfigFileName);

    }

    private void saveData(JSONObject jsonObject, String imageFileName, Bitmap bitmap){
        String directoryPath = createDirectory();
        String imageFilePath = createFilePath(directoryPath, imageFileName);


        try {
            saveFile(imageFilePath, bitmap);

            mLiveObjectUri = saveToProvider(jsonObject, imageFilePath, directoryPath);
        } catch (IOException e) {
            Log.e(LOG_TAG, "error saving data", e);
        }


    }

    private String createDirectory() {
        File directory = new File(getActivity().getFilesDir(),mLiveObjectNameID);
        boolean directoryCreated =directory.mkdir();
        if (directoryCreated) {
            Log.d(LOG_TAG, "directory created: " + directory);
        }
        return directory.getAbsolutePath();
    }

    private String createFilePath(String directoryPath, String fileName) {
        return directoryPath + File.pathSeparator + fileName;
    }

    private void saveFile(String path, Bitmap bitmap) throws IOException{

        File imgFile = new File(path);
        FileOutputStream outputStream = new FileOutputStream(imgFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        outputStream.close();
    }

    private Uri saveToProvider(JSONObject jsonObject, String imagePath, String directoryPath) {

        String title = getFromJSON(jsonObject,"title", null);
        String description = getFromJSON(jsonObject,"description", null);
        String contentType = getFromJSON(jsonObject, "type", "media");
        String mediaFileName = getFromJSON(jsonObject, "filename", "media");
        String mediaFilePath = createFilePath(directoryPath, mediaFileName);


        ContentValues values = new ContentValues();
        values.put(LObjContract.LiveObjectEntry.COLUMN_NAME_TITLE,title);
        values.put(LObjContract.LiveObjectEntry.COLUMN_NAME_DESCRIPTION,description);
        values.put(LObjContract.LiveObjectEntry.COLUMN_NAME_ICON_FILEPATH, imagePath);
        values.put(LObjContract.LiveObjectEntry.COLUMN_NAME_ID, mLiveObjectNameID);
        values.put(LObjContract.LiveObjectEntry.COLUMN_NAME_MEDIA_TYPE, contentType);
        values.put(LObjContract.LiveObjectEntry.COLUMN_NAME_MEDIA_FILEPATH, mediaFilePath);


        Uri newLiveObjectUri = getActivity().getContentResolver().insert(LObjContract.LiveObjectEntry.CONTENT_URI, values);
        return newLiveObjectUri;
    }

    private String getFromJSON(JSONObject jsonObject, String key, String intermediateObjectKey) {
        String value = "";
        try {
            if (intermediateObjectKey == null) {
                value = jsonObject.getJSONObject("media-config").getString(key);
            }
            else {
                value = jsonObject.getJSONObject("media-config").getJSONObject(intermediateObjectKey).getString(key);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return value;

    }

    protected void cancelAsyncTasks() {

        if (mSetRemoteContentTask != null) {
            mSetRemoteContentTask.cancel(true);
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
