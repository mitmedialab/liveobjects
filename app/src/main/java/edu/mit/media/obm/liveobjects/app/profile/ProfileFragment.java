package edu.mit.media.obm.liveobjects.app.profile;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.mit.media.obm.liveobjects.app.data.ProfilePreference;
import edu.mit.media.obm.shair.liveobjects.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    private TextView mNameTextView;
    private TextView mLastNameTextView;
    private TextView mCompanyTextView;
    private TextView mEmailTextView;

    private TextView mNumberVisitedObjects;

    /**
     * Use this factory method to create a new instance of
     * this fragment
     * @return A new instance of fragment ProfileFragment.
     */
    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        initUI(rootView);
        setUIContent();

        return rootView;
    }

    private void initUI(View view){
        mNameTextView = (TextView)view.findViewById(R.id.nameTextView);
        mLastNameTextView = (TextView)view.findViewById(R.id.lastNameTextView);
        mCompanyTextView = (TextView)view.findViewById(R.id.companyTextView);
        mEmailTextView = (TextView)view.findViewById(R.id.emailTextView);

        mNumberVisitedObjects = (TextView) view.findViewById(R.id.visitedLOsTextView);
    }



    private void setUIContent() {
        //TODO update number of visited objects through a query on the content provider
        //mNumberVisitedObjects.setText();

        //update the data from the preferences
        Context context =getActivity();
        SharedPreferences pref = ProfilePreference.getInstance(context);
        mNameTextView.setText(
                ProfilePreference.getString(pref,context,R.string.profile_name_key));
        mLastNameTextView.setText(
                ProfilePreference.getString(pref,context,R.string.profile_last_name_key));
        mCompanyTextView.setText(
                ProfilePreference.getString(pref,context,R.string.profile_company_key));
        mEmailTextView.setText(
                ProfilePreference.getString(pref,context,R.string.profile_email_key));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setUIContent();
    }
}
