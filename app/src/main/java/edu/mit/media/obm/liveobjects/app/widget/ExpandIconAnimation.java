package edu.mit.media.obm.liveobjects.app.widget;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

/**
 * Created by artimo14 on 3/14/15.
 */
public class ExpandIconAnimation {
    private Animation mAnimation;

    public ExpandIconAnimation(WindowManager windowManager, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        int xDest = displayMetrics.widthPixels / 2 - view.getMeasuredWidth() / 2;
        int yDest = displayMetrics.heightPixels / 2 - view.getMeasuredHeight() / 2;

        int originalPos[] = new int[2];
        view.getLocationOnScreen(originalPos);

        Log.v(getClass().getSimpleName(), String.format("displayMetrics = %d, %d", displayMetrics.widthPixels, displayMetrics.heightPixels));
        Log.v(getClass().getSimpleName(), String.format("xDest = %d, yDest = %d, originalPos = %d, %d", xDest, yDest, originalPos[0], originalPos[1]));
        mAnimation = new TranslateAnimation( 0, xDest - originalPos[0] , 0, yDest - originalPos[1] );
        mAnimation.setDuration(500);
    }

    public Animation getAnimation()
    {
        return mAnimation;
    }
}
