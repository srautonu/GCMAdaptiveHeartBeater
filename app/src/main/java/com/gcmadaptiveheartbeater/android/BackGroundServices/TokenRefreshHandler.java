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
//        final String strServerDNS = "www.ekngine.com";
//        final int serverPort = 8080;
//
//        String deviceName = getSharedPreferences(Constants.SETTINGS_FILE, 0).getString(Constants.DEVICE_NAME, "");
//        if (deviceName.isEmpty())
//            return;

//        try {
//            System.out.println("Connecting to " + strServerDNS + ":" + serverPort + "...\n");
//
//            Socket sock = new Socket(strServerDNS, serverPort);
//            DataOutputStream sock_out = new DataOutputStream(sock.getOutputStream());
//            //
//            // Set the socket read timeout to 30 seconds.
//            //
//            sock.setSoTimeout(30 * 1000);
//            sock_out.writeBytes("DVC " + deviceName + " " + refreshedToken);
//            sock_out.close();
//            sock.close();
//
//            System.out.println("Done.\n");
//        }
//        catch (IOException e)
//        {
//            System.out.println(e);
//
//        }

    }
}
