package com.gcmadaptiveheartbeater.android.BackGroundServices;

import android.provider.ContactsContract;
import android.util.Log;

import com.gcmadaptiveheartbeater.android.Constants;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static com.google.android.gms.internal.zzs.TAG;

/**
 * Created by mrahman on 18-Oct-16.
 */

public class TokenRefreshHandler extends FirebaseInstanceIdService
{
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // TODO: Implement this method to send any registration to your app's servers.
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String refreshedToken) {

    }
}
