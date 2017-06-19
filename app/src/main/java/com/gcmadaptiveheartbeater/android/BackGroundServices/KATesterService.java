//
// This file contains code for KA interval testing.
//
package com.gcmadaptiveheartbeater.android.BackGroundServices;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.gcmadaptiveheartbeater.android.Constants;
import com.gcmadaptiveheartbeater.android.NetworkUtil;
import com.gcmadaptiveheartbeater.android.SettingsUtil;

import java.io.*;
import java.net.*;

/**
 * Created by mrahman on 24-Oct-16.
 */
public class KATesterService extends StickyIntentService
{
    String m_strServerDNS = "www.ekngine.com";
    int m_serverPort = 5228;

    Socket m_sock;
    DataOutputStream m_outToServer;
    BufferedReader m_inFromServer;

    IKAIntervalTester m_tester;

    public KATesterService()
    {
        super(KATesterService.class.toString());
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log("BGService created.");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        //
        // If experiment is not running, or the KA model is anything other than
        // adaptive (Model #1), then we ignore any spurious intent
        //
        if (!SettingsUtil.isExperimentRunning(this) || Constants.EXP_MODEL_ADAPTIVE != SettingsUtil.getExpModel(this))
            return;

        String strAction = null;

        // UNKNOWN CAUSE: We are still seeing unknown intent come in if the app
        // is forcibly closed. We still need to start KA testing in this case.
        // So, manually set the intent action here:
        if (intent == null) {
            Log("Converting null intent to START_KA_TESTING action.");
            strAction = Constants.ACTION_START_KA_TESTING;
        }
        else {
            strAction = intent.getAction();
        }

        if (strAction == null)
            return;

        Log("Action: " + strAction);

        if (strAction.equalsIgnoreCase(Constants.ACTION_START_KA_TESTING))
        {
            SharedPreferences settings = getSharedPreferences(Constants.SETTINGS_FILE, 0);
            int lkgKA = settings.getInt(Constants.LKG_KA, 1 /* default lkg */);
            int lkbKA = settings.getInt(Constants.LKB_KA, 33 /* default lkb */);
            Log("Read from settings: LKG_KA: " + lkgKA + " minutes, " + "LKB_KA: " + lkbKA + " minutes.");

            m_tester = new KAHybrid();
            m_tester.InitTest(lkgKA, lkbKA);

            ScheduleNextKA();
        }
        else if (strAction.equalsIgnoreCase(Constants.ACTION_SEND_TEST_KA))
        {
            SendAndScheduleNextKA();
        }

        //
        // We ensure that all intents come from SystemEventsReceiver, using startWakefulService
        //
        SystemEventsReceiver.completeWakefulIntent(intent);
    }

    void ScheduleNextKA()
    {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.SETTINGS_FILE, 0);
        AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        int delay = 0;
        Intent intent = null;

        if (m_tester.IsCompleted() || !NetworkUtil.isConnected(this))
            return;

        if (OpenChannel()) {
            delay = m_tester.GetNextIntervalToTest();
            Log("Next KA interval to test: " + delay + " minutes.\n");

            intent = new Intent(Constants.ACTION_SEND_TEST_KA);
        }
        else {
            delay = 1; // re-attempt establishing test connection in one minute
            intent = new Intent(Constants.ACTION_START_KA_TESTING);
        }

        //
        // Now we need to update the timer.
        //
        alarm.set(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + delay * 60 * 1000,
            PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            );
    }

    void SendAndScheduleNextKA()
    {
        String strResponse;
        boolean fKASuccess;
        boolean fShouldRetry = false;
        Context context = getApplicationContext();

        if (m_tester == null || m_tester.IsCompleted() || !IsChannelOpen())
            return;

        //
        // Find out the time we have been waiting for.
        //
        int delay = m_tester.GetNextIntervalToTest();

        //
        // Now send a ping
        //
        try {
            m_outToServer.writeBytes("PING TEST\n");
            Log("sent> PING TEST\n");

            strResponse = m_inFromServer.readLine();
            if (null != strResponse) {
                Log("recv> " + strResponse + "\n");
            } else {
                //
                // end of input stream reached. That means, the other
                // side has probably closed the connection
                //
                Log("Connection reset by peer.");
                CloseChannel();
            }

        } catch (IOException e) {
            Log(e);
            CloseChannel();
        }

        fKASuccess = IsChannelOpen();
        fShouldRetry = (!fKASuccess && !NetworkUtil.isConnected(this));

        if (fShouldRetry) {
            Log("Retry clause triggered. Ignoring current test results.");
        }
        else {
            //
            // Update the KA test algorithm state based on success/failure of
            // current test
            //
            m_tester.SetCurTestResult(fKASuccess);

            //
            // If the current delay was successful, the LKG KA interval should
            // be updated in the settings. Otherwise, update LKB KA
            //
            if (fKASuccess) {
                SettingsUtil.updateSetting(this, Constants.LKG_KA, delay);
                if (SettingsUtil.getExpModel(this) == Constants.EXP_MODEL_ADAPTIVE) {
                    SettingsUtil.updateSetting(this, Constants.DATA_KA, delay);
                }
                Log("Updated settings with new LKG KA: " + delay);
            } else {
                SettingsUtil.updateSetting(this, Constants.LKB_KA, delay);
                Log("Updated settings with new LKB KA: " + delay);
            }
        }

        //
        // We have sent a KA (successfully or not). Increment the test KA counter
        //
        SettingsUtil.incrementTestKACount(this);

        Log("LKG KA Interval is " + m_tester.GetLKGInterval() + " minutes.\n");

        //
        // If the test is still not completed, find the next interval to test
        // and schedule an alarm for it
        //
        if (!m_tester.IsCompleted())
        {
            ScheduleNextKA();
        }
        else
        {
            Log("Optimal KA Interval is " + m_tester.GetLKGInterval() + " minutes.\n");

            // Close the socket, as we are done.
            CloseChannel();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        CloseChannel();

        Log("BGService destroyed.");
    }

    private boolean IsChannelOpen()
    {
        return (null != m_sock);
    }

    private boolean OpenChannel()
    {
        boolean fRet = true;

        if (null == m_sock)
        {
            Log("Connecting to " + m_strServerDNS + ":" + m_serverPort + "...");

            try {
                SocketAddress sockAddr = new InetSocketAddress(m_strServerDNS, m_serverPort);

                m_sock = new Socket();
                m_sock.connect(sockAddr, 10 * 1000); // connect within 10 seconds, else exception out

                // Set the socket read timeout to 30 seconds.
                m_sock.setSoTimeout(30 * 1000);
                m_outToServer = new DataOutputStream(m_sock.getOutputStream());
                m_inFromServer = new BufferedReader(new InputStreamReader(m_sock.getInputStream()));

                Log("Done.");
            }
            catch (IOException e)
            {
                Log(e);
                CloseChannel();
                fRet = false;
            }
        }

        return fRet;
    }

    private void CloseChannel()
    {
        if (null != m_sock)
        {
            Log("Closing socket ...");
            try
            {
                m_sock.close();
                Log("Done.");
            }
            catch(IOException e)
            {
                Log(e);
            }
            finally {
                m_sock = null;
                m_inFromServer = null;
                m_outToServer = null;
            }
        }
    }

    private void Log(Object objToLog)
    {
        Log.i("TestConn", objToLog.toString());
    }
}

interface IKAIntervalTester
{
    void InitTest(int lkg, int lkb);
    void SetCurTestResult(boolean fSucceeded);
    int GetNextIntervalToTest();
    int GetLKGInterval();
    boolean IsCompleted();
}

class KAHybrid implements IKAIntervalTester
{
    private int m_low;
    private int m_high;
    private int m_lkg;
    private int m_test;
    private boolean m_fBinaryPhase;

    @Override public void InitTest(int lkg, int lkb)
    {
        synchronized(this) {
            m_low = m_lkg = lkg;
            m_high = lkb - 1;

            //
            // If lkg is a power of 2 and doubling it still keeps us within search space,
            // then we are still in exponential phase
            //
            if (2 * lkg < lkb && Math.pow(2, (int)(Math.log(lkg)/Math.log(2))) == lkg)
            {
                m_fBinaryPhase = false;
            }
            else
            {
                m_fBinaryPhase = true;
            }
        }
    }

    @Override public int GetLKGInterval()
    {
        int retLkg;

        synchronized(this)
        {
            retLkg = m_lkg;
        }

        return retLkg;
    }

    @Override public void SetCurTestResult(boolean fSucceeded)
    {
        synchronized(this)
        {
            if (fSucceeded)
            {
                m_low = m_test;
                m_lkg = m_test;
            }
            else
            {
                m_fBinaryPhase = true;
                m_low = m_lkg;
                m_high = m_test - 1;
            }
        }
    }

    @Override public int GetNextIntervalToTest()
    {
        int retTest = 0;

        synchronized(this)
        {
            m_test = (m_fBinaryPhase ? (m_low + m_high + 1)/2 : 2 * m_lkg);
            retTest = m_test;
        }
        return retTest;
    }

    @Override public boolean IsCompleted()
    {
        boolean fCompleted = false;

        synchronized(this)
        {
            fCompleted = (m_high-m_low <= 0);
        }

        return fCompleted;
    }
}