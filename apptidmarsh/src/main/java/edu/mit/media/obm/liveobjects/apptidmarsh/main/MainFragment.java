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

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.ButterKnife;
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
import edu.mit.media.obm.liveobjects.driver.wifi.event.NetworkConnectedEvent;
import edu.mit.media.obm.liveobjects.driver.wifi.event.NetworkDevicesAvailableEvent;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.common.MapLocation;
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

    @Inject DiscoveryInfo mDiscoveryInfo;

    private Bus mNetworkConnectionBus;

    @BindString(R.string.arg_live_object_name_id) String EXTRA_LIVE_OBJ_NAME_ID;
    @BindString(R.string.arg_connected_to_live_object) String EXTRA_CONNECTED_TO_LIVE_OBJ;
    @BindString(R.string.extra_arguments) String EXTRA_ARGUMENTS;

    private ProgressDialog mConnectingDialog;

    private LiveObject mSelectedLiveObject;

    private boolean wifiDiscoveryProcessRunning = false;
    private boolean bluetoothDiscoveryProcessRunning = false;

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
            mSelectedLiveObject = findLiveObjectFromMarker(marker);

            if (mSelectedLiveObject.getStatus() == LiveObject.STATUS_ACTIVE) {
                mConnectingDialog.setMessage("Connecting to " + mSelectedLiveObject.getName());
                mConnectingDialog.show();

                // disable notification using Bluetooth for more stable connection to WiFi
                mLiveObjectNotifier.cancelWakeUp();

                mNetworkController.connect(mSelectedLiveObject);
            } else if (mSelectedLiveObject.getConnectedBefore()) {
//                // TODO: 8/24/15 temporarily disabled  @Inject
//                MapLocation mapLocation = mSelectedLiveObject.getMapLocation();
//
//                Bundle arguments = new Bundle();
//                arguments.putString(EXTRA_LIVE_OBJ_NAME_ID, mSelectedLiveObject.getName());
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

    private LiveObject findLiveObjectFromMarker(Marker marker) {
        // when a live object appearing in the list is clicked, connect to it
        String markerTitle = marker.getTitle();
        LiveObject foundLiveObject = null;

        for (LiveObject liveObject : mDiscoveryInfo.getAllLiveObjects()) {
            String liveObjectName = liveObject.getName();

            if (markerTitle.equals(liveObjectName)) {
                foundLiveObject = liveObject;
            }
        }

        if (foundLiveObject == null) {
            throw new IllegalStateException(
                    "clicked live object was not found in the list of detected live objects");
        }

        return foundLiveObject;
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

        mDiscoveryInfo.clearLostLiveObject();

        List<Map<String, Object>> allLiveObjects = mDbController.getAllLiveObjectsProperties();
        for (Map<String, Object> liveObjectProperties : allLiveObjects) {
            MLProjectPropertyProvider provider = new MLProjectPropertyProvider(liveObjectProperties);
            String liveObjectName = provider.getId();
            MapLocation mapLocation = new MapLocation(
                    provider.getMapLocationX(), provider.getMapLocationY(), provider.getMapId());

            LiveObject liveObject = new LiveObject(liveObjectName, mapLocation);
            mDiscoveryInfo.addLostLiveObject(liveObject);
        }

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
        mDiscoveryInfo.clearActiveLiveObject();
        Log.v("===");
        for (LiveObject liveObject : discoveredLiveObjects) {
            Log.v(liveObject.getName() + ", " + liveObject.getMapLocation().toString());
            mDiscoveryInfo.addActiveLiveObject(liveObject);
        }

        registerLiveObjectMarkers();

        wifiDiscoveryProcessRunning = false;
    }

    @Subscribe
    public void startContentBrowserActivity(NetworkConnectedEvent event) {
        LiveObject connectedLiveObject = event.getConnectedLiveObject();

        mConnectingDialog.dismiss();

        Log.v("startContentBrowserActivity(%s)", connectedLiveObject);
        if (isConnectedToTargetLiveObject(connectedLiveObject)) {
            Log.v("starting Content Browser Activity");
            Bundle arguments = new Bundle();
            arguments.putString(EXTRA_LIVE_OBJ_NAME_ID, mSelectedLiveObject.getName());
            arguments.putBoolean(EXTRA_CONNECTED_TO_LIVE_OBJ, true);

            // when the selected live objected is connected
            // start the corresponding detail activity
            Intent intent = new Intent(getActivity(), ContentBrowserActivity.class);
            intent.putExtra(EXTRA_ARGUMENTS, arguments);
            startActivityForResult(intent, CONTENT_BROWSER_ACTIVITY_REQUEST_CODE);

            mSelectedLiveObject = null;
        } else {
            Log.v("failed to connect to target live object");
        }
    }

    private boolean isConnectedToTargetLiveObject(LiveObject connectedLiveObject) {
        return (connectedLiveObject != null)
                && connectedLiveObject.equals(mSelectedLiveObject);
    }

    private void registerLiveObjectMarkers() {
        for (LiveObject liveObject : mDiscoveryInfo.getAllLiveObjects()) {
            boolean currentLocation = (liveObject.getStatus() != LiveObject.STATUS_LOST);
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
        mDiscoveryInfo.addSleepingLiveObject(liveObject);

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
        bluetoothDiscoveryProcessRunning = false;
    }

    private void startDiscovery() {
        if (!wifiDiscoveryProcessRunning) {
            Log.v("starting WiFi discovery");
            mNetworkController.startDiscovery();
            wifiDiscoveryProcessRunning = true;
        }

        if (!bluetoothDiscoveryProcessRunning) {
            Log.v("starting Bluetooth discovery");
            mDiscoveryInfo.clearSleepingLiveObject();
            mLiveObjectNotifier.wakeUp();
            bluetoothDiscoveryProcessRunning = true;
        }
    }
}