package edu.mit.media.obm.liveobjects.app.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public final class LObjContract {

    public static final String AUTHORITY = "edu.mit.media.obm.liveobjects.app.provider";




    //To prevent accidental instantiation of this class we use an empty constructor
    public LObjContract() {}

    /* Inner class that defines the LiveObjectEntry table contents */
    public static abstract class LiveObjectEntry implements BaseColumns {


        public static final String BASE_PATH = "liveobjects";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/liveobjects";
        public static final String CONTENT_TYPE_ITEM = ContentResolver.CURSOR_DIR_BASE_TYPE + "/liveobject";

        public static final String TABLE_NAME = "live_object";
        public static final String COLUMN_NAME_ID = "name_id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_ICON_FILEPATH = "icon_filepath";
        public static final String COLUMN_NAME_MEDIA_FILEPATH = "media_filepath";
        public static final String COLUMN_NAME_MEDIA_TYPE = "media_type";
        public static final String COLUMN_NAME_FAVOURITE = "favourite";

        public static final String[] ALL_COLUMNS = {
                _ID,
                COLUMN_NAME_ID,
                COLUMN_NAME_TITLE,
                COLUMN_NAME_DESCRIPTION,
                COLUMN_NAME_ICON_FILEPATH,
                COLUMN_NAME_MEDIA_FILEPATH,
                COLUMN_NAME_MEDIA_TYPE,
                COLUMN_NAME_FAVOURITE
        };
    }


}
