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

    import edu.mit.media.obm.liveobjects.middleware.LiveObject;
    import edu.mit.media.obm.liveobjects.middleware.LiveObjectsList;
    import edu.mit.media.obm.liveobjects.middleware.LiveObjectsManager;

    /**
     * Created by Valerio Panzica La Manna on 08/12/14.     *
     */
    public class MainFragment extends Fragment {
        private static final String LOG_TAG = MainFragment.class.getSimpleName();

        private SwipeRefreshLayout mSwipeLayout;
        private ListView mLiveObjectsListView;

        private ArrayAdapter<LiveObject> mAdapter;
        private ArrayList<LiveObject> mLiveObjectNamesList;

        private LiveObjectsManager mLiveObjectsManager;

        private LiveObject currentlySelectedLiveObject = null;

        public MainFragment() {
            super();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            setupUIElements(rootView);
            setupUIListeners();

            initLiveObjectManager();

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
                    mLiveObjectsManager.startDiscovery();
                }
            });

            // when a live object appearing in the list is clicked, connect to it
            mLiveObjectsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    currentlySelectedLiveObject = mLiveObjectNamesList.get(position);
                    mLiveObjectsManager.connect(currentlySelectedLiveObject);

                }
            });
        }

        private void initLiveObjectManager() {
            mLiveObjectsManager = new LiveObjectsManager();
            mLiveObjectsManager.initialize(getActivity());
            initDiscoveryListener();
            initConnectionListener();
        }

        private void initDiscoveryListener() {
            mLiveObjectsManager.setDiscoveryListener(new LiveObjectsManager.DiscoveryListener() {
                @Override
                public void onDiscoveryStarted() {
                    Log.d(LOG_TAG, "discovery started");
                }

                @Override
                public void onLiveObjectsAvailable(LiveObjectsList liveObjectsList) {
                    Log.d(LOG_TAG, "discovery successfully completed");
                    mLiveObjectNamesList.clear();
                    for (LiveObject liveObject: liveObjectsList) {
                        mLiveObjectNamesList.add(liveObject);
                    }
                    mAdapter.notifyDataSetChanged();
                    mSwipeLayout.setRefreshing(false);
                }
            });
        }

        private void initConnectionListener() {
            mLiveObjectsManager.setConnectionListener(new LiveObjectsManager.ConnectionListener() {
                @Override
                public void onConnectionStarted() {
                    Log.d(LOG_TAG, "connection started");
                }

                @Override
                public void onConnected(LiveObject connectedLiveObject) {

                    if (isCurrentlySelectedLiveObject(connectedLiveObject)) {
                        // when the selected live objected is connected
                        // start the corresponding detail activity
                        Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                        detailIntent.putExtra(LiveObjectsManager.EXTRA_LIVE_OBJECT, connectedLiveObject);
                        getActivity().startActivity(detailIntent);
                    }
                }

                @Override
                public void onDisconnected() {
                    Log.d(LOG_TAG, "disconnected");
                }

            });
        }

        private boolean isCurrentlySelectedLiveObject( LiveObject liveObject) {
            return currentlySelectedLiveObject != null &&
                    liveObject.equals(currentlySelectedLiveObject);
        }

        @Override
        public void onResume() {
            mLiveObjectsManager.startDiscovery();
            super.onResume();

        }

        @Override
        public void onStop() {

            //getActivity().unregisterReceiver(mWifiReceiver);
            super.onStop();

        }


    }
