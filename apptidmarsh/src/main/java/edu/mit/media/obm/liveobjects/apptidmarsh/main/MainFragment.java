package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.eventbus.Subscribe;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.ButterKnife;
import edu.mit.media.obm.liveobjects.apptidmarsh.data.MLProjectPropertyProvider;
import edu.mit.media.obm.liveobjects.apptidmarsh.detail.DetailActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.InactiveLiveObjectDetectionEvent;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.LiveObjectNotifier;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.MenuActions;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.common.MapLocation;
import edu.mit.media.obm.liveobjects.middleware.control.ConnectionListener;
import edu.mit.media.obm.liveobjects.middleware.control.DiscoveryListener;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkController;
import edu.mit.media.obm.shair.liveobjects.R;

/**
 * Created by artimo14 on 8/9/15.
 */
public class MainFragment extends GroundOverlayMapFragment {
    private static final String LOG_TAG = MainFragment.class.getSimpleName();

    private static final int DETAIL_ACTIVITY_REQUEST_CODE = 1;

    @Inject NetworkController mNetworkController;
    @Inject LiveObjectNotifier mLiveObjectNotifier;
    @Inject Bus mBus;

    @BindString(R.string.arg_live_object_name_id) String EXTRA_LIVE_OBJ_NAME_ID;
    @BindString(R.string.arg_live_object_map_location_x) String EXTRA_LIVE_OBJ_MAP_LOCATION_X;
    @BindString(R.string.arg_live_object_map_location_y) String EXTRA_LIVE_OBJ_MAP_LOCATION_Y;
    @BindString(R.string.arg_live_object_map_id) String EXTRA_LIVE_OBJ_MAP_ID;
    @BindString(R.string.arg_connected_to_live_object) String EXTRA_CONNECTED_TO_LIVE_OBJ;

    private ProgressDialog mConnectingDialog;

    private ArrayList<LiveObject> mLiveObjectList = new ArrayList<>();
    private ArrayList<LiveObject> mActiveLiveObjectList = new ArrayList<>();
    private ArrayList<LiveObject> mSleepingLiveObjectList = new ArrayList<>();
    private ArrayList<LiveObject> mPreviouslyDetectedLiveObjectList = new ArrayList<>();
    private LiveObject mSelectedLiveObject;
    private Marker mClickedMarker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        ButterKnife.bind(this, rootView);
        DependencyInjector.inject(this, getActivity());

        setupUIElements();
        initNetworkListeners();

        return rootView;
    }

    private void setupUIElements() {
        /*
        mAdapter = new AnimationArrayAdapter(getActivity(), R.layout.list_item_live_objects,
                mLiveObjectNamesList);
        mLiveObjectsGridView.setAdapter(mAdapter);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        */
        mConnectingDialog = new ProgressDialog(getActivity());
        mConnectingDialog.setIndeterminate(true);
        mConnectingDialog.setCancelable(true);
        mConnectingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mNetworkController.cancelConnecting();
            }
        });

        setOnMarkerClickListener(new ConnectToLiveObjectListener());
    }

    private class ConnectToLiveObjectListener implements GoogleMap.OnMarkerClickListener {
        @Override
        public boolean onMarkerClick(Marker marker) {
            // when a live object appearing in the list is clicked, connect to it
            mSelectedLiveObject = null;
            for (LiveObject liveObject : mLiveObjectList) {
                String markerTitle = marker.getTitle();
                String liveObjectName = liveObject.getLiveObjectName();

                if (markerTitle.equals(liveObjectName)) {
                    mSelectedLiveObject = liveObject;
                }
            }

            if (mSelectedLiveObject == null) {
                throw new IllegalStateException(
                        "clicked live object was not found in the list of detected live objects");
            }

            mConnectingDialog.setMessage("Connecting to " + mSelectedLiveObject.getLiveObjectName());
            mConnectingDialog.show();

            mNetworkController.connect(mSelectedLiveObject);

            mClickedMarker = marker;

            return true;
        }
    }

    @Override
    public void onStart() {
        Log.v(LOG_TAG, "onStart()");
        super.onStart();

        mBus.register(this);

        mNetworkController.start();
        mNetworkController.startDiscovery();

        mSleepingLiveObjectList.clear();
        mLiveObjectNotifier.wakeUp();
    }

    @Override
    public void onResume() {
        super.onResume();

        mPreviouslyDetectedLiveObjectList.clear();

        List<Map<String, Object>> allLiveObjects = mDbController.getAllLiveObjectsProperties();
        for (Map<String, Object> liveObjectProperties : allLiveObjects) {
            MLProjectPropertyProvider provider = new MLProjectPropertyProvider(liveObjectProperties);
            String liveObjectName = provider.getId();
            MapLocation mapLocation = new MapLocation(
                    provider.getMapLocationX(), provider.getMapLocationY(), provider.getMapId());
            LiveObject liveObject = new LiveObject(liveObjectName, mapLocation);
            liveObject.setStatus(LiveObject.STATUS_CONNECTED_BEFORE);

            mPreviouslyDetectedLiveObjectList.add(liveObject);
        }

        updateLiveObjectList();
        updateLiveObjectMarkers();
    }

    @Override
    public void onStop() {
        Log.v(LOG_TAG, "onStop()");
//        mNetworkController.stop();
        super.onStop();

        mBus.unregister(this);

        mLiveObjectNotifier.cancelWakeUp();
    }

    private void initNetworkListeners() {
        mNetworkController.setDiscoveryListener(new LiveObjectDiscoveryListener());
        mNetworkController.setConnectionListener(new LiveObjectConnectionListener());

        Log.v(LOG_TAG, "deleting all the network configuration related to live objects");
        if (!mNetworkController.isConnecting()) {
            mNetworkController.forgetNetworkConfigurations();
        }

//        mAdapter.notifyDataSetChanged();
    }

    private class LiveObjectDiscoveryListener implements DiscoveryListener {
        @Override
        public void onDiscoveryStarted() {
            Log.d(LOG_TAG, "discovery started");
        }

        @Override
        public void onLiveObjectsDiscovered(List<LiveObject> liveObjectList) {
            Log.d(LOG_TAG, "discovery successfully completed");
            mActiveLiveObjectList.clear();
            for (LiveObject liveObject : liveObjectList) {
                mActiveLiveObjectList.add(liveObject);
            }

            updateLiveObjectList();
            updateLiveObjectMarkers();
        }
    }

    class LiveObjectConnectionListener implements ConnectionListener {
        @Override
        public void onConnected(LiveObject connectedLiveObject) {
            Log.v(LOG_TAG, String.format("onConnected(%s)", connectedLiveObject));
            if (connectedLiveObject.equals(mSelectedLiveObject)) {
                mConnectingDialog.dismiss();

                // when the selected live objected is connected
                // start the corresponding detail activity
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                detailIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                MapLocation mapLocation = mSelectedLiveObject.getMapLocation();
                detailIntent.putExtra(EXTRA_LIVE_OBJ_NAME_ID, mSelectedLiveObject.getLiveObjectName());
                detailIntent.putExtra(EXTRA_LIVE_OBJ_MAP_LOCATION_X, mapLocation.getCoordinateX());
                detailIntent.putExtra(EXTRA_LIVE_OBJ_MAP_LOCATION_Y, mapLocation.getCoordinateY());
                detailIntent.putExtra(EXTRA_LIVE_OBJ_MAP_ID, mapLocation.getMapId());
                detailIntent.putExtra(EXTRA_CONNECTED_TO_LIVE_OBJ, true);
                startActivityForResult(detailIntent, DETAIL_ACTIVITY_REQUEST_CODE);
                mSelectedLiveObject = null;
            }
        }
    }

    private void updateLiveObjectList() {
        mLiveObjectList.clear();
        mLiveObjectList.addAll(mActiveLiveObjectList);

        // add only ones in active list if the same live object exists both in active and in
        // sleeping lists.
        // ToDo: should use Set<T>
        for (LiveObject liveObject : mSleepingLiveObjectList) {
            boolean inActiveList = false;

            for (LiveObject activeLiveObject : mActiveLiveObjectList) {
                if (liveObject.getLiveObjectName().equals(activeLiveObject.getLiveObjectName())) {
                    inActiveList = true;
                    break;
                }
            }

            if (!inActiveList) {
                mLiveObjectList.add(liveObject);
            }
        }

        for (LiveObject liveObject : mPreviouslyDetectedLiveObjectList) {
            boolean inActiveList = false;

            for (LiveObject activeLiveObject : mActiveLiveObjectList) {
                if (liveObject.getLiveObjectName().equals(activeLiveObject.getLiveObjectName())) {
                    inActiveList = true;
                    break;
                }
            }

            if (!inActiveList) {
                mLiveObjectList.add(liveObject);
            }
        }
    }

    private void updateLiveObjectMarkers() {
        for (LiveObject liveObject : mLiveObjectList) {
            addLiveObjectMarker(liveObject, true);
        }
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

                // recreate the MainActivity to reset the UI state
                MenuActions.goToHome(getActivity());
            }
        }
    }

    @Subscribe
    public void addDetectedBluetoothDevice(InactiveLiveObjectDetectionEvent event) {
        Log.v(LOG_TAG, "addDetectedBluetoothDevice()");
        LiveObject liveObject = new LiveObject(event.mDeviceName);
        liveObject.setStatus(LiveObject.STATUS_SLEEPING);
        mSleepingLiveObjectList.add(liveObject);

        updateLiveObjectList();
        updateLiveObjectMarkers();
    }
}