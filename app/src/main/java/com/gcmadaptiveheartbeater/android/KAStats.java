package com.gcmadaptiveheartbeater.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.gcmadaptiveheartbeater.android.BackGroundServices.NotificationHandler;

public class KAStats extends Fragment {
    final String[] _rgStrKAInfoLabel = {
            "LKG KA Interval",
            "LKB KA Interval",
            "Convergence Time",
            "Test KA Count",
            "GCM KA Count",
            "Connected",
            "Type",
            "Subtype",
            "APN/SSID",
            "MNC/BSSID",
            "MCC"
    };

    String[] _rgStrKAInfo = _rgStrKAInfoLabel.clone();
    private ListView _kaInfoList;
    ArrayAdapter _kaInfoListAdapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = (LinearLayout)inflater.inflate(R.layout.fragment_listview, container, false);

        _kaInfoListAdapter = new ArrayAdapter<String>(getContext(), R.layout.listview_item, _rgStrKAInfo);
        _kaInfoList = (ListView) view.findViewById(R.id.listview);
        _kaInfoList.setAdapter(_kaInfoListAdapter);

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
                Constants.CT_KA,
                Constants.TEST_KA_COUNT,
                Constants.GCM_KA_COUNT
        };

        SharedPreferences settings = getContext().getSharedPreferences(Constants.SETTINGS_FILE, 0);

        //for (int i = 0; i < _rgStrKAInfo.length; i++)
        int i;
        for (i = 0; i < 5; i++)
        {
            int value = settings.getInt(strTag[i], -1);
            _rgStrKAInfo[i] = _rgStrKAInfoLabel[i] + " (" + value + ")";
        }

        ConnectivityManager cm = (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
        _rgStrKAInfo[i] = _rgStrKAInfoLabel[i] + " (" + isConnected + ")";
        i++;

        _rgStrKAInfo[i] = _rgStrKAInfoLabel[i] + " (" + activeNetwork.getTypeName() + ")";
        i++;

        _rgStrKAInfo[i] = _rgStrKAInfoLabel[i] + " (" + activeNetwork.getSubtypeName() + ")";
        i++;

        if (activeNetwork.isConnectedOrConnecting()) {
            //
            // Get the type of network
            //  0 --> mobile
            //  1 --> WiFi
            //
            int networkType = activeNetwork.getType();

            if (0 == networkType) {
                Cursor c = getContext().getContentResolver().query(Uri.parse("content://telephony/carriers/preferapn"), null, null, null, null);

                c.moveToFirst();

                _rgStrKAInfo[i] = _rgStrKAInfoLabel[i] + " (" + c.getString(c.getColumnIndex("apn")) + ")";
                i++;

                _rgStrKAInfo[i] = _rgStrKAInfoLabel[i] + " (" + c.getString(c.getColumnIndex("mnc")) + ")";
                i++;

                _rgStrKAInfo[i] = _rgStrKAInfoLabel[i] + " (" + c.getString(c.getColumnIndex("mcc")) + ")";
                i++;
            } else if (1 == networkType) {
                // type 1 means WIFI connection
                WifiManager wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                _rgStrKAInfo[i] = _rgStrKAInfoLabel[i] + " (" + wifiInfo.getSSID() + ")";
                i++;

                _rgStrKAInfo[i] = _rgStrKAInfoLabel[i] + " (" + wifiInfo.getBSSID() + ")";
                i++;
            }
        }

        _kaInfoListAdapter.notifyDataSetChanged();
    }
}
