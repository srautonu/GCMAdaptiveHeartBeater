package com.research.ton.newandroidapp.BackGroundServices;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

import com.research.ton.newandroidapp.Constants;
import com.research.ton.newandroidapp.Utilities;

import java.io.*;
import java.net.*;

//  kopottakha.cs.uiuc.edu:8080

/**
 * Created by mrahman on 24-Oct-16.
 */
public class KATesterService extends IntentService
{
    String m_strServerDNS = "kopottakha.cs.uiuc.edu"; //"172.20.33.3" ;//"localhost";
    int m_serverPort = 8080;

    Socket m_sock;
    DataOutputStream m_outToServer;
    BufferedReader m_inFromServer;

    IKAIntervalTester m_tester;

    public KATesterService()
    {
        super("com.research.ton.BackgroundServices.KATesterService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        String strResponse;
        boolean fKASuccess;
        int lkgKA, lkbKA;

        lkgKA = getSharedPreferences(Constants.SETTINGS_FILE, 0).getInt(Constants.LKG_KA, 1 /* default lkg */);
        lkbKA = getSharedPreferences(Constants.SETTINGS_FILE, 0).getInt(Constants.LKB_KA, 33 /* default lkb */);
        Log("Read from settings: LKG_KA: " + lkgKA + " minutes, " + "LKB_KA: " + lkbKA + " minutes.");

        m_tester = new KAHybrid();
        m_tester.InitTest(lkgKA, lkbKA);

        try {
            while (false == m_tester.IsCompleted()) {
                int delay = m_tester.GetNextIntervalToTest();

                if (!IsChannelOpen()) {
                    OpenChannel();
                }

                //
                // Wait for the test KA interval time
                //
                Log("Going to sleep for " + delay + " minutes.\n");
                Thread.sleep(delay * 60 * 1000);

                //
                // Now send a ping
                //
                try {
                    //
                    // We are about to send a KA. Increment the test KA counter
                    //
                    Utilities.incrementSetting(getSharedPreferences(Constants.SETTINGS_FILE, 0), Constants.TEST_KA_COUNT);

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
                    Log(e.toString() + "\n");
                    CloseChannel();
                }

                fKASuccess = IsChannelOpen();

                //
                // Update the KA test algorithm state based on success/failure of
                // current test
                //
                m_tester.SetCurTestResult(fKASuccess);

                //
                // Get the shared settings editor to update lkg/lkb KA
                //
                SharedPreferences settings = getSharedPreferences(Constants.SETTINGS_FILE, 0);
                SharedPreferences.Editor editor = settings.edit();

                //
                // If the current delay was successful, the LKG KA interval should
                // be updated in the settings. Otherwise, update LKB KA
                //
                if (fKASuccess) {
                    editor.putInt(Constants.LKG_KA, delay);
                    Log("Updated settings with new LKG KA: " + delay);
                }
                else
                {
                    editor.putInt(Constants.LKB_KA, delay);
                    Log("Updated settings with new LKB KA: " + delay);
                }

                editor.commit();

                Log("LKG KA Interval is " + m_tester.GetLKGInterval() + " minutes.\n");
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }

        Log("Optimal KA Interval is " + m_tester.GetLKGInterval() + " minutes.\n");

        //
        // Close the socket when done.
        //
        CloseChannel();
    }

    private boolean IsChannelOpen()
    {
        return (null != m_sock);
    }

    private void OpenChannel() throws Exception
    {
        if (null == m_sock)
        {
            Log("Connecting to " + m_strServerDNS + ":" + m_serverPort + "...\n");
            m_sock = new Socket(m_strServerDNS, m_serverPort);
            //
            // Set the socket read timeout to 30 seconds.
            //
            m_sock.setSoTimeout(30 * 1000);
            m_outToServer = new DataOutputStream(m_sock.getOutputStream());
            m_inFromServer = new BufferedReader(new InputStreamReader(m_sock.getInputStream()));
            Log("Done.\n");
        }
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
                Log(e.toString() + "\n");
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