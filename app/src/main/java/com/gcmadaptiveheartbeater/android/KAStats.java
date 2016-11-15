package com.gcmadaptiveheartbeater.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

public class KAStats extends Fragment {
    final String[] _rgStrKAInfoLabel = {
        "LKG KA Interval",
        "LKB KA Interval",
        "Test KA Count",
        "GCM KA Count",
        "Test KA TS",
        "GCM KA TS",
        "Experiment start TS",
        "Experiment end TS",
        "Connected",
        "Type",
    };

    String[] _rgStrKAInfo = _rgStrKAInfoLabel.clone();
    private ListView _kaInfoList;
    ArrayAdapter _kaInfoListAdapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = (LinearLayout)inflater.inflate(R.layout.fragment_listview, container, false);

        _kaInfoListAdapter = new ArrayAdapter<String>(getContext(), R.layout.listview_item, _rgStrKAInfo);
        _kaInfoList = (ListView) view.findViewById(R.id.listview);
        _kaInfoList.setAdapter(_kaInfoListAdapter);

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                new BroadcastReceiver()
                {
                    public void onReceive(Context context, Intent intent) {
                        updateKAStats();
                    }
                },
                new IntentFilter(Constants.ACTION_HANDLE_SETTINGS_UPDATE)
        );

        return view;
    }

    public void onResume()
    {
        super.onResume();

        updateKAStats();
    }

    void updateKAStats()
    {
        //
        // The order here should be corresponding to _rgStrKAInfoLabel
        //
        final String[] strTag = {
            Constants.LKG_KA,
            Constants.LKB_KA,
            Constants.TEST_KA_COUNT,
            Constants.GCM_KA_COUNT,
            Constants.TEST_KA_TIMESTAMP,
            Constants.GCM_KA_TIMESTAMP,
            Constants.EXP_START_TIMESTAMP,
            Constants.EXP_END_TIMESTAMP
        };

        if (null == getContext())
            return;

        SharedPreferences settings = getContext().getSharedPreferences(Constants.SETTINGS_FILE, 0);

        int i;
        for (i = 0; i < strTag.length; i++)
        {
            if (i < 4)
                _rgStrKAInfo[i] = _rgStrKAInfoLabel[i] + " (" + settings.getInt(strTag[i], -1) + ")";
            else
                _rgStrKAInfo[i] = _rgStrKAInfoLabel[i] + " (" + settings.getString(strTag[i], "") + ")";
        }

        ConnectivityManager cm = (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
        _rgStrKAInfo[i] = _rgStrKAInfoLabel[i] + " (" + isConnected + ")";
        i++;

        _rgStrKAInfo[i] = _rgStrKAInfoLabel[i];
        if (isConnected)
        {
            _rgStrKAInfo[i] += " (" + activeNetwork.getTypeName() + ")";
        }

        _kaInfoListAdapter.notifyDataSetChanged();
    }
}
