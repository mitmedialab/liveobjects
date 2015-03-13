package edu.mit.media.obm.liveobjects.app.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.mit.media.obm.shair.liveobjects.R;

/**
 * Created by arata on 3/13/15.
 */
public class AnimationArrayAdapter<T> extends ArrayAdapter<T> {
    final private static int ANIMATION_DURATION = 2000;

    private Context mContext;
    private LayoutInflater mInflater;
    private int mResource;
    private int mTextViewResourceId;
    private List<T> mObjects;

    private class Holder {
        public ImageView mImageView;
        public TextView mTextView;
    }

    public AnimationArrayAdapter(Context context, int resource, int textViewResourceId, List<T> objects) {
        super(context, resource ,textViewResourceId, objects);

        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResource = resource;
        mTextViewResourceId = textViewResourceId;
        mObjects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Holder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(mResource, null);

            holder = new Holder();
            holder.mTextView = (TextView) convertView.findViewById(mTextViewResourceId);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        String text = mObjects.get(position).toString();
        holder.mTextView.setText(text);

        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.bounce_scale);
        animation.setDuration(ANIMATION_DURATION);
        convertView.startAnimation(animation);

        return convertView;
    }
}
