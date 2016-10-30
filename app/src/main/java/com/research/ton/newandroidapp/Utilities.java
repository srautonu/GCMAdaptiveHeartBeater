package com.research.ton.newandroidapp;

/**
 * Created by mrahman on 30-Oct-16.
 */

import android.content.SharedPreferences;

public class Utilities {

    public static void incrementSetting(SharedPreferences pref, String strName)
    {
        int value = pref.getInt(strName, 0);

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(strName, value + 1);
        editor.commit();

    }
}
