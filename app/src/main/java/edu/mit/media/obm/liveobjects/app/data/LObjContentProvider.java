package edu.mit.media.obm.liveobjects.app.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.Arrays;
import java.util.HashSet;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class LObjContentProvider extends ContentProvider{

    // database helper
    private LObjSQLiteHelper mDBHelper;

    private static final int LIVEOBJECTS = 1;
    private static final int LIVEOBJECT_ID = 2;


    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        //defines which URIs the content provider has to match
        sUriMatcher.addURI(LObjContract.AUTHORITY, LObjContract.BASE_PATH, LIVEOBJECTS);
        sUriMatcher.addURI(LObjContract.AUTHORITY, LObjContract.BASE_PATH + "/#", LIVEOBJECT_ID);
    }

    @Override
    public boolean onCreate() {
        mDBHelper = new LObjSQLiteHelper(getContext());
        return false;
    }

    @Override
    public String getType(Uri uri) {
        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case LIVEOBJECTS:
                return LObjContract.CONTENT_TYPE;
            case LIVEOBJECT_ID:
                return LObjContract.CONTENT_TYPE_ITEM;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case LIVEOBJECTS:
                id = db.insert(LObjContract.LiveObjectEntry.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return ContentUris.withAppendedId(LObjContract.CONTENT_URI, id);

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        //check if the caller has requested a column which does not exists
        checkColumns(projection);

        // set the table
        queryBuilder.setTables(LObjContract.LiveObjectEntry.TABLE_NAME);

        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case LIVEOBJECTS:
                break;
            case LIVEOBJECT_ID:
                queryBuilder.appendWhere(LObjContract.LiveObjectEntry._ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);

        }

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    private void checkColumns(String[] projection) {
        String[] available = { LObjContract.LiveObjectEntry._ID,
                LObjContract.LiveObjectEntry.COLUMN_NAME_DESCRIPTION,
                LObjContract.LiveObjectEntry.COLUMN_NAME_FAVOURITE,
                LObjContract.LiveObjectEntry.COLUMN_NAME_ICON_FILENAME,
                LObjContract.LiveObjectEntry.COLUMN_NAME_MEDIA_FILENAME,
                LObjContract.LiveObjectEntry.COLUMN_NAME_MEDIA_TYPE,
                LObjContract.LiveObjectEntry.COLUMN_NAME_TITLE};

        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
