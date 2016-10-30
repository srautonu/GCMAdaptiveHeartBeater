package com.research.ton.newandroidapp.BackGroundServices;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.research.ton.newandroidapp.Constants;

import java.util.Map;

import static com.google.android.gms.internal.zzs.TAG;

/**
 * Created by mrahman on 18-Oct-16.
 */

public class NotificationHandler extends FirebaseMessagingService {

    public static final String PREFS_NAME = "com.research.ton.newandroidapp.NOTIFICATION_COUNT";

    public static String[] rgStrNotificationTag = {
            "Email",
            "Social",
            "Message",
            "Call",
            "Unknown"
    };

    int getNotificationTypeId(String strTag)
    {
        for (int i = 0; i < rgStrNotificationTag.length; i++)
        {
            if (rgStrNotificationTag[i].equalsIgnoreCase(strTag))
                return i;
        }

        // The last index is used for all unknown notification tags
        return rgStrNotificationTag.length - 1;
    }

    //
    // FCM messages are handled here.
    // If the application is in the foreground handle both data and notification messages here.
    // Also if you intend on generating your own notifications as a result of a received FCM
    // message, here is where that should be initiated. See sendNotification method below.
    //
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        Map<String, String> data = remoteMessage.getData();
        String strType = remoteMessage.getData().get("type");
        int typeId, notifCount;

        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification type: " + strType);

        typeId = getNotificationTypeId(strType);

        //
        // Update the count in shared preference API
        //
        SharedPreferences settings = getSharedPreferences(Constants.SETTINGS_FILE, 0);
        notifCount = settings.getInt(rgStrNotificationTag[typeId], 0) + 1;

        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(rgStrNotificationTag[typeId], notifCount);
        editor.commit();

        Intent localIntent = new Intent(PREFS_NAME);

        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
