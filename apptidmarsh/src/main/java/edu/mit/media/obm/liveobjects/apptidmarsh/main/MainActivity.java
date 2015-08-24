package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import javax.inject.Inject;

import edu.mit.media.obm.liveobjects.apptidmarsh.history.SavedLiveObjectsActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.apptidmarsh.notifications.PeriodicAlarmManager;
import edu.mit.media.obm.liveobjects.apptidmarsh.profile.ProfileActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.SingleFragmentActivity;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkController;
import edu.mit.media.obm.shair.liveobjects.R;


public class MainActivity extends SingleFragmentActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Inject NetworkController mNetWorkController;

    @Inject PeriodicAlarmManager mPeriodicAlarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DependencyInjector.inject(this, getApplicationContext());
        mPeriodicAlarmManager.startPeriodicService();
    }

    @Override
    protected Fragment createFragment() {
        return new MainFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_goto_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);

            return true;
        } else if (id == R.id.action_goto_history) {
            Intent intent = new Intent(this, SavedLiveObjectsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        mNetWorkController.stop();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
        mPeriodicAlarmManager.stopPeriodicService();
    }
}
