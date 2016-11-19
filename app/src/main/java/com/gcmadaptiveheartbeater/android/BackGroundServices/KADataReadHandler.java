package com.gcmadaptiveheartbeater.android.BackGroundServices;

import android.content.Context;
import android.util.Log;

import com.gcmadaptiveheartbeater.android.SettingsUtil;

import java.io.*;

class KADataReadHandler implements Runnable
{
    Context _context;
    Thread _thread;
    BufferedReader _input;

    KADataReadHandler(Context context, BufferedReader reader)
    {
        _context = context;
        _input = reader;

        _thread = new Thread(this);
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

                processMessage(strMessage);
            }
        }
        catch(IOException e)
        {
            Log(e);
        }

        finally {
            try {
                _input.close();
            }
            catch (IOException e)
            {
                Log(e);
            }
        }
    }

    private boolean processMessage(String strMsg)
    {
        String[] strTokens = strMsg.split(" ");

        if (strTokens.length < 2)
            return false;

        if (false == strTokens[0].equalsIgnoreCase("NTFN"))
            return false;

        if (strTokens[1].isEmpty())
            return false;

        SettingsUtil.incrementSetting(_context, strTokens[1]);

        return true;
    }
    
    private void Log(Object objToLog) { Log.i("DataConn", objToLog.toString()); }
}