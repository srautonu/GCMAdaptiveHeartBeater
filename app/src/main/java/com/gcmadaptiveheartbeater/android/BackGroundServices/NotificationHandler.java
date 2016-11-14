package com.gcmadaptiveheartbeater.android.BackGroundServices;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.gcmadaptiveheartbeater.android.Utilities;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.gcmadaptiveheartbeater.android.Constants;

import java.util.Map;

import static com.google.android.gms.internal.zzs.TAG;

/**
 * Created by mrahman on 18-Oct-16.
 */

public class NotificationHandler extends FirebaseMessagingService {

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
        String strType = data.get("Category");
        int notificationId = Integer.parseInt(data.get("NotificationId"));

        Log.i(TAG, "Notification Received> Type: " + strType + " Id: " + notificationId);

        //
        // Update the count in shared preference API
        //
        Utilities.incrementSetting(this, strType);
    }
}
