package edu.mit.media.obm.liveobjects.app.detail;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;

import java.net.ConnectException;

import edu.mit.media.obm.liveobjects.app.main.MainActivity;
import edu.mit.media.obm.shair.liveobjects.R;


public class DetailActivity extends ActionBarActivity {
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    public static String EXTRA_LIVE_OBJ_NAME_ID = "live_obj_name_id";

    public static int RESULT_CONNECTION_ERROR = RESULT_FIRST_USER;
    public static int RESULT_JSON_ERROR = RESULT_FIRST_USER + 1;

    private DetailFragment mDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            String liveObjNameId = getIntent().getStringExtra(EXTRA_LIVE_OBJ_NAME_ID);

            mDetailFragment = DetailFragment.newInstance(liveObjNameId);
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


            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mDetailFragment)
                    .commit();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
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
            mDetailFragment.cancelAsyncTasks(); 

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
