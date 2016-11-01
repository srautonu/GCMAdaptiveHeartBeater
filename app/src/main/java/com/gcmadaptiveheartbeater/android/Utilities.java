package com.gcmadaptiveheartbeater.android;

/**
 * Created by mrahman on 30-Oct-16.
 */

import android.content.SharedPreferences;

public class Utilities {
    public static boolean isExperimentRunning(SharedPreferences pref)
    {
        return pref.getBoolean(Constants.EXP_IN_PROGRESS, false);
    }

    public static void setExperimentRunning(SharedPreferences pref, boolean fRunning)
    {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(Constants.EXP_IN_PROGRESS, fRunning);
        editor.commit();
    }

    public static  void incrementSetting(SharedPreferences pref, String strName)
    {
        if (!isExperimentRunning(pref))
            return;

        int value = pref.getInt(strName, 0);

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(strName, value + 1);
        editor.commit();
    }

    public static void updateSetting(SharedPreferences pref, String strName, int value)
    {
        if (!isExperimentRunning(pref))
            return;

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(strName, value);
        editor.commit();
    }
}
