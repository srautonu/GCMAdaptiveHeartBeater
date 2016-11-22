package com.gcmadaptiveheartbeater.android.BackGroundServices;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.gcmadaptiveheartbeater.android.Constants;
import com.gcmadaptiveheartbeater.android.NetworkUtil;
import com.gcmadaptiveheartbeater.android.SettingsUtil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by mrahman on 15-Nov-16.
 */

public class KADataService extends StickyIntentService {
    String _strServerDNS = "192.168.0.104"; //"www.ekngine.com";
    int _serverPort = 5229;

    KADataReadHandler _readHandler = null;
    Socket _sock = null;
    DataOutputStream _outToServer = null;
    BufferedReader _inFromServer = null;

    public KADataService()
    {
        super(KADataService.class.toString());
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log("BGService created.");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        CloseChannel();

        Log("BGService destroyed.");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent == null) {
            Log("null intent. Still proceeding.");
        }
        else {
            String strAction = intent.getAction();
            if (null == strAction || !strAction.equalsIgnoreCase(Constants.ACTION_SEND_DATA_KA)) {
                Log("Ignoring action: " + ((strAction == null) ? "null" : strAction));
                return;
            }
        }

        if (IsChannelOpen())
        {
            //
            // Now send a ping
            //
            try {
                _outToServer.writeBytes("PING\n");
                Log("sent> PING");
            } catch (IOException e) {
                System.out.println(e);
                CloseChannel();
                ScheduleOpenChannel();
            }
        }
        else if (!OpenChannel())
        {
            ScheduleOpenChannel();
        }

        //
        // We ensure that all intents come from SystemEventsReceiver, using startWakefulService
        //
        SystemEventsReceiver.completeWakefulIntent(intent);
    }

    private boolean IsChannelOpen()
    {
        return (null != _sock);
    }

    private boolean OpenChannel()
    {
        boolean fRet = true;

        if (!NetworkUtil.isConnected(this))
            return false;

        if (null == _sock)
        {
            Log("Connecting to " + _strServerDNS + ":" + _serverPort + "...\n");

            try {
                SocketAddress sockAddr = new InetSocketAddress(_strServerDNS, _serverPort);

                _sock = new Socket();
                _sock.connect(sockAddr, 10 * 1000); // connect within 10 seconds, else exception out

                _outToServer = new DataOutputStream(_sock.getOutputStream());
                _readHandler = new KADataReadHandler(this, new BufferedReader(new InputStreamReader(_sock.getInputStream())));
                Log("Done.\n");

                String strClntCmd = "CLNT " + SettingsUtil.getDeviceId(this);
                _outToServer.writeBytes(strClntCmd + "\n");
                Log("sent> " + strClntCmd);
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
        if (null != _sock)
        {
            Log("Closing socket ...\n");
            try
            {
                _sock.close();
                Log("Done.\n");
            }
            catch(IOException e)
            {
                Log(e);
            }
            finally {
                _sock = null;
                _inFromServer = null;
                _outToServer = null;
                _readHandler = null;
            }
        }
    }

    private void ScheduleOpenChannel()
    {
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //
        // schedule a connection attempt, one minute from now.
        //
        alarm.set(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 1 * 60 * 1000,
            PendingIntent.getBroadcast(this, 0, new Intent(Constants.ACTION_SEND_DATA_KA), PendingIntent.FLAG_UPDATE_CURRENT)
            );
    }

    private void Log(Object objToLog)
    {
        Log.i("DataConn", objToLog.toString());
    }
}

