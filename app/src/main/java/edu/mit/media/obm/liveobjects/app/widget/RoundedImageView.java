package edu.mit.media.obm.liveobjects.app.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by arata on 3/12/15.
 */
public class RoundedImageView extends ImageView {
    int mFillColor = 0;
    boolean mFillColorSet = false;

    public RoundedImageView(Context context) {
        super(context);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setFillColor(int fillColor) {
        mFillColor = fillColor;
        mFillColorSet = true;
    }

    public void clearFillColor() {
        mFillColorSet = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();

        if (drawable == null || getWidth() == 0 || getHeight() == 0) {
            return;
        }

        Bitmap originalBitmap = ((BitmapDrawable) drawable).getBitmap();
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

        if (mFillColorSet) {
            bitmap.eraseColor(mFillColor);
        }

        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

        Bitmap roundBitmap = getCroppedBitmap(bitmap, getWidth());
        canvas.drawBitmap(roundBitmap, 0, 0, null);
    }

    private static Bitmap getCroppedBitmap(Bitmap bitmap, int radius) {
        Bitmap scaledBitmap = (bitmap.getWidth() != radius || bitmap.getHeight() != radius ?
                Bitmap.createScaledBitmap(bitmap, radius, radius, false) : bitmap);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
        final float circleX = scaledBitmap.getWidth() / 2 + 0.7f;
        final float circleY = scaledBitmap.getHeight() / 2 + 0.7f;
        final float circleRadius = scaledBitmap.getWidth() / 2 * 0.9f;

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        Bitmap resultBitmap = Bitmap.createBitmap(
                scaledBitmap.getWidth(), scaledBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        Bitmap shadowBitmap = resultBitmap.copy(resultBitmap.getConfig(), true);
        Canvas shadowCanvas = new Canvas(shadowBitmap);

        Canvas scaledCanvas = new Canvas(scaledBitmap);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        paint.setColor(0x10000000);
        scaledCanvas.drawRect(rect, paint);

        paint.setColor(Color.parseColor("#BAB399"));
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        canvas.drawCircle(circleX, circleY, circleRadius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(scaledBitmap, rect, rect, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        paint.setShadowLayer(6.0f, 0.0f, 4.0f, 0xff000000);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        shadowCanvas.drawCircle(circleX, circleY, circleRadius, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        shadowCanvas.drawBitmap(resultBitmap, rect, rect, paint);

        return shadowBitmap;
    }
}
