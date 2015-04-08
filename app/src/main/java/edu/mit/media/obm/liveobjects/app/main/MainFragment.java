package edu.mit.media.obm.liveobjects.app.main;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.mit.media.obm.liveobjects.app.detail.DetailActivity;
import edu.mit.media.obm.liveobjects.app.LiveObjectsApplication;
import edu.mit.media.obm.liveobjects.app.profile.ProfileActivity;
import edu.mit.media.obm.liveobjects.app.widget.AnimationArrayAdapter;
import edu.mit.media.obm.liveobjects.app.widget.BitmapEditor;
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

    private View mClickedView;

    private NetworkController mNetworkController;

    private LiveObject mSelectedLiveObject;

    private ProgressDialog mConnectingDialog;

    private MiddlewareInterface mMiddleware;

    private Button mHistoryButton;
    private Button mProfileButton;

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

        mHistoryButton = (Button) rootView.findViewById(R.id.historyButton);
        mProfileButton = (Button) rootView.findViewById(R.id.profileButton);

        setBackgroundImage(rootView);
    }

    private void setBackgroundImage(View rootView) {
        Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.main_background);
        BitmapEditor bitmapEditor = new BitmapEditor(getActivity());
        background = bitmapEditor.cropToDisplayAspectRatio(background, getActivity().getWindowManager());
        bitmapEditor.blurBitmap(background, 2);

        BitmapDrawable drawableBackground = new BitmapDrawable(getResources(), background);
        LinearLayout rootLayout = (LinearLayout) rootView.findViewById(R.id.root_layout);
        rootLayout.setBackgroundDrawable(drawableBackground);
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

                mConnectingDialog.setMessage(
                        "Connecting to " + mSelectedLiveObject.getLiveObjectName());
                mConnectingDialog.show();

                mNetworkController.connect(mSelectedLiveObject);

                mClickedView = view;
            }
        });

        mHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent  intent = new Intent(getActivity(), SavedLiveObjectsActivity.class);
                startActivity(intent);
            }
        });

        mProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initNetworkListeners() {
        mNetworkController = mMiddleware.getNetworkController();

        initDiscoveryListener();
        initConnectionListener();
        
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
                Log.v(LOG_TAG, String.format("onConnected(%s)", connectedLiveObject));
                if (connectedLiveObject.equals(mSelectedLiveObject)) {
                    mConnectingDialog.dismiss();

                    final TextView liveObjectTitleTextView =
                            (TextView)mClickedView.findViewById(R.id.grid_item_title_textview);

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
        mNetworkController.start();
        mNetworkController.startDiscovery();
    }

    @Override
    public void onStop() {
        Log.v(LOG_TAG, "onStop()");
//        mNetworkController.stop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.v(LOG_TAG, "deleting all the network configuration related to live objects");
        mMiddleware.getNetworkController().forgetNetworkConfigurations();
        super.onDestroy();
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
