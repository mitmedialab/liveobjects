package edu.mit.media.obm.liveobjects.app.profile;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.noveogroup.android.log.Log;

import edu.mit.media.obm.liveobjects.app.data.ProfilePreference;
import edu.mit.media.obm.shair.liveobjects.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    private TextView mNameTextView;
    private TextView mCompanyTextView;
    private TextView mEmailTextView;
    private DonutProgress mDonutProgress;
    private TextView mPrizeTextView;


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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        initUI(rootView);
        setDonutProgress();
        setUIProfileContent();

        return rootView;
    }

    private void initUI(View view){
        mNameTextView = (TextView)view.findViewById(R.id.nameTextView);
        mCompanyTextView = (TextView)view.findViewById(R.id.companyTextView);
        mEmailTextView = (TextView)view.findViewById(R.id.emailTextView);
        mDonutProgress = (DonutProgress) view.findViewById(R.id.donut_progress);
        mPrizeTextView = (TextView)view.findViewById(R.id.prize_text_view);
    }


    private void setDonutProgress() {
        int totalNumberOfObjects = getResources().getInteger(R.integer.total_number_of_objects);
        mDonutProgress.setSuffixText("/" + totalNumberOfObjects);
        mDonutProgress.setMax(totalNumberOfObjects);

        int numberOfVisitedObjects = Math.min(getNumberOfVisitedObjects(), totalNumberOfObjects);
        mDonutProgress.setProgress(numberOfVisitedObjects);

        if (numberOfVisitedObjects == totalNumberOfObjects) {
            mPrizeTextView.setVisibility(View.VISIBLE);
        }
    }

    private int getNumberOfVisitedObjects() {
        return 0;
        //TODO to implement


    }
    private void setUIProfileContent() {

        //update the data from the preferences
        Context context =getActivity();
        SharedPreferences pref = ProfilePreference.getInstance(context);
        mNameTextView.setText(
                ProfilePreference.getString(pref,context,R.string.profile_name_key));
        mCompanyTextView.setText(
                ProfilePreference.getString(pref,context,R.string.profile_company_key));
        mEmailTextView.setText(
                ProfilePreference.getString(pref,context,R.string.profile_email_key));
    }


    @Override
    public void onResume() {
        super.onResume();
        setUIProfileContent();
        Log.d("onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("onPause");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("ON ACTIVITY RESULT");
        setUIProfileContent();
    }
}
