//
// This file contains code to start/stop different services.
//
package com.gcmadaptiveheartbeater.android.BackGroundServices;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.gcmadaptiveheartbeater.android.Constants;
import com.gcmadaptiveheartbeater.android.SettingsUtil;

/**
 * Created by mrahman on 26-Nov-16.
 */

public class ServicesMgr {
    public static void startExperiment(
            Context context,
            String strDeviceId,
            int expModel,
            int dataKARateM
        )
    {
        //
        // Clear all cached settings
        //
        context.getSharedPreferences(Constants.SETTINGS_FILE, 0).edit().clear().commit();

        //
        // Mark experiment running and bootstrap information
        //
        SettingsUtil.setExperimentRunning(context, true);
        SettingsUtil.putDeviceId(context, strDeviceId);
        SettingsUtil.putExpModel(context, expModel);
        if (expModel == Constants.EXP_MODEL_FIXED_RATE)
        {
            SettingsUtil.updateSetting(context, Constants.DATA_KA, dataKARateM);
        }

        startExperimentClock(context);

        if (expModel != Constants.EXP_MODEL_ANDROID) {
            startDataKA(context);
        }

        if (expModel == Constants.EXP_MODEL_ADAPTIVE)
        {
            startTestKA(context);
        }
    }

    public static void endExperiment(Context context)
    {
        int expModel = SettingsUtil.getExpModel(context);

        if (expModel == Constants.EXP_MODEL_ADAPTIVE)
        {
            stopTestKA(context);
        }

        if (expModel != Constants.EXP_MODEL_ANDROID) {
            stopDataKA(context);
        }

        SettingsUtil.setExperimentRunning(context, false);
    }

    private static void startExperimentClock(Context context)
    {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Set up an alarm for 12 hour trigger
        alarm.set(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 12 * 60 * 60 * 1000,
                PendingIntent.getBroadcast(context, 0, new Intent(Constants.ACTION_END_EXPERIMENT), PendingIntent.FLAG_UPDATE_CURRENT)
        );
    }

    // request a DATA KA immediately
    private static void startDataKA(Context context)
    {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Set up an alarm for immediate trigger
        alarm.set(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                PendingIntent.getBroadcast(context, 0, new Intent(Constants.ACTION_SEND_DATA_KA), PendingIntent.FLAG_UPDATE_CURRENT)
        );
    }

    private static void stopDataKA(Context context)
    {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent mServiceIntent = new Intent(context, KADataService.class);

        Intent intentDataKA = new Intent(Constants.ACTION_SEND_DATA_KA);
        PendingIntent piDataKA = PendingIntent.getBroadcast(context, 0, intentDataKA, PendingIntent.FLAG_UPDATE_CURRENT);

        alarm.cancel(piDataKA);

        context.stopService(mServiceIntent);
    }

    private static void startTestKA(Context context)
    {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intentStartKA = new Intent(Constants.ACTION_START_KA_TESTING);
        PendingIntent piStartKA = PendingIntent.getBroadcast(context, 0, intentStartKA, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set up an alarm for immediate trigger
        alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), piStartKA);
    }

    private static void stopTestKA(Context context)
    {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intentSendKA = new Intent(Constants.ACTION_SEND_TEST_KA);
        Intent intentStartKA = new Intent(Constants.ACTION_START_KA_TESTING);
        Intent mServiceIntent = new Intent(context, KATesterService.class);

        PendingIntent piSendKA = PendingIntent.getBroadcast(context, 0, intentSendKA, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent piStartKA = PendingIntent.getBroadcast(context, 0, intentStartKA, PendingIntent.FLAG_UPDATE_CURRENT);

        alarm.cancel(piSendKA);
        alarm.cancel(piStartKA);

        context.stopService(mServiceIntent);
    }
}
