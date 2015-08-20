package edu.mit.media.obm.liveobjects.apptidmarsh.detail;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.ButterKnife;
import edu.mit.media.obm.liveobjects.apptidmarsh.data.MLProjectContract;
import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.middleware.common.ContentId;
import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;
import edu.mit.media.obm.liveobjects.middleware.util.JSONUtil;
import edu.mit.media.obm.shair.liveobjects.R;

/**
 * Created by artimo14 on 8/19/15.
 */
public class ContentBrowserFragment extends Fragment {
    @BindString(R.string.media_config_filename) String MEDIA_CONFIG_FILE_NAME;
    @BindString(R.string.dir_contents) String DIRECTORY_NAME;
    @BindString(R.string.arg_live_object_name_id) String ARG_LIVE_OBJ_NAME_ID;
    @BindString(R.string.arg_connected_to_live_object) String ARG_CONNECTED_TO_LIVE_OBJ;

    @Inject ContentController mContentController;
    @Inject DbController mDbController;

    String mLiveObjectName;
    boolean mConnectedToLiveObject;


    private DetailFragment.OnErrorListener mOnErrorListener = null;

    private AsyncTask<String, Void, JSONObject> mSetPropertiesTask = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        ButterKnife.bind(this, rootView);
        DependencyInjector.inject(this, getActivity());

        Bundle arguments = getArguments();
        if (arguments != null) {
            mLiveObjectName = arguments.getString(ARG_LIVE_OBJ_NAME_ID);
            mConnectedToLiveObject = arguments.getBoolean(ARG_CONNECTED_TO_LIVE_OBJ);
        }

        Map<String, Object> liveObjectProperties = fetchProperties(mLiveObjectName);
        setUIContent(liveObjectProperties);

        return rootView;
    }

    private Map<String, Object> fetchProperties(final String liveObjectId) {
        String mediaConfigFileName = MEDIA_CONFIG_FILE_NAME + ".jso";

        mSetPropertiesTask =
                new AsyncTask<String, Void, JSONObject>() {
                    @Override
                    protected JSONObject doInBackground(String... params) {
                        String configFileName = params[0];

                        InputStream inputStream;
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

    private void setUIContent(Map<String, Object> liveObjectProperties) {

    }

    protected void cancelAsyncTasks() {
        if (mSetPropertiesTask != null) {
            mSetPropertiesTask.cancel(true);
        }
    }

    public void setOnCancelListener(DetailFragment.OnErrorListener onCancelListener) {
        mOnErrorListener = onCancelListener;
    }
}
