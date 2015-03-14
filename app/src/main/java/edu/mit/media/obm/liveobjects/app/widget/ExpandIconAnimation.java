package edu.mit.media.obm.liveobjects.app.widget;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * Created by artimo14 on 3/14/15.
 */
public class ExpandIconAnimation {
    private AnimationSet mAnimationSet;

    public ExpandIconAnimation(WindowManager windowManager, View view) {
        Animation translateAnimation = createMoveToCenterAnimation(windowManager, view);
        translateAnimation.setDuration(500);

        Animation expandAnimation = createExpandAnimation(windowManager, view);
        expandAnimation.setDuration(500);
        expandAnimation.setStartOffset(500);

        mAnimationSet = new AnimationSet(true);
        mAnimationSet.addAnimation(translateAnimation);
        mAnimationSet.addAnimation(expandAnimation);
    }

    private Animation createMoveToCenterAnimation(WindowManager windowManager, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        int xDest = displayMetrics.widthPixels / 2 - view.getMeasuredWidth() / 2;
        int yDest = displayMetrics.heightPixels / 2 - view.getMeasuredHeight() / 2;

        int originalPos[] = new int[2];
        view.getLocationOnScreen(originalPos);

        Animation animation = new TranslateAnimation( 0, xDest - originalPos[0] , 0, yDest - originalPos[1] );

        return animation;
    }

    private Animation createExpandAnimation(WindowManager windowManager, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        float scale = 10.0f;

        float xDest = displayMetrics.widthPixels / 2 - view.getMeasuredWidth() / 2 / (scale * 2);
        float yDest = displayMetrics.heightPixels / 2 - view.getMeasuredHeight() / 2 / (scale * 2);

        int originalPos[] = new int[2];
        view.getLocationOnScreen(originalPos);

        Animation animation = new ScaleAnimation(1.0f, scale, 1.0f, scale,
                Animation.ABSOLUTE, xDest - originalPos[0],
                Animation.ABSOLUTE, yDest - originalPos[1]);

        return animation;
    }

    public Animation getAnimation()
    {
        return mAnimationSet;
    }
}
