package edu.mit.media.obm.liveobjects.apptidmarsh.history;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.noveogroup.android.log.Log;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import edu.mit.media.obm.liveobjects.apptidmarsh.data.MLProjectPropertyProvider;
import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.Util;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.BitmapEditor;
import edu.mit.media.obm.liveobjects.middleware.common.ContentId;
import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.shair.liveobjects.R;


/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class SavedLiveObjectsAdapter extends ArrayAdapter<Map<String, Object>> {
    private final Context mContext;
    private final List<Map<String, Object>> mLiveObjectsPropertiesList;

    @Inject ContentController mContentController;

    // TODO to incorporate in the json file
    private static final String IMAGE_FOLDER = "DCIM";

    public SavedLiveObjectsAdapter(Context context, List<Map<String, Object>> liveObjectsPropertiesList) {
        super(context, R.layout.saved_live_object_row, liveObjectsPropertiesList);
        DependencyInjector.inject(this, context);

        mContext = context;
        mLiveObjectsPropertiesList = liveObjectsPropertiesList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Map<String, Object> liveObjectProperties = mLiveObjectsPropertiesList.get(position);
        MLProjectPropertyProvider provider = new MLProjectPropertyProvider(liveObjectProperties);

        // ToDo: set correct index
        String iconFileName = provider.getIconFileName(0);
        String title = provider.getProjectTitle(0);
        String liveObjectId = provider.getId();

        final ContentId iconContentId = new ContentId(liveObjectId, IMAGE_FOLDER, iconFileName);

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
        try {
            InputStream imageInputStream = mContentController.getInputStreamContent(iconContentId);
            Bitmap bitmap = Util.getBitmap(imageInputStream);
            BitmapEditor bitmapEditor = new BitmapEditor(mContext);
            Bitmap croppedBitmap = bitmapEditor.cropToAspectRatio(bitmap, 1.0F);
            holder.mIconView.setImageBitmap(croppedBitmap);
        } catch (Exception e) {
            Log.e("error setting icon image", e);
        }

        return convertView;
    }

    static class ViewHolder {
        @Bind(R.id.row_item_icon_imageview) ImageView mIconView;
        @Bind(R.id.row_item_title_textview) TextView mTitleView;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public int getCount() {
        return mLiveObjectsPropertiesList.size();
    }
}
