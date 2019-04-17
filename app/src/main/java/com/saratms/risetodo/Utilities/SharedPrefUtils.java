package com.saratms.risetodo.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Sarah Al-Shamy on 16/03/2019.
 */

public class SharedPrefUtils {

    public static SharedPreferences sharedPreferences;

    public static String getSoundSharedPref(Context context) {
        sharedPreferences = context.getSharedPreferences("shared_pref", Context.MODE_PRIVATE);
        return (sharedPreferences.getString("sound", "yes"));
    }
}
