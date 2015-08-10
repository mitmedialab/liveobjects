package edu.mit.media.obm.liveobjects.apptidmarsh.detail;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;

import java.net.ConnectException;

import butterknife.BindString;
import butterknife.ButterKnife;
import edu.mit.media.obm.liveobjects.apptidmarsh.main.GroundOverlayMapFragment;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.MenuActions;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.SingleFragmentActivity;
import edu.mit.media.obm.shair.liveobjects.R;


public class DetailActivity extends SingleFragmentActivity {
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    @BindString(R.string.arg_live_object_name_id) String EXTRA_LIVE_OBJ_NAME_ID;
    @BindString(R.string.arg_connected_to_live_object) String EXTRA_CONNECTED_TO_LIVE_OBJ;

    public static int RESULT_CONNECTION_ERROR = RESULT_FIRST_USER;
    public static int RESULT_JSON_ERROR = RESULT_FIRST_USER + 1;

    private DetailFragment mDetailFragment;

    @Override
    protected Fragment createFragment() {
        String liveObjNameId = getIntent().getStringExtra(EXTRA_LIVE_OBJ_NAME_ID);
        boolean showAddComment = getIntent().getBooleanExtra(EXTRA_CONNECTED_TO_LIVE_OBJ, false);

        mDetailFragment = DetailFragment.newInstance(this, liveObjNameId, showAddComment);
        mDetailFragment.setOnCancelListener(new DetailFragment.OnErrorListener() {
            @Override
            public void onError(Exception exception) {
                Class exceptionClass = exception.getClass();
                int result = RESULT_OK;

                if (ConnectException.class.equals(exceptionClass)) {
                    result = RESULT_CONNECTION_ERROR;
                } else if (JSONException.class.equals(exceptionClass)) {
                    result = RESULT_JSON_ERROR;
                }

                mDetailFragment.cancelAsyncTasks();
                setResult(result);
                finish();
            }
        });

        getSupportActionBar().setTitle(liveObjNameId);

        return mDetailFragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_detail;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
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
            if (mDetailFragment != null) {
                mDetailFragment.cancelAsyncTasks();
            }

            MenuActions.goToHome(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MenuActions.goToHome(this);
    }
}
