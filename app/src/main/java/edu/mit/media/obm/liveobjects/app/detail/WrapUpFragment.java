package edu.mit.media.obm.liveobjects.app.detail;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.mit.media.obm.liveobjects.app.data.LObjContentProvider;
import edu.mit.media.obm.liveobjects.app.data.LObjContract;
import edu.mit.media.obm.liveobjects.app.media.MediaViewActivity;
import edu.mit.media.obm.liveobjects.app.widget.BitmapEditor;
import edu.mit.media.obm.shair.liveobjects.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WrapUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WrapUpFragment extends Fragment {
    private static final String ARG_LIVE_OBJ_NAME_ID = "live_obj_name_id";
    private static final String ARG_SHOW_ADD_COMMENT = "show_add_comment";

    private String mLiveObjNameId;
    private boolean mShowAddComment;

    private TextView mTitleTextView;
    private TextView mDescriptionTextView;
    private LinearLayout mFavouriteButtonLayout;
    private LinearLayout mReplayButtonLayout;
    private Button mAddCommentButton;

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
        mDescriptionTextView = (TextView) rootView.findViewById(R.id.wrapup_description_textview);
        mFavouriteButtonLayout = (LinearLayout) rootView.findViewById(R.id.favorite_button);
        mReplayButtonLayout = (LinearLayout) rootView.findViewById(R.id.replay_button);
        mAddCommentButton = (Button) rootView.findViewById(R.id.addCommentButton);
    }

    private void setUIContent(View rootView) {
        Cursor cursor = LObjContentProvider.getLocalLiveObject(mLiveObjNameId, getActivity());
        cursor.moveToFirst();
        setImage(rootView, cursor);
        setText(mTitleTextView, cursor, LObjContract.LiveObjectEntry.COLUMN_NAME_TITLE);
        setText(mDescriptionTextView, cursor, LObjContract.LiveObjectEntry.COLUMN_NAME_DESCRIPTION);

    }

    private void setUIListener(View rootView) {
        mFavouriteButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
    }

    private void setImage(View view, Cursor cursor){
        Activity activity = getActivity();

        String imageFilePath = cursor.getString(cursor.getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_ICON_FILEPATH));
        Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);

        BitmapEditor bitmapEditor = new BitmapEditor(activity);
        Bitmap croppedBitmap = bitmapEditor.cropToDisplayAspectRatio(bitmap, activity.getWindowManager());
        bitmapEditor.blurBitmap(croppedBitmap, 6);

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

    private void initUIListener() {
        mReplayButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

    }





}
