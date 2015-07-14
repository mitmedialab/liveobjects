package edu.mit.media.obm.liveobjects.apptidmarsh.detail;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import edu.mit.media.obm.liveobjects.apptidmarsh.widget.MenuActions;
import edu.mit.media.obm.shair.liveobjects.R;

public class WrapUpActivity extends ActionBarActivity {

    public static String EXTRA_LIVE_OBJ_NAME_ID = "live_obj_name_id";
    public static String EXTRA_SHOW_ADD_COMMENT = "show_add_comment";

    private String mLiveObjectId;
    private boolean mShowAddComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrap_up);
        if (savedInstanceState == null) {
            mLiveObjectId = getIntent().getStringExtra(EXTRA_LIVE_OBJ_NAME_ID);
            mShowAddComment = getIntent().getBooleanExtra(EXTRA_SHOW_ADD_COMMENT, true);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_wrap_up_container, WrapUpFragment.newInstance(mLiveObjectId, mShowAddComment))
                    .commit();

            getSupportActionBar().setTitle(mLiveObjectId);
        }
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
            MenuActions.goToHome(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
