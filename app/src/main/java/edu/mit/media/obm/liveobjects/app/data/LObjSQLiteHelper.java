package edu.mit.media.obm.liveobjects.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import edu.mit.media.obm.liveobjects.app.data.LObjContract.*;
/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class LObjSQLiteHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "LiveObjects.db";
    public static final int DATABASE_VERSION = 4;

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ", ";

    private static final String SQL_CREATE_TABLE = "create table " +
            LiveObjectEntry.TABLE_NAME + "(" +
            LiveObjectEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT " + COMMA_SEP +
            LiveObjectEntry.COLUMN_NAME_ID + TEXT_TYPE + COMMA_SEP +
            LiveObjectEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
            LiveObjectEntry.COLUMN_NAME_GROUP + TEXT_TYPE + COMMA_SEP +
            LiveObjectEntry.COLUMN_NAME_URL + TEXT_TYPE + COMMA_SEP +
            LiveObjectEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
            LiveObjectEntry.COLUMN_NAME_FAVORITE + " INTEGER DEFAULT 0" + COMMA_SEP +
            LiveObjectEntry.COLUMN_NAME_ICON_FILEPATH + TEXT_TYPE + COMMA_SEP +
            LiveObjectEntry.COLUMN_NAME_MEDIA_TYPE + TEXT_TYPE + COMMA_SEP +
            LiveObjectEntry.COLUMN_NAME_MEDIA_FILEPATH + TEXT_TYPE + " )";


    private static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + LiveObjectEntry.TABLE_NAME;


    public LObjSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
