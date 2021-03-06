package edu.mit.media.obm.liveobjects.app.profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import edu.mit.media.obm.liveobjects.app.data.ProfilePreference;
import edu.mit.media.obm.shair.liveobjects.R;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class EditProfileFragment extends DialogFragment {

    private EditText mNameEditText;
    private EditText mCompanyEditText;
    private EditText mEmailEditText;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //builder.setTitle("Edit Profile Info");
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.dialog_edit_profile, null);
        initUI(rootView);

        setUIContent();

        builder.setView(rootView);





        // Add action buttons
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String name = mNameEditText.getText().toString();
                String company = mCompanyEditText.getText().toString();
                String email = mEmailEditText.getText().toString();
                SharedPreferences pref= ProfilePreference.getInstance(getActivity());
                ProfilePreference.updateProfileInfo(pref,
                        getActivity(),
                        name,
                        company,
                        email);
                Fragment targetFragment = getTargetFragment();
                if (targetFragment != null) {
                    //notify back the fragment
                    getTargetFragment().onActivityResult(getTargetRequestCode(),0, null);
                }




            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditProfileFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    private void initUI(View view) {
        mNameEditText = (EditText) view.findViewById(R.id.profile_name_edit_text);
        mCompanyEditText = (EditText) view.findViewById(R.id.profile_company_edit_text);
        mEmailEditText = (EditText) view.findViewById(R.id.profile_email_edit_text);

    }

    private void setUIContent() {
        SharedPreferences profilePref = ProfilePreference.getInstance(getActivity());

        mNameEditText.setText(ProfilePreference.getString(profilePref,
                getActivity(), R.string.profile_name_key));
        mCompanyEditText.setText(ProfilePreference.getString(profilePref,
                getActivity(), R.string.profile_company_key));
        mEmailEditText.setText(ProfilePreference.getString(profilePref,
                getActivity(), R.string.profile_email_key));
    }

}
