package com.github.jhaucke.smarthome.washeragent.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogWriter {
    public static void appendLog(String text)
    {
        File logFile = new File("sdcard/WasherAgent.log");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        //BufferedWriter for performance, true to set append to file flag
        BufferedWriter buf = null;
        try
        {
            buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date()) + " - " + text);
            buf.newLine();
            buf.flush();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
