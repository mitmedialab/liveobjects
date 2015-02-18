    package edu.mit.media.obm.shair.liveobjects;

    import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import edu.mit.media.obm.liveobjects.driver.wifi.WifiDriver;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.control.ConnectionListener;
import edu.mit.media.obm.liveobjects.middleware.control.DiscoveryListener;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkBridge;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkController;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkDriver;

    /**
     * Created by Valerio Panzica La Manna on 08/12/14.     *
     */
    public class MainFragment extends Fragment {
        private static final String LOG_TAG = MainFragment.class.getSimpleName();

        private SwipeRefreshLayout mSwipeLayout;
        private ListView mLiveObjectsListView;

        private ArrayAdapter<LiveObject> mAdapter;
        private ArrayList<LiveObject> mLiveObjectNamesList;

        private NetworkController mNetworkController;

        private LiveObject mSelectedLiveObject;



        public MainFragment() {
            super();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            setupUIElements(rootView);
            setupUIListeners();

            initMiddleware();

            return rootView;
        }

        private void setupUIElements(View rootView) {
            mLiveObjectsListView = (ListView) rootView.findViewById(R.id.live_objects_list_view);
            mLiveObjectNamesList = new ArrayList<>();
            mAdapter = new ArrayAdapter<LiveObject>(getActivity(), R.layout.list_item_live_objects, R.id.list_item_title_textview, mLiveObjectNamesList);
            mLiveObjectsListView.setAdapter(mAdapter);
            mSwipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
            mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);
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
            mLiveObjectsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mSelectedLiveObject = mLiveObjectNamesList.get(position);
                    mNetworkController.connect(mSelectedLiveObject);

                }
            });
        }

        private void initMiddleware() {
            //TODO create a middleware class as a wrapper for all controllers

            NetworkDriver networkDriver = new WifiDriver(getActivity());
            mNetworkController = new NetworkBridge(networkDriver);
            initDiscoveryListener();
            initConnectionListener();


        }

        private void initDiscoveryListener() {
            mNetworkController.setDiscoveryListener( new DiscoveryListener() {
                @Override
                public void onDiscoveryStarted() {
                    Log.d(LOG_TAG, "discovery started");
                }

                @Override
                public void onLiveObjectsDiscovered(List<LiveObject> liveObjectList) {
                    Log.d(LOG_TAG, "discovery successfully completed");
                    mLiveObjectNamesList.clear();
                    for (LiveObject liveObject: liveObjectList) {
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
                        getActivity().startActivity(detailIntent);
                        mSelectedLiveObject = null;
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


    }
