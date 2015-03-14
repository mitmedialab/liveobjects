package edu.mit.media.obm.liveobjects.app;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.mit.media.obm.liveobjects.app.widget.AnimationArrayAdapter;
import edu.mit.media.obm.liveobjects.app.widget.ExpandIconAnimation;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.common.MiddlewareInterface;
import edu.mit.media.obm.liveobjects.middleware.control.ConnectionListener;
import edu.mit.media.obm.liveobjects.middleware.control.DiscoveryListener;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkController;
import edu.mit.media.obm.shair.liveobjects.R;

/**
 * Created by Valerio Panzica La Manna on 08/12/14.
 */
public class MainFragment extends Fragment {
    private static final String LOG_TAG = MainFragment.class.getSimpleName();

    private static final int DETAIL_ACTIVITY_REQUEST_CODE = 1;

    private SwipeRefreshLayout mSwipeLayout;
    private GridView mLiveObjectsGridView;

    private ArrayAdapter<LiveObject> mAdapter;
    private ArrayList<LiveObject> mLiveObjectNamesList;

    private NetworkController mNetworkController;

    private LiveObject mSelectedLiveObject;

    private ProgressDialog mConnectingDialog;

    private MiddlewareInterface mMiddleware;

    public MainFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        setupUIElements(rootView);
        setupUIListeners();

        mMiddleware = ((LiveObjectsApplication) getActivity().getApplication()).getMiddleware();
        initNetworkListeners();

        return rootView;
    }

    private void setupUIElements(View rootView) {
        mLiveObjectsGridView = (GridView) rootView.findViewById(R.id.live_objects_list_view);
        mLiveObjectNamesList = new ArrayList<>();
        mAdapter = new AnimationArrayAdapter<>(getActivity(), R.layout.list_item_live_objects,
                R.id.grid_item_title_textview, mLiveObjectNamesList);
        mLiveObjectsGridView.setAdapter(mAdapter);
        mSwipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        mConnectingDialog = new ProgressDialog(getActivity());
        mConnectingDialog.setIndeterminate(true);
        mConnectingDialog.setCancelable(true);
        mConnectingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mNetworkController.cancelConnecting();
            }
        });
    }

    private void setupUIListeners() {
        // when refreshing start a new discovery
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mNetworkController.startDiscovery();
            }
        });

        // when a live object appearing in the list is clicked, connect to it
        mLiveObjectsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedLiveObject = mLiveObjectNamesList.get(position);

//                mConnectingDialog.setMessage(
//                        "Connecting to " + mSelectedLiveObject.getLiveObjectName());
//                mConnectingDialog.show();
//
//                mNetworkController.connect(mSelectedLiveObject);

                Animation animation = new ExpandIconAnimation(
                        getActivity().getWindowManager(), view).getAnimation();
                view.setAnimation(animation);
                animation.start();
            }
        });
    }

    private void initNetworkListeners() {
        mNetworkController = mMiddleware.getNetworkController();

        initDiscoveryListener();
        initConnectionListener();

        // p    opulate with dummy
        for (int i = 0; i < 10; i++) {
            LiveObject liveObject = new LiveObject("test" + i);
            mLiveObjectNamesList.add(liveObject);
        }

        mAdapter.notifyDataSetChanged();
    }

    private void initDiscoveryListener() {
        mNetworkController.setDiscoveryListener(new DiscoveryListener() {
            @Override
            public void onDiscoveryStarted() {
                Log.d(LOG_TAG, "discovery started");
            }

            @Override
            public void onLiveObjectsDiscovered(List<LiveObject> liveObjectList) {
                Log.d(LOG_TAG, "discovery successfully completed");
                mLiveObjectNamesList.clear();
                for (LiveObject liveObject : liveObjectList) {
                    mLiveObjectNamesList.add(liveObject);
                }
                mAdapter.notifyDataSetChanged();
                mSwipeLayout.setRefreshing(false);
            }
        });
    }

    private void initConnectionListener() {
        mNetworkController.setConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected(LiveObject connectedLiveObject) {
                if (connectedLiveObject.equals(mSelectedLiveObject)) {
                    // when the selected live objected is connected
                    // start the corresponding detail activity
                    Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                    //TODO
                    //detailIntent.putExtra(LiveObjectsManager.EXTRA_LIVE_OBJECT, connectedLiveObject);
                    startActivityForResult(detailIntent, DETAIL_ACTIVITY_REQUEST_CODE);
                    mSelectedLiveObject = null;

                    mConnectingDialog.dismiss();
                }

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mNetworkController.start();
        mNetworkController.startDiscovery();
    }


    @Override
    public void onStop() {
        mNetworkController.stop();
        super.onStop();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.v(LOG_TAG, String.format("onActivityResult(requestCode=%d)", requestCode));
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == DETAIL_ACTIVITY_REQUEST_CODE) {
            Log.v(LOG_TAG, "returned from DetailActivity");
            final String errorMessage;

            if (resultCode == DetailActivity.RESULT_CONNECTION_ERROR) {
                errorMessage = "a network error in the live object";
            } else if (resultCode == DetailActivity.RESULT_JSON_ERROR) {
                errorMessage = "An error in the contents in the live object";
            } else {
                errorMessage = null;
            }

            if (errorMessage != null) {
                getActivity().runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }
}
