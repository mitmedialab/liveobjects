package edu.mit.media.obm.liveobjects.app.utils;

import android.content.Context;
import android.database.Cursor;

import edu.mit.media.obm.liveobjects.app.data.LObjContract;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class EmailFormatter {

    public static String getSubject() {
        return "MIT Media Lab Member Week: Live Objects Summary";
    }

    public static String getBody(Context context) {
        Cursor cursor = context.getContentResolver().query(
                LObjContract.LiveObjectEntry.CONTENT_URI,
                LObjContract.LiveObjectEntry.ALL_COLUMNS,
                null, null, null);

        StringBuilder builder = new StringBuilder();
        builder.append("Thank you for using the Live Objects App, ");
        builder.append("a project by Object-Based Media Group, MIT Media Lab. \n");
        builder.append("http://liveobjects.media.mit.edu \n");

        builder.append("\n\n");

        builder.append("Project list: \n\n");


        while (cursor.moveToNext()) {

            builder.append("Project Title: ");
            String projectTitle = cursor.getString(
                    cursor.getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_TITLE));
            builder.append(projectTitle);
            builder.append("\n");

            builder.append("Group: ");
            String group = cursor.getString(
                    cursor.getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_GROUP));
            builder.append(group);
            builder.append("\n");

            builder.append("Website: ");
            String website = cursor.getString(
                    cursor.getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_URL));
            builder.append(website);
            builder.append("\n");

            builder.append("Description: ");
            String description = cursor.getString(
                    cursor.getColumnIndex(LObjContract.LiveObjectEntry.COLUMN_NAME_DESCRIPTION));
            builder.append(description);
            builder.append("\n");


            builder.append("\n");

        }



        return builder.toString();


    }
}
