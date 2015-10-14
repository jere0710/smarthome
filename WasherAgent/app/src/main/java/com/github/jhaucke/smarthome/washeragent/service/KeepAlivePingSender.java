package com.github.jhaucke.smarthome.washeragent.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.github.jhaucke.smarthome.washeragent.Constants;

import org.eclipse.paho.client.mqttv3.MqttPingSender;
import org.eclipse.paho.client.mqttv3.internal.ClientComms;

public class KeepAlivePingSender implements MqttPingSender {

    private Context serviceContext;
    private ClientComms clientComms;
    private AlarmReceiver alarmReceiver;
    private String action;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private volatile boolean hasStarted = false;

    public KeepAlivePingSender(Context serviceContext) {
        if (serviceContext == null) {
            throw new IllegalArgumentException("ServiceContext cannot be null.");
        }
        this.serviceContext = serviceContext;
    }

    @Override
    public void init(ClientComms clientComms) {
        if (clientComms == null) {
            throw new IllegalArgumentException("ClientComms cannot be null.");
        }
        this.clientComms = clientComms;
        alarmReceiver = new AlarmReceiver();

        action = Constants.ACTION_KEEP_ALIVE_PING_SENDER;

        alarmMgr = (AlarmManager) serviceContext.getSystemService(Service.ALARM_SERVICE);
        Intent intent = new Intent(action);
        alarmIntent = PendingIntent.getBroadcast(serviceContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void start() {
        LogWriter.appendLog("start");
        serviceContext.registerReceiver(alarmReceiver, new IntentFilter(action));
        schedule(Long.MIN_VALUE);
        hasStarted = true;
    }

    @Override
    public void stop() {
        LogWriter.appendLog("stop");
        alarmMgr.cancel(alarmIntent);
        if (hasStarted) {
            hasStarted = false;
            try {
                serviceContext.unregisterReceiver(alarmReceiver);
            } catch (IllegalArgumentException e) {
                //Ignore unregister errors.
            }
        }
    }

    @Override
    public void schedule(long delayInMilliseconds) {
        if (delayInMilliseconds == Long.MIN_VALUE) {
            delayInMilliseconds = clientComms.getKeepAlive();
        }
        long nextAlarmInMilliseconds = System.currentTimeMillis()
                + delayInMilliseconds;
        LogWriter.appendLog("next ping in " + delayInMilliseconds / 1000 + " s");
        alarmMgr.setWindow(AlarmManager.RTC_WAKEUP, nextAlarmInMilliseconds, Constants.ALARM_WINDOW_KEEP_ALIVE_PING, alarmIntent);
    }

    class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            LogWriter.appendLog("onReceive Ping");
            clientComms.checkForActivity();
        }
    }
}
