package edu.mit.media.obm.liveobjects.apptidmarsh.widget;

import android.app.Activity;
import android.content.Intent;

import edu.mit.media.obm.liveobjects.app.main.MainActivity;

/**
 * Created by artimo14 on 3/22/15.
 */
public class MenuActions {
    public static void goToHome(Activity callerActivity) {
        Intent intent = new Intent(callerActivity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        callerActivity.startActivity(intent);
        callerActivity.finish();
    }
}
