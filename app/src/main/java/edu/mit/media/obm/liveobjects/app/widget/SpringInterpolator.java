package edu.mit.media.obm.liveobjects.app.widget;

import android.util.Log;
import android.view.animation.Interpolator;

/**
 * Created by arata on 3/13/15.
 */
public class SpringInterpolator implements Interpolator {
    public SpringInterpolator() {
    }

    @Override
    public float getInterpolation(float t) {
        double sin = Math.sin(Math.PI * (2.0 * t * 4.0 - 0.5));
        double amplitude = Math.exp(-t * 10.0);

        return (float)(amplitude * sin + 1.0);
    }
}
