package edu.mit.media.obm.liveobjects.app.history;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import edu.mit.media.obm.liveobjects.app.data.LObjContract;
import edu.mit.media.obm.shair.liveobjects.R;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class SavedLiveObjectsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static String LOG_TAG = SavedLiveObjectsFragment.class.getSimpleName();

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_TAB_ID = "tab_id";
    private int mTabId;
    private ListView mListView;
    private SimpleCursorAdapter mAdapter;


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
        mListView = (ListView) rootView.findViewById(R.id.saved_liveobjs_listview);
        fillData();

        return rootView;
    }


    private void fillData() {

        // Fields from the database (projection)
        // Must include the _id column for the adapter to work
        String[] from = new String[] { LObjContract.LiveObjectEntry.COLUMN_NAME_TITLE };
        // Fields on the UI to which we map
        int[] to = new int[] { R.id.row_item_title_textview };

        getLoaderManager().initLoader(0, null, this);
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.saved_live_object_row, null, from,
                to, 0);



        mListView.setAdapter(mAdapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {LObjContract.LiveObjectEntry._ID, LObjContract.LiveObjectEntry.COLUMN_NAME_TITLE};

        CursorLoader cursorLoader = new CursorLoader(getActivity(), LObjContract.CONTENT_URI, projection, null,null,null);
        return cursorLoader;
    }



    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "receiving data " + data);
        mAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

}
