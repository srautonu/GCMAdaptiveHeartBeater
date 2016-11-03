package com.gcmadaptiveheartbeater.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gcmadaptiveheartbeater.android.BackGroundServices.GCMKAUpdater;
import com.gcmadaptiveheartbeater.android.BackGroundServices.KATesterService;


/**
 * Created by mrahman on 01-Nov-16.
 */

public class Commands extends Fragment {
    TextView _deviceId;
    Button _expToggleBtn;
    int _expTogglerState;

    String[] _strExpToggler = {
        "Start Experiment",
        "End Experiment"
    };

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        SharedPreferences pref = getContext().getSharedPreferences(Constants.SETTINGS_FILE, 0);

        View view = (LinearLayout) inflater.inflate(R.layout.fragment_commands, container, false);

        _expToggleBtn = (Button) view.findViewById(R.id.expToggleBtn);
        _deviceId = (TextView) view.findViewById(R.id.deviceId);

        String strAccount = getAccount();
        _deviceId.setText(strAccount.toCharArray(), 0, strAccount.length());
        if (Utilities.isExperimentRunning(pref))
        {
            _expTogglerState = 1;
        }
        else
        {
            _expTogglerState = 0;
        }
        _expToggleBtn.setText(_strExpToggler[_expTogglerState]);

        _expToggleBtn.setOnClickListener(
            new OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences pref = getContext().getSharedPreferences(Constants.SETTINGS_FILE, 0);

                    _expTogglerState = (_expTogglerState + 1) % 2;
                    _expToggleBtn.setText(_strExpToggler[_expTogglerState]);

                    if (Utilities.isExperimentRunning(pref))
                    {
                        //
                        // If experiment is already running, a click performs stop action
                        //
                        Utilities.setExperimentRunning(pref, false);
                    }
                    else // start experiment
                    {
                        //
                        // Clear all cached settings
                        //
                        pref.edit().clear().commit();

                        //
                        // Mark experiment running
                        //
                        Utilities.setExperimentRunning(pref, true);

                        //
                        // Model #1: Android as is. No KA testing, No additional GCM KA
                        // Model #2: Agressive GCM KA (1 minute). No KA Testing
                        // Model #3: KA testing. GCM Adaptive KA
                        //

                        //
                        // Start KA interval testing
                        //
                        Intent mServiceIntent = new Intent(getContext(), KATesterService.class);
                        getContext().startService(mServiceIntent);

                        //
                        // Start GCM KA alarm
                        //
                        scheduleAlarm();
                    }
                }
            }
        );

        return view;
    }


    private String getAccount()
    {
        AccountManager accountManager = AccountManager.get(getContext());
        Account account = null;

        Account[] accounts = accountManager.getAccountsByType("com.google");
        if (accounts.length > 0) {
            account = accounts[0];
        }

        if (account == null) {
            return "";
        } else {
            return account.name;
        }
    }

    // Setup a recurring alarm for sending GCM KA
    private void scheduleAlarm()
    {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getContext(), GCMKAUpdater.class);

        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(getContext(), 0 /* request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarm = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        // Set up an alarm for immediate trigger
        alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pIntent);
    }
}

