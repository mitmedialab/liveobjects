package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Camera;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.ButterKnife;
import dagger.ObjectGraph;
import edu.mit.media.obm.liveobjects.apptidmarsh.data.MLProjectPropertyProvider;
import edu.mit.media.obm.liveobjects.apptidmarsh.detail.DetailActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.CameraChangeEvent;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.FinishedDetectingInactiveLiveObjectEvent;
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
    @BindString(R.string.extra_arguments) String EXTRA_ARGUMENTS;

    private ProgressDialog mConnectingDialog;

    private ArrayList<LiveObject> mLiveObjectList = new ArrayList<>();
    private ArrayList<LiveObject> mActiveLiveObjectList = new ArrayList<>();
    private ArrayList<LiveObject> mSleepingLiveObjectList = new ArrayList<>();
    private ArrayList<LiveObject> mPreviouslyDetectedLiveObjectList = new ArrayList<>();
    private LiveObject mSelectedLiveObject;
    private Marker mClickedMarker;

    private boolean isWifiDiscoveryProcessRunning = false;
    private boolean isBluetoothDiscoveryProcessRunning = false;

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

            // disable notification using Bluetooth for more stable connection to WiFi
            mLiveObjectNotifier.cancelWakeUp();

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

        enableWifi();

        mNetworkController.start();

        startDiscovery();
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
            liveObject.setStatus(LiveObject.STATUS_OUT_OF_SITE);
            liveObject.setConnectedBefore(true);

            mPreviouslyDetectedLiveObjectList.add(liveObject);
        }

        updateLiveObjectList();
        registerLiveObjectMarkers();
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
                liveObject.setConnectedBefore(isConnectedBefore(liveObject));
                mActiveLiveObjectList.add(liveObject);

                // register all the detected live objects with empty properties
                Map<String, Object> emptyProperties = new HashMap<>();
                mDbController.putLiveObject(liveObject.getLiveObjectName(), emptyProperties);
            }

            updateLiveObjectList();
            registerLiveObjectMarkers();

            isWifiDiscoveryProcessRunning = false;
        }
    }

    class LiveObjectConnectionListener implements ConnectionListener {
        @Override
        public void onConnected(LiveObject connectedLiveObject) {
            Log.v(LOG_TAG, String.format("onConnected(%s)", connectedLiveObject));
            if (connectedLiveObject.equals(mSelectedLiveObject)) {
                mConnectingDialog.dismiss();


                Bundle arguments = new Bundle();
                MapLocation mapLocation = mSelectedLiveObject.getMapLocation();
                arguments.putString(EXTRA_LIVE_OBJ_NAME_ID, mSelectedLiveObject.getLiveObjectName());
                arguments.putInt(EXTRA_LIVE_OBJ_MAP_LOCATION_X, mapLocation.getCoordinateX());
                arguments.putInt(EXTRA_LIVE_OBJ_MAP_LOCATION_Y, mapLocation.getCoordinateY());
                arguments.putInt(EXTRA_LIVE_OBJ_MAP_ID, mapLocation.getMapId());
                arguments.putBoolean(EXTRA_CONNECTED_TO_LIVE_OBJ, true);

                // when the selected live objected is connected
                // start the corresponding detail activity
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                detailIntent.putExtra(EXTRA_ARGUMENTS, arguments);
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

    private void registerLiveObjectMarkers() {
        for (LiveObject liveObject : mLiveObjectList) {
            boolean currentLocation = (liveObject.getStatus() != LiveObject.STATUS_OUT_OF_SITE);
            boolean connectedBefore = liveObject.getConnectedBefore();
            updateLiveObjectMarker(liveObject, currentLocation, connectedBefore);
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

    private boolean isConnectedBefore(LiveObject liveObject) {
        Boolean found = false;

        List<Map<String, Object>> allLiveObjects = mDbController.getAllLiveObjectsProperties();
        for (Map<String, Object> liveObjectProperties : allLiveObjects) {
            MLProjectPropertyProvider provider = new MLProjectPropertyProvider(liveObjectProperties);
            String liveObjectName = provider.getId();
            if (liveObject.getLiveObjectName().equals(liveObjectName)) {
                found = true;
            }
        }

        return found;
    }

    private void enableWifi() {
        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            Toast.makeText(getActivity(), "Turning on WiFi", Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe
    public void addDetectedBluetoothDevice(InactiveLiveObjectDetectionEvent event) {
        Log.v(LOG_TAG, "addDetectedBluetoothDevice()");
        LiveObject liveObject = event.mLiveObject;
        liveObject.setStatus(LiveObject.STATUS_SLEEPING);
        liveObject.setConnectedBefore(isConnectedBefore(liveObject));
        mSleepingLiveObjectList.add(liveObject);

        updateLiveObjectList();
        registerLiveObjectMarkers();
    }

    @Subscribe
    public void triggerLiveObjectScan(CameraChangeEvent event) {
        Log.v(LOG_TAG, "triggerLiveObjectScan()");

        startDiscovery();
    }

    @Subscribe
    public void finalizeBluetoothDetectionProcess(FinishedDetectingInactiveLiveObjectEvent event) {
        Log.v(LOG_TAG, "finalizeBluetoothDetectionProcess()");
        isBluetoothDiscoveryProcessRunning = false;
    }

    private void startDiscovery() {
        if (!isWifiDiscoveryProcessRunning) {
            Log.v(LOG_TAG, "starting WiFi discovery");
            mNetworkController.startDiscovery();
            isWifiDiscoveryProcessRunning = true;
        }

        if (!isBluetoothDiscoveryProcessRunning) {
            Log.v(LOG_TAG, "starting Bluetooth discovery");
            mSleepingLiveObjectList.clear();
            mLiveObjectNotifier.wakeUp();
            isBluetoothDiscoveryProcessRunning = true;
        }
    }
}