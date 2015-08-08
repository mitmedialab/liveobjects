package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import edu.mit.media.obm.liveobjects.apptidmarsh.LiveObjectsApplication;
import edu.mit.media.obm.liveobjects.apptidmarsh.detail.DetailActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.history.SavedLiveObjectsActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.apptidmarsh.profile.ProfileActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.InactiveLiveObjectDetectionEvent;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.LiveObjectNotifier;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.AnimationArrayAdapter;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.BitmapEditor;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.ExpandIconAnimation;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.MenuActions;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
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

    private ArrayAdapter<LiveObject> mAdapter;

    private ArrayList<LiveObject> mLiveObjectNamesList;
    private ArrayList<LiveObject> mActiveLiveObjectNamesList;
    private ArrayList<LiveObject> mSleepingLiveObjectNamesList;

    private View mClickedView;

    private LiveObject mSelectedLiveObject;

    private ProgressDialog mConnectingDialog;

    @Inject NetworkController mNetworkController;
    @Inject LiveObjectNotifier mLiveObjectNotifier;
    @Inject Bus mBus;

    @Bind(R.id.swipe_container) SwipeRefreshLayout mSwipeLayout;
    @Bind(R.id.live_objects_list_view) GridView mLiveObjectsGridView;
    @Bind(R.id.root_layout) LinearLayout mRootLayout;

    @OnItemClick(R.id.live_objects_list_view)
    void onLiveObjectsListViewItemClick(View view, int position) {
        // when a live object appearing in the list is clicked, connect to it
        mSelectedLiveObject = mLiveObjectNamesList.get(position);

        mConnectingDialog.setMessage("Connecting to " + mSelectedLiveObject.getLiveObjectName());
        mConnectingDialog.show();

        mNetworkController.connect(mSelectedLiveObject);

        mClickedView = view;
    }

    @OnClick(R.id.historyButton)
    void onClickHistoryButton() {
        Intent intent = new Intent(getActivity(), SavedLiveObjectsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.profileButton)
    void onClickProfileButton() {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        startActivity(intent);
    }

    public MainFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);

        DependencyInjector.inject(this, getActivity());

        setupUIElements();
        setupUIListeners();

        initNetworkListeners();

        return rootView;
    }

    private void setupUIElements() {
        mLiveObjectNamesList = new ArrayList<>();
        mActiveLiveObjectNamesList = new ArrayList<>();
        mSleepingLiveObjectNamesList = new ArrayList<>();

        mAdapter = new AnimationArrayAdapter(getActivity(), R.layout.list_item_live_objects,
                mLiveObjectNamesList);
        mLiveObjectsGridView.setAdapter(mAdapter);
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

        setBackgroundImage();
    }

    private void setBackgroundImage() {
        Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.main_background);
        BitmapEditor bitmapEditor = new BitmapEditor(getActivity());
        background = bitmapEditor.cropToDisplayAspectRatio(background, getActivity().getWindowManager());
        bitmapEditor.blurBitmap(background, 2);

        BitmapDrawable drawableBackground = new BitmapDrawable(getResources(), background);
        mRootLayout.setBackgroundDrawable(drawableBackground);
    }

    private void setupUIListeners() {
        // when refreshing start a new discovery
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mNetworkController.startDiscovery();
                mLiveObjectNotifier.wakeUp();
            }
        });
    }

    private void initNetworkListeners() {
        initDiscoveryListener();
        initConnectionListener();

        Log.v(LOG_TAG, "deleting all the network configuration related to live objects");
        if (!mNetworkController.isConnecting()) {
            mNetworkController.forgetNetworkConfigurations();
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
                mActiveLiveObjectNamesList.clear();
                for (LiveObject liveObject : liveObjectList) {
                    mActiveLiveObjectNamesList.add(liveObject);
                }

                updateLiveObjectsList();
            }
        });
    }

    private void updateLiveObjectsList() {
        mLiveObjectNamesList.clear();
        mLiveObjectNamesList.addAll(mActiveLiveObjectNamesList);

        // add only ones in active list if the same live object exists both in active and in
        // sleeping lists.
        // ToDo: should use Set<T>
        for (LiveObject liveObject : mSleepingLiveObjectNamesList) {
            boolean inActiveList = false;

            for (LiveObject activeLiveObject : mActiveLiveObjectNamesList) {
                if (liveObject.getLiveObjectName().equals(activeLiveObject.getLiveObjectName())) {
                    inActiveList = true;
                    break;
                }
            }

            if (!inActiveList) {
                mLiveObjectNamesList.add(liveObject);
            }
        }

        mAdapter.notifyDataSetChanged();
        mSwipeLayout.setRefreshing(false);
    }

    private void initConnectionListener() {
        mNetworkController.setConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected(LiveObject connectedLiveObject) {
                Log.v(LOG_TAG, String.format("onConnected(%s)", connectedLiveObject));
                if (connectedLiveObject.equals(mSelectedLiveObject)) {
                    mConnectingDialog.dismiss();

                    final TextView liveObjectTitleTextView =
                            ButterKnife.findById(mClickedView, R.id.grid_item_title_textview);

                    Animation animation = new ExpandIconAnimation(
                            getActivity().getWindowManager(), mClickedView).getAnimation();
                    animation.setFillAfter(true);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            // doesn't show the title of a live object to prevent a strange error
                            // regarding too huge texts when the icon is expanding on an emulator.
                            Log.v(LOG_TAG, "onAnimationStart()");
                            liveObjectTitleTextView.setVisibility(View.GONE);

                            mSwipeLayout.setClipChildren(false);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            Log.v(LOG_TAG, "onAnimationEnd()");

                            // when the selected live objected is connected
                            // start the corresponding detail activity
                            Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                            detailIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                            detailIntent.putExtra(DetailActivity.EXTRA_LIVE_OBJ_NAME_ID, mSelectedLiveObject.getLiveObjectName());
                            startActivityForResult(detailIntent, DETAIL_ACTIVITY_REQUEST_CODE);
                            mSelectedLiveObject = null;
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    // make views other than the clicked one invisible for z-ordering problems
                    ViewGroup viewGroup = ((ViewGroup) mClickedView.getParent());
                    int clickedIndex = viewGroup.indexOfChild(mClickedView);
                    for (int i = 0; i < viewGroup.getChildCount(); i++) {
                        if (i != clickedIndex) {
                            viewGroup.getChildAt(i).setVisibility(View.INVISIBLE);
                        }
                    }

                    mClickedView.startAnimation(animation);
                    Log.v(LOG_TAG, "starting an animation");
                }

            }
        });
    }

    @Override
    public void onStart() {
        Log.v(LOG_TAG, "onStart()");
        super.onStart();

        mBus.register(this);

        mNetworkController.start();
        mNetworkController.startDiscovery();

        mSleepingLiveObjectNamesList.clear();
        mLiveObjectNotifier.wakeUp();
    }

    @Override
    public void onStop() {
        Log.v(LOG_TAG, "onStop()");
//        mNetworkController.stop();
        super.onStop();

        mBus.unregister(this);

        mLiveObjectNotifier.cancelWakeUp();
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
        liveObject.setActive(false);
        mSleepingLiveObjectNamesList.add(liveObject);

        updateLiveObjectsList();
    }
}
