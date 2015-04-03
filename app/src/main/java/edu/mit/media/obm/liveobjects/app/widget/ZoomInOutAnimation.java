package edu.mit.media.obm.liveobjects.app.widget;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import edu.mit.media.obm.shair.liveobjects.R;

/**
 * Simple Animation repeatedly zooming in and out a given image
 *
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class ZoomInOutAnimation {
    private ImageView mImageView;
    private Context mContext;

    public ZoomInOutAnimation(ImageView imageView, Context context) {
        mImageView = imageView;
        mContext = context;
    }

    public void startAnimation() {
        final Animation zoomIn = AnimationUtils.loadAnimation(mContext, R.anim.zoom_in);
        final Animation zoomOut = AnimationUtils.loadAnimation(mContext, R.anim.zoom_out);

        zoomIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mImageView.startAnimation(zoomOut);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        zoomOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mImageView.startAnimation(zoomIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mImageView.setAnimation(zoomIn);
        mImageView.setAnimation(zoomOut);

        mImageView.startAnimation(zoomIn);

    }


}
