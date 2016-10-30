package com.research.ton.newandroidapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.research.ton.newandroidapp.BackGroundServices.*;

public class MainActivity extends AppCompatActivity {

    private Button btn;
    private EditText txf;
    private TextView[] view = new TextView[4];

    private void setupMocks()
    {
        SharedPreferences pref = getSharedPreferences(Constants.SETTINGS_FILE, 0);
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt(Constants.LKG_KA, 16);
        editor.putInt(Constants.LKB_KA, 32);
        editor.putInt(Constants.TEST_KA_COUNT, 5);
        editor.putInt(Constants.GCM_KA_COUNT, 10);

        editor.commit();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //
        // DEBUG HOOK
        //
        // setupMocks();


        setContentView(R.layout.activity_main);

        btn = (Button) findViewById(R.id.button);
        txf = (EditText) findViewById(R.id.Txtfld);
        view[0] = (TextView) findViewById(R.id.view);
        view[1] = (TextView) findViewById(R.id.view2);
        view[2] = (TextView) findViewById(R.id.view3);
        view[3] = (TextView) findViewById(R.id.view4);

        updateNotificationCount();

        LocalBroadcastManager.getInstance(this).registerReceiver(
            new BroadcastReceiver()
            {
                public void onReceive(Context context, Intent intent)
                {
                    updateNotificationCount();
                }
            },
            new IntentFilter(NotificationHandler.PREFS_NAME)
        );

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("button clicked");
                //view.setText(txf.getText());
                finish();
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            }
        });

        Intent mServiceIntent = new Intent(this, KATesterService.class);
        startService(mServiceIntent);

        scheduleAlarm();

        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
        System.out.println("Connected: " + isConnected);
        System.out.println("Extra: " + activeNetwork.getExtraInfo());
        System.out.println("Type: " + activeNetwork.getType() + " --> " + activeNetwork.getTypeName());
        System.out.println("Subtype: " + activeNetwork.getSubtype() + " --> " + activeNetwork.getSubtypeName());
        if (activeNetwork.isConnectedOrConnecting())
        {
            //
            // Get the type of network
            //  0 --> mobile
            //  1 --> WiFi
            //
            int networkType = activeNetwork.getType();

            if (0 == networkType)
            {
                Cursor c = getContentResolver().query(Uri.parse("content://telephony/carriers/preferapn"), null, null, null, null);

                c.moveToFirst();

                //
                //now the cursor points to the first preferred APN and we can get some information about it
                //

                String[] strAPNAspects = {
                        "name",
                        "numeric",
                        "mcc",
                        "mnc",
                        "apn",
                        "user",
                        "server",
                        "current"
                };

                int index = c.getColumnIndex("_id");    //getting index of required column
                Short id = c.getShort(index);           //getting APN's id from
                System.out.println("APN Id: " + id);

                for (String str : strAPNAspects) {
                    index = c.getColumnIndex(str);    //getting column index of required property
                    System.out.println(str + ": " + c.getString(index));
                }
            }
            else if (1 == networkType)
            {
                // type 1 means WIFI connection
                WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                System.out.println("WiFi SSID: " + wifiInfo.getSSID());
                System.out.println("WiFi BSSID: " + wifiInfo.getBSSID());
            }

        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        //
        // Retrieve interesting information from settings and trace.
        //
        SharedPreferences pref = getSharedPreferences(Constants.SETTINGS_FILE, 0);

        System.out.println("Test KA count: " + pref.getInt(Constants.TEST_KA_COUNT, -1));
        System.out.println("GCM KA count: " + pref.getInt(Constants.GCM_KA_COUNT, -1));
        System.out.println("LKG KA: " + pref.getInt(Constants.LKG_KA, -1));
        System.out.println("LKB KA: " + pref.getInt(Constants.LKB_KA, -1));
    }

    private void updateNotificationCount()
    {
        SharedPreferences settings = getSharedPreferences(Constants.SETTINGS_FILE, 0);

        for (int i = 0; i < view.length; i++)
        {
            String strType = NotificationHandler.rgStrNotificationTag[i];

            int notifCount = settings.getInt(strType, 0);
            view[i].setText(NotificationHandler.rgStrNotificationTag[i] + " " + notifCount);
        }
    }

    // Setup a recurring alarm for sending GCM KA
    public void scheduleAlarm()
    {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), GCMKAUpdater.class);

        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, 0 /* request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        // Set up an alarm for immediate trigger
        alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pIntent);
    }

}
