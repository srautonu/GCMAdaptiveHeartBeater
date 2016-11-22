package com.gcmadaptiveheartbeater.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.gcmadaptiveheartbeater.android.BackGroundServices.KADataService;
import com.gcmadaptiveheartbeater.android.BackGroundServices.KATesterService;


/**
 * Created by mrahman on 01-Nov-16.
 */

public class Commands extends Fragment {
    TextView _deviceId;
    Button _expToggleBtn;
    int _expTogglerState;
    Spinner _deviceIdSpinner;
    Spinner _expModelSpinner;
    Spinner _heartBeatRateSpinner;

    String[] _strExpToggler = {
        "Start Experiment",
        "End Experiment"
    };

    private void setupMocks()
    {
        SharedPreferences.Editor editor = getContext().getSharedPreferences(Constants.SETTINGS_FILE, 0).edit();

        editor.putInt(Constants.DATA_KA, 6);
        editor.commit();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = (RelativeLayout) inflater.inflate(R.layout.fragment_commands, container, false);

        _deviceIdSpinner = (Spinner) view.findViewById(R.id.deviceIdSpinner);
        _expModelSpinner = (Spinner) view.findViewById(R.id.expModelSpinner);
        _heartBeatRateSpinner = (Spinner) view.findViewById(R.id.heartBeatRateSpinner);

        _expToggleBtn = (Button) view.findViewById(R.id.expToggleBtn);

        if (SettingsUtil.isExperimentRunning(getContext()))
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
                    _expTogglerState = (_expTogglerState + 1) % 2;
                    _expToggleBtn.setText(_strExpToggler[_expTogglerState]);

                    if (SettingsUtil.isExperimentRunning(getContext()))
                    {
                        //
                        // If experiment is already running, a click performs stop action
                        //
                        if (SettingsUtil.getExpModel(getContext()) == Constants.EXP_MODEL_ADAPTIVE)
                        {
                            stopTestKA();
                        }

                        stopDataKA();

                        SettingsUtil.setExperimentRunning(getContext(), false);
                    }
                    else // start experiment
                    {
                        //
                        // Clear all cached settings
                        //
                        getContext().getSharedPreferences(Constants.SETTINGS_FILE, 0).edit().clear().commit();

                        // broadcast the trigger to update all the tabs with settings info
                        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(
                                new Intent(Constants.ACTION_HANDLE_SETTINGS_UPDATE)
                            );

                        //
                        // Mark experiment running
                        //
                        SettingsUtil.setExperimentRunning(getContext(), true);

                        SettingsUtil.putDeviceId(getContext(), _deviceIdSpinner.getSelectedItem().toString());

                        int expModel = 1 + (int)_expModelSpinner.getSelectedItemId();
                        SettingsUtil.putExpModel(getContext(), expModel);
                        if (expModel == Constants.EXP_MODEL_FIXED_RATE)
                        {
                            SettingsUtil.updateSetting(
                                getContext(),
                                Constants.DATA_KA,
                                Integer.parseInt(_heartBeatRateSpinner.getSelectedItem().toString())
                                );
                        }

                        //setupMocks();


                        startDataKA();
                        if (expModel == Constants.EXP_MODEL_ADAPTIVE)
                        {
                            startTestKA();
                        }
                  }
                }
            }
        );

        return view;
    }

    // request a DATA KA immediately
    private void startDataKA()
    {
        AlarmManager alarm = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        // Set up an alarm for immediate trigger
        alarm.set(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            PendingIntent.getBroadcast(getContext(), 0, new Intent(Constants.ACTION_SEND_DATA_KA), PendingIntent.FLAG_UPDATE_CURRENT)
        );
    }

    private void stopDataKA()
    {
        AlarmManager alarm = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent mServiceIntent = new Intent(getContext(), KADataService.class);

        Intent intentDataKA = new Intent(Constants.ACTION_SEND_DATA_KA);
        PendingIntent piDataKA = PendingIntent.getBroadcast(getContext(), 0, intentDataKA, PendingIntent.FLAG_UPDATE_CURRENT);

        alarm.cancel(piDataKA);

        getContext().stopService(mServiceIntent);
    }

    private void startTestKA()
    {
        AlarmManager alarm = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        Intent intentStartKA = new Intent(Constants.ACTION_START_KA_TESTING);
        PendingIntent piStartKA = PendingIntent.getBroadcast(getContext(), 0, intentStartKA, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set up an alarm for immediate trigger
        alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), piStartKA);
    }

    private void stopTestKA()
    {
        AlarmManager alarm = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        Intent intentSendKA = new Intent(Constants.ACTION_SEND_TEST_KA);
        Intent intentStartKA = new Intent(Constants.ACTION_START_KA_TESTING);
        Intent mServiceIntent = new Intent(getContext(), KATesterService.class);

        PendingIntent piSendKA = PendingIntent.getBroadcast(getContext(), 0, intentSendKA, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent piStartKA = PendingIntent.getBroadcast(getContext(), 0, intentStartKA, PendingIntent.FLAG_UPDATE_CURRENT);

        alarm.cancel(piSendKA);
        alarm.cancel(piStartKA);

        getContext().stopService(mServiceIntent);
    }
}

