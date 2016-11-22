package com.gcmadaptiveheartbeater.android.BackGroundServices;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gcmadaptiveheartbeater.android.Constants;
import com.gcmadaptiveheartbeater.android.SettingsUtil;

import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;

class KADataReadHandler implements Runnable
{
    Context _context;
    Thread _thread;
    BufferedReader _input;
    ArrayBlockingQueue<String> _queuePingResponse;

    KADataReadHandler(Context context, ArrayBlockingQueue<String> queuePingResponse, BufferedReader reader)
    {
        _context = context;
        _queuePingResponse = queuePingResponse;
        _input = reader;

        _thread = new Thread(this, "KADataReadHandler");
        _thread.start();
    }
    
    @Override public void run()
    {
        String strMessage;

        try
        {
            while(true)
            {
                strMessage = _input.readLine();
                if (strMessage == null)
                {
                    break;
                }
                Log("recv> " + strMessage);

                if (strMessage.startsWith("PING ")) {
                    _queuePingResponse.put(strMessage);
                }
                else if (strMessage.startsWith("NTFN ")) {
                    processNotification(strMessage);
                }
            }
        }
        catch(IOException | InterruptedException e)
        {
            Log(_thread.getName() + ">" + e.toString());
        }

        finally {
            try {
                _input.close();
            }
            catch (IOException e)
            {
                Log(_thread.getName() + ">" + e.toString());
            }
        }
    }

    private void processNotification(String strMsg)
    {
        try {
            String[] strTokens = strMsg.split(" ");
            if (!strTokens[1].isEmpty())
                SettingsUtil.incrementSetting(_context, strTokens[1]);
        }
        catch (Exception e)
        {
            Log(_thread.getName() + ">" + e.toString());
        }
    }
    
    private void Log(Object objToLog) { Log.i("DataConn", objToLog.toString()); }
}