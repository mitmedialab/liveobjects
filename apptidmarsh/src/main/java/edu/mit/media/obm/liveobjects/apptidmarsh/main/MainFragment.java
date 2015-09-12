package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.noveogroup.android.log.Log;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.ButterKnife;
import edu.mit.media.obm.liveobjects.apptidmarsh.data.MLProjectContract;
import edu.mit.media.obm.liveobjects.apptidmarsh.data.MLProjectPropertyProvider;
import edu.mit.media.obm.liveobjects.apptidmarsh.detail.ContentBrowserActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.detail.DetailActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.CameraChangeEvent;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.FinishedDetectingInactiveLiveObjectEvent;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.InactiveLiveObjectDetectionEvent;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.LiveObjectNotifier;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.MenuActions;
import edu.mit.media.obm.liveobjects.driver.wifi.WifiNetworkBus;
import edu.mit.media.obm.liveobjects.driver.wifi.event.ConnectedToNetworkDeviceEvent;
import edu.mit.media.obm.liveobjects.driver.wifi.event.NetworkDevicesAvailableEvent;
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
    private static final int CONTENT_BROWSER_ACTIVITY_REQUEST_CODE = 1;

    @Inject NetworkController mNetworkController;
    @Inject LiveObjectNotifier mLiveObjectNotifier;
    @Inject Bus mBus;

    private Bus mNetworkConnectionBus;

    @BindString(R.string.arg_live_object_name_id) String EXTRA_LIVE_OBJ_NAME_ID;
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

        mNetworkConnectionBus = WifiNetworkBus.getBus();

        setupUIElements();

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

            if (mSelectedLiveObject.getStatus() == LiveObject.STATUS_ACTIVE) {
                mConnectingDialog.setMessage("Connecting to " + mSelectedLiveObject.getLiveObjectName());
                mConnectingDialog.show();

                // disable notification using Bluetooth for more stable connection to WiFi
                mLiveObjectNotifier.cancelWakeUp();

                mNetworkController.connect(mSelectedLiveObject);

                mClickedMarker = marker;
            } else if (mSelectedLiveObject.getConnectedBefore()) {
//                // TODO: 8/24/15 temporarily disabled  
//                MapLocation mapLocation = mSelectedLiveObject.getMapLocation();
//
//                Bundle arguments = new Bundle();
//                arguments.putString(EXTRA_LIVE_OBJ_NAME_ID, mSelectedLiveObject.getLiveObjectName());
//                arguments.putBoolean(EXTRA_CONNECTED_TO_LIVE_OBJ, false);
//
//                Intent intent = new Intent(getActivity(), DetailActivity.class);
//                intent.putExtra(EXTRA_ARGUMENTS, arguments);
//                startActivity(intent);
            } else {
                // cannot connect to a live object which is neither active nor connected before
            }

            return true;
        }
    }

    @Override
    public void onStart() {
        Log.v("onStart()");
        super.onStart();

        mBus.register(this);
        mNetworkConnectionBus.register(this);

        enableWifi();

        mNetworkController.start();

        Log.v("deleting all the network configuration related to live objects");
        if (!mNetworkController.isConnecting()) {
            mNetworkController.forgetNetworkConfigurations();
        }

//        mAdapter.notifyDataSetChanged();

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
            boolean isEmpty = mDbController.isLiveObjectEmpty(liveObjectName);

            LiveObject liveObject = new LiveObject(liveObjectName, mapLocation);
            liveObject.setStatus(LiveObject.STATUS_OUT_OF_SITE);
            liveObject.setConnectedBefore(!isEmpty);

            mPreviouslyDetectedLiveObjectList.add(liveObject);
        }

        updateLiveObjectList();
        registerLiveObjectMarkers();
    }

    @Override
    public void onStop() {
        Log.v("onStop()");
        mNetworkController.stop();
        super.onStop();

        mBus.unregister(this);
        mNetworkConnectionBus.unregister(this);

        mLiveObjectNotifier.cancelWakeUp();
    }

    @Subscribe
    public void updateDiscoveredLiveObjectList(NetworkDevicesAvailableEvent event) {
        List<LiveObject> discoveredLiveObjects = event.getAvailableLiveObjects();

        Log.d("discovery successfully completed");
        mActiveLiveObjectList.clear();
        Log.v("===");
        for (LiveObject liveObject : discoveredLiveObjects) {
            Log.v(liveObject.getLiveObjectName() + ", " + liveObject.getMapLocation().toString());
            liveObject.setConnectedBefore(isConnectedBefore(liveObject));
            mActiveLiveObjectList.add(liveObject);

            addLiveObjectToDb(liveObject);
        }

        updateLiveObjectList();
        registerLiveObjectMarkers();

        isWifiDiscoveryProcessRunning = false;
    }

    @Subscribe
    public void startContentBrowserActivity(ConnectedToNetworkDeviceEvent event) {
        String connectedLiveObject = event.getConnectedDeviceName();

        Log.v("onConnected(%s)", connectedLiveObject);
        if (connectedLiveObject.equals(mSelectedLiveObject)) {
            mConnectingDialog.dismiss();

            Bundle arguments = new Bundle();
            arguments.putString(EXTRA_LIVE_OBJ_NAME_ID, mSelectedLiveObject.getLiveObjectName());
            arguments.putBoolean(EXTRA_CONNECTED_TO_LIVE_OBJ, true);

            // when the selected live objected is connected
            // start the corresponding detail activity
            Intent intent = new Intent(getActivity(), ContentBrowserActivity.class);
            intent.putExtra(EXTRA_ARGUMENTS, arguments);
            startActivityForResult(intent, CONTENT_BROWSER_ACTIVITY_REQUEST_CODE);

            mSelectedLiveObject = null;
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
        Log.v("onActivityResult(requestCode=%d)", requestCode);
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == CONTENT_BROWSER_ACTIVITY_REQUEST_CODE) {
            Log.v("returned from DetailActivity");
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
        return !mDbController.isLiveObjectEmpty(liveObject.getLiveObjectName());
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
        Log.v("addDetectedBluetoothDevice()");
        LiveObject liveObject = event.mLiveObject;
        liveObject.setStatus(LiveObject.STATUS_SLEEPING);
        liveObject.setConnectedBefore(isConnectedBefore(liveObject));
        mSleepingLiveObjectList.add(liveObject);

        addLiveObjectToDb(liveObject);

        updateLiveObjectList();
        registerLiveObjectMarkers();
    }

    @Subscribe
    public void triggerLiveObjectScan(CameraChangeEvent event) {
        Log.v("triggerLiveObjectScan()");

        startDiscovery();
    }

    @Subscribe
    public void finalizeBluetoothDetectionProcess(FinishedDetectingInactiveLiveObjectEvent event) {
        Log.v("finalizeBluetoothDetectionProcess()");
        isBluetoothDiscoveryProcessRunning = false;
    }

    private void startDiscovery() {
        if (!isWifiDiscoveryProcessRunning) {
            Log.v("starting WiFi discovery");
            mNetworkController.startDiscovery();
            isWifiDiscoveryProcessRunning = true;
        }

        if (!isBluetoothDiscoveryProcessRunning) {
            Log.v("starting Bluetooth discovery");
            mSleepingLiveObjectList.clear();
            mLiveObjectNotifier.wakeUp();
            isBluetoothDiscoveryProcessRunning = true;
        }
    }

    private void addLiveObjectToDb(LiveObject liveObject) {
        for (String id : mDbController.getLiveObjectsIds()) {
            if (id.equals(liveObject.getLiveObjectName())) {
                return;
            }
        }

        // register all the detected live objects with empty properties
        Map<String, Object> emptyProperties = new HashMap<>();
        // add map location to properties
        MapLocation mapLocation = liveObject.getMapLocation();
        emptyProperties.put(MLProjectContract.MAP_LOCATION_X, mapLocation.getX());
        emptyProperties.put(MLProjectContract.MAP_LOCATION_Y, mapLocation.getY());
        emptyProperties.put(MLProjectContract.MAP_ID, mapLocation.getId());
        emptyProperties.put(MLProjectContract.IS_FAVORITE, MLProjectContract.IS_FAVORITE_FALSE);
        mDbController.putLiveObject(liveObject.getLiveObjectName(), emptyProperties);
    }
}