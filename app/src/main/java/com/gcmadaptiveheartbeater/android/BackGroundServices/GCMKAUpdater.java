package com.gcmadaptiveheartbeater.android.BackGroundServices;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import com.gcmadaptiveheartbeater.android.Constants;
import com.gcmadaptiveheartbeater.android.Utilities;

/**
 * Created by mrahman on 24-Oct-16.
 */

public class GCMKAUpdater extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent gTalkHeartBeatIntent = new Intent("com.google.android.intent.action.GTALK_HEARTBEAT");
        Intent mcsHeartBeatIntent = new Intent("com.google.android.intent.action.MCS_HEARTBEAT");

        SharedPreferences pref = context.getSharedPreferences(Constants.SETTINGS_FILE, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, 0 /* request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        int lkgKA = pref.getInt(Constants.LKG_KA, 1);

        //
        // We are about to send GCM KA. Update the counter.
        //
        Utilities.incrementSetting(pref, Constants.GCM_KA_COUNT);

        System.out.println("Sending GCM KA.");
        context.sendBroadcast(gTalkHeartBeatIntent);
        context.sendBroadcast(mcsHeartBeatIntent);

        //
        // Now we need to update the timer. The existing pending intent
        // will get updated.
        //
        //
        alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + lkgKA * 60 * 1000, pIntent);
    }
}
