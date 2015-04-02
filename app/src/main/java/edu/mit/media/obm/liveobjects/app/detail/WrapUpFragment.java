package edu.mit.media.obm.liveobjects.app.detail;


import android.app.Activity;
import android.app.Application;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;

import edu.mit.media.obm.liveobjects.app.LiveObjectsApplication;
import edu.mit.media.obm.liveobjects.app.data.LObjContract;
import edu.mit.media.obm.liveobjects.app.utils.Util;
import edu.mit.media.obm.liveobjects.app.widget.BitmapEditor;
import edu.mit.media.obm.shair.liveobjects.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WrapUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WrapUpFragment extends Fragment {
    private static final String LOG_TAG = WrapUpFragment.class.getSimpleName();

    View mRootView;
    TextView mDescriptionTextView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment WrapUpFragment.
     */
    public static WrapUpFragment newInstance() {
        WrapUpFragment fragment = new WrapUpFragment();
        return fragment;
    }

    public WrapUpFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_wrap_up, container, false);
        mDescriptionTextView = (TextView) mRootView.findViewById(R.id.descriptionTextView);

        setLiveObjectInfo();

        return mRootView;
    }

    private void setLiveObjectInfo() {
        LiveObjectsApplication application = (LiveObjectsApplication)getActivity().getApplication();
        String selectedLiveObjectName = application.getSelectedLiveObjectName();

        Cursor cursor = getLocalLiveObject(selectedLiveObjectName);
        cursor.moveToFirst();

        setLocalDescription(cursor);
        setLocalBackgroundImage(cursor);
    }

    private Cursor getLocalLiveObject(String liveObjectNameID) {
        String selection = LObjContract.LiveObjectEntry.COLUMN_NAME_ID + "= ?";
        String[] selectionArgs ={liveObjectNameID};
        Cursor cursor = getActivity().getContentResolver().query(
                LObjContract.LiveObjectEntry.CONTENT_URI,
                LObjContract.LiveObjectEntry.ALL_COLUMNS,
                selection, selectionArgs, null);
        return  cursor;
    }

    private void setLocalTitle(Cursor cursor){
        Log.d(LOG_TAG, "cursor " + cursor);
        String title = cursor.getString(cursor.
                getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_TITLE));
      //  mObjectTitleTextView.setText(title);
    }

    private void setLocalDescription(Cursor cursor){
        Log.d(LOG_TAG, "cursor " + cursor);
        String description = cursor.getString(cursor.
                getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_DESCRIPTION));
        mDescriptionTextView.setText(description);
    }

    private void setLocalBackgroundImage(Cursor cursor) {
        String imagePath = cursor.getString(cursor.
                getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_ICON_FILEPATH));
        try {
            File file = new File(imagePath);
            FileInputStream inputStream = new FileInputStream(file);
            Bitmap bitmap = Util.getBitmap(inputStream);
            setBackgroundImage(bitmap);
            inputStream.close();

        } catch (Exception e) {
            Log.e(LOG_TAG, "error opening file: " + imagePath, e);
        }
    }

    private void setBackgroundImage(Bitmap bitmap){
        final Activity activity = getActivity();

        BitmapEditor bitmapEditor = new BitmapEditor(activity);
        Bitmap croppedBitmap = bitmapEditor.cropToDisplayAspectRatio(bitmap, activity.getWindowManager());
        bitmapEditor.blurBitmap(croppedBitmap, 6);

        if (croppedBitmap != null ) {
            final BitmapDrawable background = new BitmapDrawable(croppedBitmap);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    View rootLayout = mRootView.findViewById(R.id.root_layout);
                    rootLayout.setBackgroundDrawable(background);
                }
            });
        }
    }
}
