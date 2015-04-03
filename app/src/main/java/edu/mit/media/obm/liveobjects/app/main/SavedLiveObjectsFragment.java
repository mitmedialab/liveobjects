package edu.mit.media.obm.liveobjects.app.main;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;

import edu.mit.media.obm.liveobjects.app.data.LObjContract;
import edu.mit.media.obm.liveobjects.app.detail.WrapUpActivity;
import edu.mit.media.obm.liveobjects.app.utils.Util;
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
        initListListener(mListView);
        fillData();


        return rootView;
    }


    private void initListListener(ListView l) {
        l.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Uri liveObjectUri = ContentUris.withAppendedId(LObjContract.LiveObjectEntry.CONTENT_URI, id);
                Cursor cursor = getActivity().getContentResolver().query(liveObjectUri,
                        LObjContract.LiveObjectEntry.ALL_COLUMNS, null, null, null);
                cursor.moveToFirst();
                String liveObjNameId = cursor.getString(
                        cursor.getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_ID));
                Intent intent = new Intent(getActivity(), WrapUpActivity.class);
                intent.putExtra(WrapUpActivity.EXTRA_SHOW_ADD_COMMENT, false);
                intent.putExtra(WrapUpActivity.EXTRA_LIVE_OBJ_NAME_ID, liveObjNameId);
                startActivity(intent);

            }
        });
    }


    private void fillData() {

        // Fields from the database (projection)
        // Must include the _id column for the adapter to work
        String[] from = new String[] { LObjContract.LiveObjectEntry.COLUMN_NAME_TITLE,
                LObjContract.LiveObjectEntry.COLUMN_NAME_ICON_FILEPATH};
        // Fields on the UI to which we map
        int[] to = new int[] { R.id.row_item_title_textview, R.id.row_item_icon_imageview};

        getLoaderManager().initLoader(0, null, this);

        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.saved_live_object_row, null, from,
                to, 0);


        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                if (view.getId() == R.id.row_item_icon_imageview) {
                    String imageFilePath = cursor.getString(
                            cursor.getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_ICON_FILEPATH)
                    );
                    File file = new File(imageFilePath);
                    try {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        Bitmap bitmap = Util.getBitmap(fileInputStream);
                        ImageView iconView = (ImageView) view;
                        Bitmap croppedBitmap = Util.cropImage(bitmap, iconView);
                        iconView.setImageBitmap(croppedBitmap);

                    } catch (Exception e) {
                        Log.e(LOG_TAG, "error opening icon file", e);
                        return false;
                    }
                    return true;

                }

                else if (view.getId() == R.id.row_item_title_textview) {
                    String title = cursor.getString(
                            cursor.getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_TITLE)
                    );
                    ((TextView)view).setText(title);
                    return true;

                }

                return false;
            }
        });


        mListView.setAdapter(mAdapter);


    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {LObjContract.LiveObjectEntry._ID, LObjContract.LiveObjectEntry.COLUMN_NAME_TITLE, LObjContract.LiveObjectEntry.COLUMN_NAME_ICON_FILEPATH};
        String selection = null;
        String [] selectionArgs = null;

        if (mTabId == SavedLiveObjectsActivity.FAVOURITE_TAB_ID) {
            selection = LObjContract.LiveObjectEntry.COLUMN_NAME_FAVORITE + "= ?";
            selectionArgs = new String[1];
            selectionArgs[0] = "" + LObjContract.LiveObjectEntry.FAVORITE_TRUE;

        }


        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                LObjContract.LiveObjectEntry.CONTENT_URI, projection,
                selection, selectionArgs, null);
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
