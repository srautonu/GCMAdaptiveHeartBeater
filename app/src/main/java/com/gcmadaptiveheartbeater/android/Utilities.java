package com.gcmadaptiveheartbeater.android;

/**
 * Created by mrahman on 30-Oct-16.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

public class Utilities {
    public static boolean isExperimentRunning(SharedPreferences pref)
    {
        return pref.getBoolean(Constants.EXP_IN_PROGRESS, false);
    }

    public static int getExpModel(Context context)
    {
        //
        // Model #1: Android as is. No KA testing, No additional GCM KA
        // Model #2: Agressive GCM KA (1 minute). No KA Testing
        // Model #3: KA testing. GCM Adaptive KA
        //
        // Default to Model 3 (Adaptive KA)
        //
        return context.getSharedPreferences(Constants.SETTINGS_FILE, 0).getInt(Constants.EXP_MODEL, 3);
    }

    public static void setExperimentRunning(SharedPreferences pref, boolean fRunning)
    {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(Constants.EXP_IN_PROGRESS, fRunning);
        editor.commit();
    }

    public static  void incrementSetting(Context context, String strName)
    {
        SharedPreferences pref = context.getSharedPreferences(Constants.SETTINGS_FILE, 0);
        updateSetting(context, strName, 1 + pref.getInt(strName, 0));
    }

    public static void updateSetting(Context context, String strName, int value)
    {
        SharedPreferences pref = context.getSharedPreferences(Constants.SETTINGS_FILE, 0);

        if (!isExperimentRunning(pref))
            return;

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(strName, value);
        editor.commit();

        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.ACTION_HANDLE_SETTINGS_UPDATE));
    }

    public static void updateSetting(Context context, String strName, String value)
    {
        SharedPreferences pref = context.getSharedPreferences(Constants.SETTINGS_FILE, 0);

        if (!isExperimentRunning(pref))
            return;

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(strName, value);
        editor.commit();

        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.ACTION_HANDLE_SETTINGS_UPDATE));
    }

}
