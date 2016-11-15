package com.gcmadaptiveheartbeater.android;

/**
 * Created by mrahman on 30-Oct-16.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utilities {
    public static boolean isExperimentRunning(Context context)
    {
        return context.getSharedPreferences(Constants.SETTINGS_FILE, 0).getBoolean(Constants.EXP_IN_PROGRESS, false);
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

    public static void putExpModel(Context context, int model)
    {
        SharedPreferences pref = context.getSharedPreferences(Constants.SETTINGS_FILE, 0);
        pref.edit().putInt(Constants.EXP_MODEL, model).commit();
        notifySettingsChanged(context);
    }

    public static void incrementGCMKACount(Context context)
    {
        if (!isExperimentRunning(context))
            return;

        SharedPreferences pref = context.getSharedPreferences(Constants.SETTINGS_FILE, 0);
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt(Constants.GCM_KA_COUNT, 1 + pref.getInt(Constants.GCM_KA_COUNT, 0));
        editor.putString(Constants.GCM_KA_TIMESTAMP, new SimpleDateFormat("MM/dd/yyyy h:mm:ss a").format(new Date()));

        editor.commit();
        notifySettingsChanged(context);
    }

    public static void incrementTestKACount(Context context)
    {
        if (!isExperimentRunning(context))
            return;

        SharedPreferences pref = context.getSharedPreferences(Constants.SETTINGS_FILE, 0);
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt(Constants.TEST_KA_COUNT, 1 + pref.getInt(Constants.TEST_KA_COUNT, 0));
        editor.putString(Constants.TEST_KA_TIMESTAMP, new SimpleDateFormat("MM/dd/yyyy h:mm:ss a").format(new Date()));

        editor.commit();
        notifySettingsChanged(context);
    }

    public static void setExperimentRunning(Context context, boolean fRunning)
    {
        SharedPreferences.Editor editor = context.getSharedPreferences(Constants.SETTINGS_FILE, 0).edit();
        editor.putBoolean(Constants.EXP_IN_PROGRESS, fRunning);
        if (fRunning)
        {
            editor.putString(Constants.EXP_START_TIMESTAMP, new SimpleDateFormat("MM/dd/yyyy h:mm:ss a").format(new Date()));
        }
        else
        {
            editor.putString(Constants.EXP_END_TIMESTAMP, new SimpleDateFormat("MM/dd/yyyy h:mm:ss a").format(new Date()));
        }

        editor.commit();
        notifySettingsChanged(context);
    }

    public static  void incrementSetting(Context context, String strName)
    {
        updateSetting(context, strName, 1 + context.getSharedPreferences(Constants.SETTINGS_FILE, 0).getInt(strName, 0));
    }

    public static void updateSetting(Context context, String strName, int value)
    {
        if (!isExperimentRunning(context))
            return;

        SharedPreferences.Editor editor = context.getSharedPreferences(Constants.SETTINGS_FILE, 0).edit();
        editor.putInt(strName, value);
        editor.commit();

        notifySettingsChanged(context);
    }

    public static void updateSetting(Context context, String strName, String value)
    {
        if (!isExperimentRunning(context))
            return;

        SharedPreferences.Editor editor = context.getSharedPreferences(Constants.SETTINGS_FILE, 0).edit();
        editor.putString(strName, value);
        editor.commit();

        notifySettingsChanged(context);
    }

    private static void notifySettingsChanged(Context context)
    {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.ACTION_HANDLE_SETTINGS_UPDATE));
    }
}
