package edu.mit.media.obm.liveobjects.app.widget;

import android.app.ActionBar;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.mit.media.obm.liveobjects.app.data.LObjContentProvider;
import edu.mit.media.obm.liveobjects.app.data.LObjContract;
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

    private RandomColorGenerator mRandomColorGenerator;

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

            int w = holder.mImageView.getMeasuredWidth(), h = holder.mImageView.getMeasuredHeight();

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        String text = mObjects.get(position).toString();
        holder.mTextView.setText(text);

        setImage(holder.mImageView, text);

        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.bounce_scale);
        animation.setInterpolator(new SpringInterpolator());
        convertView.startAnimation(animation);

        return convertView;
    }

    private void setImage(RoundedImageView imageView, String liveObjectName) {
        Cursor cursor = LObjContentProvider.getLocalLiveObject(liveObjectName, mContext);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            String imageFilePath = cursor.getString(cursor.getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_ICON_FILEPATH));

            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            bitmap = bitmap.createScaledBitmap(bitmap, layoutParams.width, layoutParams.height, true);

            BitmapEditor bitmapEditor = new BitmapEditor(mContext);
            bitmapEditor.blurBitmap(bitmap, 2);
            BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);

            imageView.setImageDrawable(bitmapDrawable);
        } else {
            imageView.setFillColor(mRandomColorGenerator.getNextColor());
        }
    }

    private class RandomColorGenerator {
        private final static int HUE_OFFSET = 150;
        private int mCurrentHue;

        public RandomColorGenerator() {
            mCurrentHue = 0;
        }

        public int getNextColor() {
            float[] hsv = new float[3];
            hsv[0] = (float) mCurrentHue;
            hsv[1] = 1.0f;
            hsv[2] = 0.75f;

            mCurrentHue = (mCurrentHue + HUE_OFFSET) % 360;

            return Color.HSVToColor(hsv);
        }
    }
}
