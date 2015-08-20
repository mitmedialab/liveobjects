package edu.mit.media.obm.liveobjects.apptidmarsh.detail;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.json.JSONException;

import java.net.ConnectException;

import butterknife.BindString;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.SingleFragmentActivity;
import edu.mit.media.obm.shair.liveobjects.R;

/**
 * Created by artimo14 on 8/19/15.
 */
public class ContentBrowserActivity extends SingleFragmentActivity {
    @BindString(R.string.arg_live_object_name_id) String EXTRA_LIVE_OBJ_NAME_ID;
    @BindString(R.string.extra_arguments) String EXTRA_ARGUMENTS;

    public static int RESULT_CONNECTION_ERROR = RESULT_FIRST_USER;
    public static int RESULT_JSON_ERROR = RESULT_FIRST_USER + 1;

    @Override
    protected Fragment createFragment() {
        final ContentBrowserFragment fragment = new ContentBrowserFragment();

        fragment.setOnCancelListener(new DetailFragment.OnErrorListener() {
            @Override
            public void onError(Exception exception) {
                Class exceptionClass = exception.getClass();
                int result = RESULT_OK;

                if (ConnectException.class.equals(exceptionClass)) {
                    result = RESULT_CONNECTION_ERROR;
                } else if (JSONException.class.equals(exceptionClass)) {
                    result = RESULT_JSON_ERROR;
                }

                fragment.cancelAsyncTasks();
                setResult(result);
                finish();
            }
        });

        Bundle arguments = getIntent().getBundleExtra(EXTRA_ARGUMENTS);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String liveObjNameId = getIntent().getStringExtra(EXTRA_LIVE_OBJ_NAME_ID);
        getSupportActionBar().setTitle(liveObjNameId);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_content_browser;
    }
}
