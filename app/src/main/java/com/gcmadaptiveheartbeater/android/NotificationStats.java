package com.gcmadaptiveheartbeater.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

/**
 * Created by mrahman on 31-Oct-16.
 */

public class NotificationStats extends Fragment {
    //
    // Note: The categories names here must be exactly same as used
    // in the NotificationPusher component.
    //
    final String[] _rgStrCategory = { "Messenger", "Mail", "Social", "Calendar" };

    String[] _rgStrNoticationInfo = _rgStrCategory.clone();
    private ListView _notificationList;
    ArrayAdapter _notificationListAdapter;
    NotificationBroadcastReceiver _notificationReceiver;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = (LinearLayout)inflater.inflate(R.layout.fragment_listview, container, false);

        _notificationListAdapter = new ArrayAdapter<String>(getContext(), R.layout.listview_item, _rgStrNoticationInfo);
        _notificationList = (ListView) view.findViewById(R.id.listview);
        _notificationList.setAdapter(_notificationListAdapter);

        _notificationReceiver = new NotificationBroadcastReceiver(this);


        return view;
    }

    public void onResume()
    {
        super.onResume();

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
            _notificationReceiver,
            new IntentFilter(Constants.SETTINGS_UPDATED_INTENT)
            );

        updateNotificationCount();
    }

    public void onStop()
    {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(_notificationReceiver);
    }

    void updateNotificationCount()
    {
        SharedPreferences settings = getContext().getSharedPreferences(Constants.SETTINGS_FILE, 0);
        for (int i = 0; i < _rgStrCategory.length; i++)
        {
            int notifCount = settings.getInt(_rgStrCategory[i], 0);
            _rgStrNoticationInfo[i] = _rgStrCategory[i] + " (" + notifCount + ")";
        }
        _notificationListAdapter.notifyDataSetChanged();
    }
}

class NotificationBroadcastReceiver extends BroadcastReceiver
{
    NotificationStats _notificationStats;

    NotificationBroadcastReceiver(NotificationStats notificationStats)
    {
        _notificationStats = notificationStats;
    }

    public void onReceive(Context context, Intent intent)
    {
        _notificationStats.updateNotificationCount();
    }
}


