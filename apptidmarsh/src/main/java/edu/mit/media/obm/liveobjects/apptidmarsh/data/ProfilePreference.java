package edu.mit.media.obm.liveobjects.apptidmarsh.data;

import android.content.Context;
import android.content.SharedPreferences;

import edu.mit.media.obm.shair.liveobjects.R;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class ProfilePreference {


    public static SharedPreferences getInstance(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.profile_preference_file_key),
                Context.MODE_PRIVATE
        );
        return sharedPreferences;
    }

    public static void updateProfileInfo(SharedPreferences profilePreference, Context context, String name, String company, String email) {

        SharedPreferences.Editor editor = profilePreference.edit();
        editor.putString(
                context.getString(R.string.profile_name_key), name);
        editor.putString(
                context.getString(R.string.profile_company_key), company);
        editor.putString(
                context.getString(R.string.profile_email_key), email);
        editor.commit();

    }

    public static boolean isProfileCompleted(SharedPreferences profilePreference, Context context){
        return profilePreference.contains(context.getString(R.string.profile_name_key)) &&
                profilePreference.contains(context.getString(R.string.profile_email_key))&&
                !getString(profilePreference, context, R.string.profile_name_key).isEmpty() &&
                !getString(profilePreference, context, R.string.profile_email_key).isEmpty();

    }



    public static String getString(SharedPreferences profilePreference, Context context, int key) {
        return profilePreference.getString(
                context.getString(key),"");
    }

}
