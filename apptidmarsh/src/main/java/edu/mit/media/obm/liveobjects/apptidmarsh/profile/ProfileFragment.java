package edu.mit.media.obm.liveobjects.apptidmarsh.profile;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.mit.media.obm.liveobjects.apptidmarsh.data.ProfilePreference;
import edu.mit.media.obm.shair.liveobjects.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    private static final String LOG_TAG = ProfileFragment.class.getSimpleName();

    private TextView mNameTextView;
    private TextView mCompanyTextView;
    private TextView mEmailTextView;


    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment
     *
     * @return A new instance of fragment ProfileFragment.
     */
    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        initUI(rootView);
        setUIProfileContent();

        return rootView;
    }

    private void initUI(View view) {
        mNameTextView = (TextView) view.findViewById(R.id.nameTextView);
        mCompanyTextView = (TextView) view.findViewById(R.id.companyTextView);
        mEmailTextView = (TextView) view.findViewById(R.id.emailTextView);
    }

    private void setUIProfileContent() {

        //update the data from the preferences
        Context context = getActivity();
        SharedPreferences pref = ProfilePreference.getInstance(context);
        mNameTextView.setText(
                ProfilePreference.getString(pref, context, R.string.profile_name_key));
        mCompanyTextView.setText(
                ProfilePreference.getString(pref, context, R.string.profile_company_key));
        mEmailTextView.setText(
                ProfilePreference.getString(pref, context, R.string.profile_email_key));
    }


    @Override
    public void onResume() {
        super.onResume();
        setUIProfileContent();
        Log.d(LOG_TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "ON ACTIVITY RESULT");
        setUIProfileContent();
    }
}
