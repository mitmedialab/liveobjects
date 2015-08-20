package edu.mit.media.obm.liveobjects.apptidmarsh.history;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import dagger.ObjectGraph;
import edu.mit.media.obm.liveobjects.apptidmarsh.data.MLProjectPropertyProvider;
import edu.mit.media.obm.liveobjects.apptidmarsh.detail.DetailActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.middleware.common.MapLocation;
import edu.mit.media.obm.liveobjects.middleware.common.MiddlewareInterface;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;
import edu.mit.media.obm.shair.liveobjects.R;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class SavedLiveObjectsFragment extends Fragment {
    private static String LOG_TAG = SavedLiveObjectsFragment.class.getSimpleName();

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_TAB_ID = "tab_id";
    private int mTabId;

    @Bind(R.id.saved_liveobjs_listview) ListView mListView;
    @BindString(R.string.arg_live_object_name_id) String EXTRA_LIVE_OBJ_NAME_ID;
    @BindString(R.string.arg_connected_to_live_object) String EXTRA_CONNECTED_TO_LIVE_OBJ;
    @BindString(R.string.extra_arguments) String EXTRA_ARGUMENTS;

    @Inject DbController mDbController;

    @OnItemClick(R.id.saved_liveobjs_listview)
    void onSavedLiveObjectsListViewItemClick(int position) {
        MLProjectPropertyProvider provider =
                new MLProjectPropertyProvider(mLiveObjectsPropertiesList.get(position));
        String liveObjNameId = provider.getId();

        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_LIVE_OBJ_NAME_ID, liveObjNameId);
        arguments.putBoolean(EXTRA_CONNECTED_TO_LIVE_OBJ, false);

        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra(EXTRA_ARGUMENTS, arguments);
        startActivity(intent);
    }

    private SavedLiveObjectsAdapter mAdapter;

    private List<Map<String, Object>> mLiveObjectsPropertiesList;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SavedLiveObjectsFragment newInstance(int tabId) {
        SavedLiveObjectsFragment fragment = new SavedLiveObjectsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_ID, tabId);
        fragment.setArguments(args);
        return fragment;
    }

    public SavedLiveObjectsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mTabId = getArguments().getInt(ARG_TAB_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_saved_live_objects, container, false);
        ButterKnife.bind(this, rootView);
        DependencyInjector.inject(this, getActivity());
        
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        fillData();
    }

    private void fillData() {
        List<Map<String, Object>> allLiveObjects = mDbController.getAllLiveObjectsProperties();
        List<Map<String, Object>> nonEmptyLiveObjects = new ArrayList<>();
        for (Map<String, Object> liveObjectProperties : allLiveObjects) {
            MLProjectPropertyProvider provider = new MLProjectPropertyProvider(liveObjectProperties);
            if (!mDbController.isLiveObjectEmpty(provider.getId())) {
                nonEmptyLiveObjects.add(liveObjectProperties);
            }
        }

        mLiveObjectsPropertiesList = new ArrayList<>();
        if (mTabId == SavedLiveObjectsActivity.FAVOURITE_TAB_ID) {

            for (Map<String, Object> liveObjectProperties : nonEmptyLiveObjects) {
                MLProjectPropertyProvider provider = new MLProjectPropertyProvider(liveObjectProperties);
                if (provider.isFavorite()) {
                    mLiveObjectsPropertiesList.add(liveObjectProperties);
                }
            }
        } else {
            mLiveObjectsPropertiesList = nonEmptyLiveObjects;
        }

        mAdapter = new SavedLiveObjectsAdapter(getActivity(), mLiveObjectsPropertiesList);

        mListView.setAdapter(mAdapter);
    }
}
