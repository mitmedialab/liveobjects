package edu.mit.media.obm.liveobjects.app.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

/**
 * Created by artimo14 on 3/18/15.
 */
public class BitmapEditor {
    Context mContext;

    public BitmapEditor(Context context) {
        mContext = context;
    }

    public Bitmap cropToAspectRatio(Bitmap bitmap, float aspectRatio) {
        float bitmapAspectRatio =
                (float)bitmap.getHeight() / bitmap.getWidth();

        Bitmap croppedBitmap;
        if (bitmapAspectRatio < aspectRatio) {
            int croppedWidth = (int)(bitmap.getWidth() * bitmapAspectRatio / aspectRatio);
            croppedBitmap = Bitmap.createBitmap(bitmap,
                    (bitmap.getWidth() - croppedWidth) / 2, 0, croppedWidth, bitmap.getHeight());
        } else {
            int croppedHeight = (int)(bitmap.getHeight() * aspectRatio / bitmapAspectRatio);
            croppedBitmap = Bitmap.createBitmap(bitmap,
                    0, (bitmap.getHeight() - croppedHeight) / 2, bitmap.getWidth(), croppedHeight);
        }

        return croppedBitmap;
    }

    public Bitmap cropToDisplayAspectRatio(Bitmap bitmap, WindowManager windowManager) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        float displayAspectRatio = (float)displayMetrics.heightPixels / displayMetrics.widthPixels;

        return cropToAspectRatio(bitmap, displayAspectRatio);
    }

    public void blurBitmap(Bitmap bitmap, int radius) {
        RenderScript renderScript = RenderScript.create(mContext);

        final Allocation input = Allocation.createFromBitmap(renderScript, bitmap);
        final Allocation output = Allocation.createTyped(renderScript, input.getType());
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        script.setRadius(radius);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(bitmap);
    }

}
