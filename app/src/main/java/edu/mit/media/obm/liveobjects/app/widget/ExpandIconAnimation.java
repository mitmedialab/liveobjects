package edu.mit.media.obm.liveobjects.app.widget;

import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.noveogroup.android.log.Log;

/**
 * Created by artimo14 on 3/14/15.
 */
public class ExpandIconAnimation {
    private AnimationSet mAnimationSet;

    private static final int TRANSLATE_DURATION = 400;
    private static final int TRANSLATE_OFFISET = 0;
    private static final int EXPAND_DURATION = 400;
    private static final int EXPAND_OFFSET = TRANSLATE_DURATION;

    public ExpandIconAnimation(WindowManager windowManager, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        Animation translateAnimation = createMoveToCenterAnimation(displayMetrics, view);
        translateAnimation.setDuration(TRANSLATE_DURATION);
        translateAnimation.setStartOffset(TRANSLATE_OFFISET);

        Animation expandAnimation = createExpandAnimation(displayMetrics, view);
        expandAnimation.setDuration(EXPAND_DURATION);
        expandAnimation.setStartOffset(EXPAND_OFFSET);

        mAnimationSet = new AnimationSet(true);
        mAnimationSet.addAnimation(translateAnimation);
        mAnimationSet.addAnimation(expandAnimation);
    }

    private Animation createMoveToCenterAnimation(DisplayMetrics displayMetrics, View view) {
        int xDest = displayMetrics.widthPixels / 2 - view.getMeasuredWidth() / 2;
        int yDest = displayMetrics.heightPixels / 2 - view.getMeasuredHeight() / 2;

        int originalPos[] = new int[2];
        view.getLocationOnScreen(originalPos);

        Animation animation = new TranslateAnimation(0, xDest - originalPos[0], 0, yDest - originalPos[1]);

        return animation;
    }

    private Animation createExpandAnimation(DisplayMetrics displayMetrics, View view) {
        float diagonalLength = (float) Math.sqrt(Math.pow(displayMetrics.widthPixels, 2) +
                Math.pow(displayMetrics.heightPixels, 2));
        float scale = diagonalLength / view.getMeasuredWidth() * 1.4f;
        Log.v("displayMetrics = (%d, %d)", displayMetrics.widthPixels, displayMetrics.heightPixels);
        Log.v("diagonalLength = %f, width = %d, scale = %f", diagonalLength, view.getMeasuredWidth(), scale);

        float xDest = displayMetrics.widthPixels / 2 - view.getMeasuredWidth() / 2 / (scale * 2);
        float yDest = displayMetrics.heightPixels / 2 - view.getMeasuredHeight() / 2 / (scale * 2);

        int originalPos[] = new int[2];
        view.getLocationOnScreen(originalPos);

        Animation animation = new ScaleAnimation(1.0f, scale, 1.0f, scale,
                Animation.ABSOLUTE, xDest - originalPos[0],
                Animation.ABSOLUTE, yDest - originalPos[1]);

        return animation;
    }

    public Animation getAnimation() {
        return mAnimationSet;
    }
}
