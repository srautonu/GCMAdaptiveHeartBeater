package com.gcmadaptiveheartbeater.android.BackGroundServices;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.gcmadaptiveheartbeater.android.Constants;
import com.gcmadaptiveheartbeater.android.Utilities;

import java.io.*;
import java.net.*;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

abstract class MyIntentService extends Service
{
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private String mName;

    private final class ServiceHandler extends Handler
    {
        public ServiceHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            onHandleIntent((Intent)msg.obj);
        }
    }

    public MyIntentService(String name) {
        super();
        mName = name;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread thread = new HandlerThread("MyIntentService[" + mName + "]");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStart(intent, startId);

        return START_STICKY_COMPATIBILITY ;
    }

    @Override
    public void onDestroy()
    {
        mServiceLooper.quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    protected abstract void onHandleIntent(Intent intent);
}


// kopottakha.cs.uiuc.edu:8080
// www.ekngine.com:5228

/**
 * Created by mrahman on 24-Oct-16.
 */
public class KATesterService extends MyIntentService
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
        String strAction = intent.getAction();
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
        // We ensure that all intents come from GCMKAUpdater, using startWakefulService
        //
        GCMKAUpdater.completeWakefulIntent(intent);
    }

    void ScheduleNextKA()
    {
        if (m_tester.IsCompleted())
            return;

        OpenChannel();

        int delay = m_tester.GetNextIntervalToTest();
        Log("Next KA interval to test: " + delay + " minutes.\n");

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.SETTINGS_FILE, 0);
        AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        //
        // Now we need to update the timer.
        //
        alarm.set(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + delay * 60 * 1000,
                PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(Constants.ACTION_SEND_TEST_KA), PendingIntent.FLAG_UPDATE_CURRENT)
        );
    }

    void SendAndScheduleNextKA()
    {
        String strResponse;
        boolean fKASuccess;
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
                Log("Connection reset by peer.\n");
                CloseChannel();
            }

        } catch (IOException e) {
            System.out.println(e);
            CloseChannel();
        }

        fKASuccess = IsChannelOpen();

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
            Utilities.updateSetting(this, Constants.LKG_KA, delay);
            Log("Updated settings with new LKG KA: " + delay);
        } else {
            Utilities.updateSetting(this, Constants.LKB_KA, delay);
            Log("Updated settings with new LKB KA: " + delay);
        }

        //
        // We have sent a KA (successfully or not). Increment the test KA counter
        //
        Utilities.incrementTestKACount(this);

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
            Log("Connecting to " + m_strServerDNS + ":" + m_serverPort + "...\n");

            try {
                m_sock = new Socket(m_strServerDNS, m_serverPort);
                //
                // Set the socket read timeout to 30 seconds.
                //
                m_sock.setSoTimeout(30 * 1000);
                m_outToServer = new DataOutputStream(m_sock.getOutputStream());
                m_inFromServer = new BufferedReader(new InputStreamReader(m_sock.getInputStream()));

                Log("Done.\n");
            }
            catch (IOException e)
            {
                System.out.println(e);
                fRet = false;
            }
        }

        return fRet;
    }

    private void CloseChannel()
    {
        if (null != m_sock)
        {
            Log("Closing socket ...\n");
            try
            {
                m_inFromServer.close();
                m_inFromServer = null;

                m_outToServer.close();
                m_outToServer = null;

                m_sock.close();
                m_sock = null;

                Log("Done.\n");
            }
            catch(IOException e)
            {
                System.out.println(e);
            }
        }
    }

    private void Log(String strToLog)
    {
        System.out.println("TestConn - " + strToLog);
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