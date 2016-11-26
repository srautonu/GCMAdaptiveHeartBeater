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
import android.widget.Adapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.gcmadaptiveheartbeater.android.BackGroundServices.KADataService;
import com.gcmadaptiveheartbeater.android.BackGroundServices.KATesterService;
import com.gcmadaptiveheartbeater.android.BackGroundServices.ServicesMgr;


/**
 * Created by mrahman on 01-Nov-16.
 */

public class Commands extends Fragment {
    TextView _deviceId;
    Button _expToggleBtn;
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
        boolean fExpRunning = SettingsUtil.isExperimentRunning(getContext());

        View view = (RelativeLayout) inflater.inflate(R.layout.fragment_commands, container, false);

        _deviceIdSpinner = (Spinner) view.findViewById(R.id.deviceIdSpinner);
        _deviceIdSpinner.setEnabled(!fExpRunning);

        _expModelSpinner = (Spinner) view.findViewById(R.id.expModelSpinner);
        _expModelSpinner.setEnabled(!fExpRunning);

        _heartBeatRateSpinner = (Spinner) view.findViewById(R.id.heartBeatRateSpinner);
        _heartBeatRateSpinner.setEnabled(!fExpRunning);

        _expToggleBtn = (Button) view.findViewById(R.id.expToggleBtn);
        _expToggleBtn.setText(_strExpToggler[SettingsUtil.isExperimentRunning(getContext()) ? 1 : 0]);

        _expToggleBtn.setOnClickListener(
            new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SettingsUtil.isExperimentRunning(getContext()))
                    {
                        // Change the text in the start/end experiment button
                        _expToggleBtn.setText(_strExpToggler[0]);

                        _deviceIdSpinner.setEnabled(true);
                        _expModelSpinner.setEnabled(true);
                        _heartBeatRateSpinner.setEnabled(true);

                        ServicesMgr.endExperiment(getContext());
                    }
                    else // start experiment
                    {
                        _expToggleBtn.setText(_strExpToggler[1]);

                        _deviceIdSpinner.setEnabled(false);
                        _expModelSpinner.setEnabled(false);
                        _heartBeatRateSpinner.setEnabled(false);

                        String strDeviceId = _deviceIdSpinner.getSelectedItem().toString();
                        int expModel = 1 + (int)_expModelSpinner.getSelectedItemId();
                        int dataKaRateM = 0;

                        if (expModel == Constants.EXP_MODEL_FIXED_RATE)
                        {
                            dataKaRateM = Integer.parseInt(_heartBeatRateSpinner.getSelectedItem().toString());
                        }

                        ServicesMgr.startExperiment(getContext(), strDeviceId, expModel, dataKaRateM);
                    }
                }
            }
        );

        return view;
    }
}

