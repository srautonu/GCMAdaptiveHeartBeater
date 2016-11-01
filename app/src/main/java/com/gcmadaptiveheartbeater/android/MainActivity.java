package com.gcmadaptiveheartbeater.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.gcmadaptiveheartbeater.android.BackGroundServices.*;

import java.util.List;
import java.util.Vector;

/**
 * Created by mrahman on 31-Oct-16.
 */

public class MainActivity extends FragmentActivity {

    private PagerAdapter _pagerAdapter;

    private void setupMocks()
    {
        SharedPreferences.Editor editor = getSharedPreferences(Constants.SETTINGS_FILE, 0).edit();

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
        //setupMocks();

        setContentView(R.layout.viewpager);

        List<Fragment> fragments = new Vector<Fragment>();
        fragments.add(Fragment.instantiate(this, Commands.class.getName()));
        fragments.add(Fragment.instantiate(this, NotificationStats.class.getName()));
        fragments.add(Fragment.instantiate(this, KAStats.class.getName()));

        _pagerAdapter = new PagerAdapter(getSupportFragmentManager(), fragments);
        ViewPager pager = (ViewPager) super.findViewById(R.id.viewpager);
        pager.setAdapter(_pagerAdapter);

        //
        // Make notification info page the primary page, when we are already
        // inside an experiment
        //
        if (Utilities.isExperimentRunning(getSharedPreferences(Constants.SETTINGS_FILE, 0)))
            pager.setCurrentItem(1);
    }
}

class PagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> _fragments;

    public PagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        _fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return _fragments.get(position);
    }

    @Override
    public int getCount() {
        return _fragments.size();
    }
}
