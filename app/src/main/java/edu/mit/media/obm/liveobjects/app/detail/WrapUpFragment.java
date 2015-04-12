package edu.mit.media.obm.liveobjects.app.detail;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;

import edu.mit.media.obm.liveobjects.app.LiveObjectsApplication;
import edu.mit.media.obm.liveobjects.app.data.LObjContentProvider;
import edu.mit.media.obm.liveobjects.app.data.LObjContract;
import edu.mit.media.obm.liveobjects.app.data.ProfilePreference;
import edu.mit.media.obm.liveobjects.app.media.MediaViewActivity;
import edu.mit.media.obm.liveobjects.app.widget.BitmapEditor;
import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.shair.liveobjects.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WrapUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WrapUpFragment extends Fragment {
    private static final String LOG_TAG = WrapUpFragment.class.getSimpleName();

    private static final String ARG_LIVE_OBJ_NAME_ID = "live_obj_name_id";
    private static final String ARG_SHOW_ADD_COMMENT = "show_add_comment";

    private String mLiveObjNameId;
    private boolean mShowAddComment;

    private TextView mTitleTextView;
    private TextView mGroupTextView;
    private TextView mDescriptionTextView;
    private LinearLayout mFavouriteButtonLayout;
    private LinearLayout mReplayButtonLayout;
    private LinearLayout mAddCommentLayout;

    AlertDialog mAddCommentAlert;

    private boolean mIsFavorite;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment WrapUpFragment.
     */
    public static WrapUpFragment newInstance(String liveObjNameId, boolean showAddComment) {
        WrapUpFragment fragment = new WrapUpFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LIVE_OBJ_NAME_ID, liveObjNameId);
        args.putBoolean(ARG_SHOW_ADD_COMMENT, showAddComment);
        fragment.setArguments(args);
        return fragment;
    }

    public WrapUpFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLiveObjNameId = getArguments().getString(ARG_LIVE_OBJ_NAME_ID);
            mShowAddComment = getArguments().getBoolean(ARG_SHOW_ADD_COMMENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wrap_up, container, false);

        initUI(rootView);
        setUIContent(rootView);
        setUIListener(rootView);

        return rootView;
    }

    private void initUI(View rootView) {
        mTitleTextView = (TextView) rootView.findViewById(R.id.wrapup_title_textview);
        mGroupTextView =(TextView) rootView.findViewById(R.id.wrapup_group_textview);
        mDescriptionTextView = (TextView) rootView.findViewById(R.id.wrapup_description_textview);
        mFavouriteButtonLayout = (LinearLayout) rootView.findViewById(R.id.favorite_button);
        mReplayButtonLayout = (LinearLayout) rootView.findViewById(R.id.replay_button);
        mAddCommentLayout = (LinearLayout) rootView.findViewById(R.id.addCommentButton);
        mAddCommentAlert = initAddCommentAlert();
    }

    private AlertDialog initAddCommentAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        TextView titleTextView = new TextView(new ContextThemeWrapper(getActivity(), R.style.LiveObjectsTextViewStyle));
        titleTextView.setText(" Add Comment");
        alert.setCustomTitle(titleTextView);

        // Set an EditText view to get the user input
        final EditText input = new EditText(new ContextThemeWrapper(getActivity(), R.style.LiveObjectsEditTextStyle));
        input.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        input.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        input.setHint("Type your comment here");
        alert.setView(input);

        alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LiveObjectsApplication application = (LiveObjectsApplication) getActivity().getApplication();
                ContentController contentController = application.getMiddleware().getContentController();
                String commentText = makeComment(input.getText().toString());
                Log.d(LOG_TAG, "ADDING COMMENT: " + input.getText().toString());
                contentController.putStringContent(generateCommentFileName(), "COMMENTS", commentText);

                input.setText("");
                Toast.makeText(getActivity(), "Uploaded a comment", Toast.LENGTH_SHORT).show();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        AlertDialog dialog = alert.create();

        return dialog;
    }

    private String makeComment(String text) {
        SharedPreferences pref = ProfilePreference.getInstance(getActivity());
        String name = "Name: " + ProfilePreference.getString(pref, getActivity(), R.string.profile_name_key) +"\n";
        String company = "Company: " + ProfilePreference.getString(pref, getActivity(), R.string.profile_company_key) +"\n";
        String email = "Email: " + ProfilePreference.getString(pref, getActivity(), R.string.profile_email_key) +"\n";
        String commentHeader = "Comment: \n";
        String message = name + company + email + commentHeader + text;
        return message;
    }
    private String generateCommentFileName() {
        Calendar rightNow = Calendar.getInstance();
        String commentName = String.format("%1$td%1$tk%1$tM%1$tS.TXT", rightNow);


        return commentName;
    }


    private void setUIContent(View rootView) {
        Cursor cursor = LObjContentProvider.getLocalLiveObject(mLiveObjNameId, getActivity());
        cursor.moveToFirst();
        setImage(rootView, cursor);
        setText(mTitleTextView, cursor, LObjContract.LiveObjectEntry.COLUMN_NAME_TITLE);
        setText(mGroupTextView, cursor, LObjContract.LiveObjectEntry.COLUMN_NAME_GROUP);
        setText(mDescriptionTextView, cursor, LObjContract.LiveObjectEntry.COLUMN_NAME_DESCRIPTION);
        mAddCommentLayout.setEnabled(mShowAddComment);
        mIsFavorite = setFavoriteButtonState(mFavouriteButtonLayout, cursor);

        mReplayButtonLayout.setEnabled(isContentStoredLocally(cursor));
    }

    private boolean isContentStoredLocally(Cursor cursor) {
        String filePath = cursor.getString(
                cursor.getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_MEDIA_FILEPATH));
        String sizeFilePath = filePath + ".size";
        File sizeFile = new File(sizeFilePath);

        Log.v(LOG_TAG, "111");
        if (!sizeFile.exists()) {
            Log.v(LOG_TAG, "222");
            return false;
        }

        Log.v(LOG_TAG, "333");
        int recordedFileSize;

        try {
            Log.v(LOG_TAG, "444");
            BufferedReader reader = new BufferedReader(new FileReader(sizeFile));
            recordedFileSize = Integer.valueOf(reader.readLine());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        File contentFile = new File(filePath);

        return (recordedFileSize == contentFile.length());
    }

    private boolean setFavoriteButtonState(LinearLayout favouriteButtonLayout, Cursor cursor) {
        boolean isFavorite = cursor.getInt(
                cursor.getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_FAVORITE))
                == LObjContract.LiveObjectEntry.FAVORITE_TRUE;

        updateFavoriteUI(favouriteButtonLayout, isFavorite);

        return isFavorite;

    }

    private void updateFavoriteUI(LinearLayout favouriteButtonLayout, boolean isFavorite) {
        int backgroundColorId = (isFavorite ? R.color.theme_transparent_orange : R.color.theme_pure_transparent_background);
        int backgroundColor = getResources().getColor(backgroundColorId);

        favouriteButtonLayout.setBackgroundColor(backgroundColor);
    }

    private void setUIListener(View rootView) {
        mFavouriteButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // change the favorite state state
                mIsFavorite = !mIsFavorite;
                updateFavorite(mLiveObjNameId, mIsFavorite);
                updateFavoriteUI(mFavouriteButtonLayout, mIsFavorite);

            }
        });

        mReplayButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewIntent = new Intent(getActivity(), MediaViewActivity.class);
                viewIntent.putExtra(MediaViewActivity.EXTRA_LIVE_OBJ_NAME_ID, mLiveObjNameId);
                getActivity().startActivity(viewIntent);
            }
        });

        mAddCommentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddCommentAlert.show();

                // TODO: changing button format should be done in initAddCommentAlert()
                Button positiveButton = mAddCommentAlert.getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = mAddCommentAlert.getButton(DialogInterface.BUTTON_NEGATIVE);

                positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                positiveButton.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
                negativeButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                negativeButton.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

            }
        });


    }

    private void updateFavorite(String liveObjNameId, boolean isFavorite) {

        String selection = LObjContract.LiveObjectEntry.COLUMN_NAME_ID + "= ?";
        String[] selectionArgs ={liveObjNameId};

        int isFavoriteInInt = isFavorite ?
                LObjContract.LiveObjectEntry.FAVORITE_TRUE :
                LObjContract.LiveObjectEntry.FAVORITE_FALSE;
        ContentValues values = new ContentValues();
        values.put(LObjContract.LiveObjectEntry.COLUMN_NAME_FAVORITE, isFavoriteInInt);

        getActivity().getContentResolver().update(LObjContract.LiveObjectEntry.CONTENT_URI,
                values,
                selection,
                selectionArgs);
    }

    private void setImage(View view, Cursor cursor){
        Activity activity = getActivity();

        String imageFilePath = cursor.getString(cursor.getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_ICON_FILEPATH));
        Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);

        BitmapEditor bitmapEditor = new BitmapEditor(activity);
        Bitmap croppedBitmap = bitmapEditor.cropToDisplayAspectRatio(bitmap, activity.getWindowManager());
        bitmapEditor.blurBitmap(croppedBitmap, 2);

        if (croppedBitmap != null ) {
            BitmapDrawable background = new BitmapDrawable(croppedBitmap);
            view.setBackgroundDrawable(background);
        }
    }

    private void setText(TextView textView, Cursor cursor, String columnName) {
        String value = cursor.getString(
                cursor.getColumnIndex(columnName));
        textView.setText(value);

    }







}
