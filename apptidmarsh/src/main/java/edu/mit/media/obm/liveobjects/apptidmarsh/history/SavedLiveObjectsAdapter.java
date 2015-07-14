package edu.mit.media.obm.liveobjects.apptidmarsh.history;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import edu.mit.media.obm.liveobjects.apptidmarsh.LiveObjectsApplication;
import edu.mit.media.obm.liveobjects.apptidmarsh.data.MLProjectPropertyProvider;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.Util;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.BitmapEditor;
import edu.mit.media.obm.liveobjects.middleware.common.ContentId;
import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.shair.liveobjects.R;


/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class SavedLiveObjectsAdapter extends ArrayAdapter<Map<String,Object>> {
    private static final String LOG_TAG = SavedLiveObjectsAdapter.class.getSimpleName();

    private final Context mContext;
    private final List<Map<String,Object>> mLiveObjectsPropertiesList;

    private final ContentController mContentController;

    // TODO to incorporate in the json file
    private static final String IMAGE_FOLDER = "DCIM";

    public SavedLiveObjectsAdapter(Context context, List<Map<String,Object>> liveObjectsPropertiesList) {
        super(context, R.layout.saved_live_object_row, liveObjectsPropertiesList );
        mContext = context;
        mLiveObjectsPropertiesList = liveObjectsPropertiesList;

        mContentController = ((LiveObjectsApplication) ((Activity) mContext).getApplication()).getMiddleware().getContentController();

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Map<String, Object> liveObjectProperties = mLiveObjectsPropertiesList.get(position);
        MLProjectPropertyProvider provider = new MLProjectPropertyProvider(liveObjectProperties);

        String iconFileName = provider.getIconFileName();
        String title = provider.getProjectTitle();
        String liveObjectId = provider.getId();

        final ContentId iconContentId = new ContentId(liveObjectId,IMAGE_FOLDER, iconFileName);



                LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.saved_live_object_row, parent, false);

        final ImageView iconView = (ImageView) rowView.findViewById(R.id.row_item_icon_imageview);
        TextView titleView = (TextView) rowView.findViewById(R.id.row_item_title_textview);
        titleView.setText(title);
        try {
            InputStream imageInputStream = mContentController.getInputStreamContent(iconContentId);
            Bitmap bitmap = Util.getBitmap(imageInputStream);
            BitmapEditor bitmapEditor = new BitmapEditor(mContext);
            Bitmap croppedBitmap = bitmapEditor.cropToAspectRatio(bitmap, 1.0F);
            iconView.setImageBitmap(croppedBitmap);
        } catch (Exception e) {
            Log.e(LOG_TAG, "error setting icon image", e);
        }


        return rowView;

    }

    @Override
    public int getCount() {
        return mLiveObjectsPropertiesList.size();
    }
}
