package edu.mit.media.obm.liveobjects.apptidmarsh.detail;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import edu.mit.media.obm.liveobjects.apptidmarsh.data.MLProjectPropertyProvider;
import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;
import edu.mit.media.obm.shair.liveobjects.R;

/**
 * Created by artimo14 on 8/20/15.
 */
public class ContentBrowserAdapter extends ArrayAdapter<Map<String, Object>> {
    private static final String LOG_TAG = ContentBrowserAdapter.class.getSimpleName();

    private final Context mContext;
    private final List<Map<String, Object>> mContentPropertiesList;

    public ContentBrowserAdapter(Context context, List<Map<String, Object>> contentPropertiesList) {
        super(context, R.layout.saved_live_object_row, contentPropertiesList);
        DependencyInjector.inject(this, context);

        mContext = context;
        mContentPropertiesList = contentPropertiesList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Map<String, Object> liveObjectProperties = mContentPropertiesList.get(position);
        MLProjectPropertyProvider provider = new MLProjectPropertyProvider(liveObjectProperties);

        String title = provider.getProjectTitle(position);

        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.saved_live_object_row, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        holder.mTitleView.setText(title);

        return convertView;
    }

    static class ViewHolder {
        @Bind(R.id.row_item_title_textview) TextView mTitleView;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public int getCount() {
        return mContentPropertiesList.size();
    }
}
