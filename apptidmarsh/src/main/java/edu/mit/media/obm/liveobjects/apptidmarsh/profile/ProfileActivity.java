package edu.mit.media.obm.liveobjects.apptidmarsh.profile;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.MenuItem;

import edu.mit.media.obm.liveobjects.apptidmarsh.data.ProfilePreference;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.MenuActions;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.SingleFragmentActivity;
import edu.mit.media.obm.shair.liveobjects.R;

public class ProfileActivity extends SingleFragmentActivity {

    private ProfileFragment mProfileFragment;

    @Override
    protected Fragment createFragment() {
        return ProfileFragment.newInstance();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            SharedPreferences pref = ProfilePreference.getInstance(this);
            if (!ProfilePreference.isProfileCompleted(pref, this)) {
                launchProfileEdit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_goto_home) {
            MenuActions.goToHome(this);
            return true;
        } else if (id == R.id.action_edit_profile) {
            launchProfileEdit();


        }

        return super.onOptionsItemSelected(item);
    }

    private void launchProfileEdit() {
        DialogFragment editProfileFragment = new EditProfileFragment();
        if (mProfileFragment != null) {
            editProfileFragment.setTargetFragment(mProfileFragment, 1);
        }
        editProfileFragment.show(getSupportFragmentManager(), null);
    }

}
