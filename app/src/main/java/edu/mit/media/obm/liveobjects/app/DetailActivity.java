package edu.mit.media.obm.liveobjects.app;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;

import java.io.IOException;
import java.net.ConnectException;

import edu.mit.media.obm.shair.liveobjects.R;


public class DetailActivity extends ActionBarActivity {
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    public static int RESULT_CONNECTION_ERROR = RESULT_FIRST_USER;
    public static int RESULT_JSON_ERROR = RESULT_FIRST_USER + 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setOnCancelListener(new DetailFragment.OnErrorListener() {
                @Override
                public void onError(Exception exception) {
                    Class exceptionClass = exception.getClass();
                    int result = RESULT_OK;

                    if (ConnectException.class.equals(exceptionClass)) {
                        result = RESULT_CONNECTION_ERROR;
                    } else if (JSONException.class.equals(exceptionClass)) {
                        result = RESULT_JSON_ERROR;
                    }

                    setResult(result);
                    finish();
                }
            });

            //TODO
//            LiveObject liveObject = getIntent().getParcelableExtra(LiveObjectsManager.EXTRA_LIVE_OBJECT);
//            Bundle bundle = new Bundle();
//            bundle.putParcelable(LiveObjectsManager.EXTRA_LIVE_OBJECT, liveObject);
//            detailFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, detailFragment)
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
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
