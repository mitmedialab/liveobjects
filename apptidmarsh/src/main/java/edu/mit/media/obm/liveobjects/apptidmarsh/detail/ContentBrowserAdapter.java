package edu.mit.media.obm.liveobjects.apptidmarsh.detail;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.noveogroup.android.log.Log;

import java.io.IOException;
import java.io.InputStream;
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
 * Created by artimo14 on 8/20/15.
 */
public class ContentBrowserAdapter extends ArrayAdapter<Map<String, Object>> {
    @Inject ContentController mContentController;

    private final Context mContext;
    private final MLProjectPropertyProvider mPropertyProvider;

    // TODO to incorporate in the json file
    private static final String IMAGE_FOLDER = "DCIM";

    public ContentBrowserAdapter(Context context, MLProjectPropertyProvider propertyProvider) {
        super(context, R.layout.saved_live_object_row, propertyProvider.getContents());
        DependencyInjector.inject(this, context);

        mPropertyProvider = propertyProvider;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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

        String title = mPropertyProvider.getProjectTitle(position);
        holder.mTitleView.setText(title);

        // ToDo: set correct index
        String iconFileName = mPropertyProvider.getIconFileName(position);
        String liveObjectId = mPropertyProvider.getId();

        final ContentId iconContentId = new ContentId(liveObjectId, IMAGE_FOLDER, iconFileName);
        setIconImage(holder, iconContentId);


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
        return mPropertyProvider.getContents().size();
    }

    private void setIconImage(final ViewHolder holder, final ContentId contentId) {
        new AsyncTask<Void, Void, InputStream>() {
            @Override
            protected InputStream doInBackground(Void... params) {
                Log.v("doInBackground()");

                try {
                    return mContentController.getInputStreamContent(contentId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(InputStream imageInputStream) {
                Log.v("onPostExecute()");

                try {
                    Bitmap bitmap = Util.getBitmap(imageInputStream);
                    BitmapEditor bitmapEditor = new BitmapEditor(mContext);
                    Bitmap croppedBitmap = bitmapEditor.cropToAspectRatio(bitmap, 1.0F);
                    holder.mIconView.setImageBitmap(croppedBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

}
