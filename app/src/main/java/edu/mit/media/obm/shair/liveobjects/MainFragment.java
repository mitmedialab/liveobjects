    package edu.mit.media.obm.shair.liveobjects;

    import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

    /**
     * Created by Valerio Panzica La Manna on 08/12/14.
     * Example code on how to show the number of elements in a FlashAir folder and the name of each element.
     */
    public class MainFragment extends Fragment {
        private static final String LOG_TAG = MainFragment.class.getSimpleName();

        private ListView mLiveObjectsListView;

        private ArrayAdapter<String> mAdapter;
        private ArrayList<String> mLiveObjectsList;


        private WifiManager mWifiManager;
        private WifiReceiver mWifiReceiver;

        private static final String NETWORK_PASSWORD = "12345678";
        List<ScanResult> mWifiList;

        private String networkSSID = "";

        static String[] NETWORKS;



        public MainFragment() {
            super();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            Button button = (Button) rootView.findViewById(R.id.button1);
            mLiveObjectsListView = (ListView) rootView.findViewById(R.id.live_objects_list_view);
            mLiveObjectsList = new ArrayList<>();
            mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mLiveObjectsList);
            mLiveObjectsListView.setAdapter(mAdapter);

            mLiveObjectsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String liveObjectName = mLiveObjectsList.get(position);
                    connectToLiveObject(liveObjectName);
                }
            });

            mWifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
            mWifiReceiver = new WifiReceiver();

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    switch (v.getId()) {
                        case R.id.button1:
                            searchLiveObjects();
                        break;

                    }
                }
            });
            return rootView;
        }

        @Override
        public void onResume() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            getActivity().registerReceiver(mWifiReceiver, intentFilter);
            super.onResume();

        }

        @Override
        public void onStop() {
            networkSSID = "";
            getActivity().unregisterReceiver(mWifiReceiver);
            super.onStop();

        }

        private void searchLiveObjects() {

            new AsyncTask<Void, Void,Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    mWifiManager.startScan();
                    return null;
                }
            }.execute();

        }

        private void connectToLiveObject(final String liveObjectName) {
            //TODO pass liveObjectName as parameter of the asyncTask
            new AsyncTask<Void,Void,Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    networkSSID = Util.convertLiveObjectNameToSSID(liveObjectName);
                    WifiConfiguration conf = new WifiConfiguration();
                    conf.SSID = "\"" + networkSSID + "\"";
                    conf.preSharedKey = "\"" + NETWORK_PASSWORD + "\"";
                    mWifiManager.addNetwork(conf);
                    int netId = mWifiManager.addNetwork(conf);
                    mWifiManager.disconnect();
                    mWifiManager.enableNetwork(netId, true);
                    mWifiManager.reconnect();
                    return null;
                }

            }.execute();

        }

        class WifiReceiver extends BroadcastReceiver {
            public void onReceive(Context c, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION :

                        Log.d(LOG_TAG, "SCANNING WIFI");
                        mWifiList = mWifiManager.getScanResults();
                        NETWORKS = new String[mWifiList.size()];
                        mLiveObjectsList.clear();
                        for (ScanResult scanResult : mWifiList) {
                            Log.d(LOG_TAG, "scanResult: " + scanResult.SSID.toString());
                            if (Util.isLiveObject(scanResult.SSID)){
                                String liveObjectName = Util.convertSSIDToLiveObjectName(scanResult.SSID);
                                mLiveObjectsList.add(liveObjectName);
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                        break;
                    case WifiManager.NETWORK_STATE_CHANGED_ACTION :
                        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                        NetworkInfo.State state = networkInfo.getState();

                        if(state.equals(NetworkInfo.State.CONNECTED))
                        {
                            if (!networkSSID.isEmpty()) {
                                Intent viewIntent = new Intent(getActivity(), VideoViewActivity.class);
                                getActivity().startActivity(viewIntent);
                            }
                        }
                }

            }

        }

    }
