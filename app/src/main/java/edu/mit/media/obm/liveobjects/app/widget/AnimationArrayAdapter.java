package edu.mit.media.obm.liveobjects.app.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.noveogroup.android.log.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.mit.media.obm.liveobjects.app.LiveObjectsApplication;
import edu.mit.media.obm.liveobjects.app.data.MLProjectPropertyProvider;
import edu.mit.media.obm.liveobjects.app.utils.Util;
import edu.mit.media.obm.liveobjects.middleware.common.ContentId;
import edu.mit.media.obm.liveobjects.middleware.common.MiddlewareInterface;
import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;
import edu.mit.media.obm.shair.liveobjects.R;

/**
 * Created by arata on 3/13/15.
 */
public class AnimationArrayAdapter<T> extends ArrayAdapter<T> {
    private Context mContext;
    private LayoutInflater mInflater;
    private int mResource;
    private int mTextViewResourceId;
    private List<T> mObjects;

    private List<String> mNewObjects;
    private List<String> mOldObjects;

    private RandomColorGenerator mRandomColorGenerator;

    private MiddlewareInterface mMiddleware;
    private DbController mDbController;
    private ContentController mContentController;

    //TODO to incorporate in the live object
    private static final String ICON_FOLDER = "DCIM";

    private class Holder {
        public RoundedImageView mImageView;
        public TextView mTextView;
    }

    public AnimationArrayAdapter(Context context, int resource, int textViewResourceId, List<T> objects) {
        super(context, resource, textViewResourceId, objects);

        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResource = resource;
        mTextViewResourceId = textViewResourceId;
        mObjects = objects;

        mMiddleware = ((LiveObjectsApplication) ((Activity) mContext).getApplication()).getMiddleware();
        mContentController = mMiddleware.getContentController();
        mDbController = mMiddleware.getDbController();

        mNewObjects = new ArrayList<>();
        for (T object : mObjects) {
            String text = object.toString();
            mNewObjects.add(text);
        }
        mOldObjects = new ArrayList<>();

        mRandomColorGenerator = new RandomColorGenerator();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Holder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(mResource, null);

            holder = new Holder();
            holder.mTextView = (TextView) convertView.findViewById(mTextViewResourceId);
            holder.mImageView = (RoundedImageView) convertView.findViewById(R.id.grid_item_icon);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        final String text = mObjects.get(position).toString();
        holder.mTextView.setText(text);
        addLineBreakIfNecessary(holder.mTextView);

        setImage(holder.mImageView, text);

        if (mNewObjects.contains(text)) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.bounce_scale);
            animation.setInterpolator(new SpringInterpolator());
            convertView.startAnimation(animation);
        }

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        mNewObjects.clear();
        for (T object : mObjects) {
            String text = object.toString();
            if (!mOldObjects.contains(text)) {
                mNewObjects.add(text);
            }
        }

        mOldObjects.clear();
        for (T object : mObjects) {
            String text = object.toString();
            mOldObjects.add(text);
        }
    }

    private void setImage(RoundedImageView imageView, String liveObjectName) {
        if (!mDbController.isLiveObjectEmpty(liveObjectName)) {
            Map<String, Object> liveObjectProperties = mDbController.getProperties(liveObjectName);
            MLProjectPropertyProvider provider = new MLProjectPropertyProvider(liveObjectProperties);
            String iconFileName = provider.getIconFileName();
            ContentId iconContentId = new ContentId(liveObjectName,ICON_FOLDER, iconFileName);
            try {
                InputStream imageInputStream = mContentController.getInputStreamContent(iconContentId);
                Bitmap bitmap = Util.getBitmap(imageInputStream);
                ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                bitmap = bitmap.createScaledBitmap(bitmap, layoutParams.width, layoutParams.height, true);

                BitmapEditor bitmapEditor = new BitmapEditor(mContext);
                bitmapEditor.blurBitmap(bitmap, 2);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);

                imageView.setImageDrawable(bitmapDrawable);
            } catch (Exception e) {
                Log.e("error setting icon image", e);
            }

        }
        else {
            imageView.setFillColor(mRandomColorGenerator.generateColor(liveObjectName));
        }
    }

    private void addLineBreakIfNecessary(final TextView textView) {
        textView.setVisibility(View.INVISIBLE);

        textView.post(new Runnable() {
            @Override
            public void run() {
                int lineCount = textView.getLineCount();
                String originalText = textView.getText().toString();

                if (lineCount > 1) {
                    if (originalText.contains("\n")) {
                        // prevent to be processed multiple times (getView() may be called more than once)
                        return;
                    }

                    for (int line = 0; line < lineCount - 1; line++) {
                        processLine(textView, lineCount, line);
                    }
                }

                int newLineCount = textView.getLineCount();
                if (newLineCount > lineCount) {
                    if (newLineCount == 3) {
                        textView.setText(originalText);

                        for (int line = 0; line < newLineCount - 1; line++) {
                            processLine(textView, newLineCount, line);
                        }
                    }
                }


                textView.setVisibility(View.VISIBLE);
            }

            private void processLine(final TextView textView, int lineCount, int line) {
                String formatText = textView.getText().toString();
                int textLen = formatText.length();
                int lineBreakPos = textLen * (line + 1) / lineCount;

                int capitalClosestToCenter = -1;
                int closestDistanceFromCenter = Integer.MAX_VALUE;
                for (int i = 0; i < textLen; i++) {
                    char c = formatText.charAt(i);
                    if (Character.isUpperCase(c) ||
                            (i > 0 && !Character.isLetterOrDigit(formatText.charAt(i - 1)))) {
                        int distanceFromCenter = Math.abs(i - lineBreakPos);
                        if (distanceFromCenter < closestDistanceFromCenter) {
                            closestDistanceFromCenter = distanceFromCenter;
                            capitalClosestToCenter = i;
                        }
                    }
                }

                if (capitalClosestToCenter > 0) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(formatText.substring(0, capitalClosestToCenter));
                    builder.append("\n");
                    builder.append(formatText.substring(capitalClosestToCenter));

                    textView.setText(builder.toString());
                }
            }
        });
    }

    private class RandomColorGenerator {
        private final static int HUE_OFFSET = 150;
        private int mCurrentHue;
        private Map<String, Integer> colorMap;

        public RandomColorGenerator() {
            mCurrentHue = 0;
            colorMap = new HashMap<>();
        }

        public int generateColor(String id) {
            if (colorMap.containsKey(id)) {
                return colorMap.get(id);
            }

            float[] hsv = new float[3];
            hsv[0] = (float) mCurrentHue;
            hsv[1] = 1.0f;
            hsv[2] = 0.75f;

            mCurrentHue = (mCurrentHue + HUE_OFFSET) % 360;
            int color = Color.HSVToColor(hsv);

            colorMap.put(id, color);

            return color;
        }
    }
}
