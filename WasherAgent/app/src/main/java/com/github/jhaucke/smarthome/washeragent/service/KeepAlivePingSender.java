package com.github.jhaucke.smarthome.washeragent.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.github.jhaucke.smarthome.washeragent.R;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttPingSender;
import org.eclipse.paho.client.mqttv3.internal.ClientComms;

import java.util.UUID;

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
        createNotification("construct");
    }

    @Override
    public void init(ClientComms clientComms) {
        createNotification("init");
        if (clientComms == null) {
            throw new IllegalArgumentException("ClientComms cannot be null.");
        }
        this.clientComms = clientComms;
        alarmReceiver = new AlarmReceiver();

        action = "KeepAlivePingSender.ClientID." + clientComms.getClient().getClientId();

        alarmMgr = (AlarmManager) serviceContext.getSystemService(Service.ALARM_SERVICE);
        Intent intent = new Intent(action);
        alarmIntent = PendingIntent.getBroadcast(serviceContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void start() {
        createNotification("start");
        serviceContext.registerReceiver(alarmReceiver, new IntentFilter(action));
        schedule(0);
        hasStarted = true;
    }

    @Override
    public void stop() {
        createNotification("stop");
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
        delayInMilliseconds = clientComms.getKeepAlive();
        long nextAlarmInMilliseconds = System.currentTimeMillis()
                + delayInMilliseconds;
        alarmMgr.set(AlarmManager.RTC_WAKEUP, nextAlarmInMilliseconds,
                alarmIntent);
        createNotification("schedule " + delayInMilliseconds);
    }

    private void createNotification(String message) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(serviceContext)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("My little smarthome")
                        .setContentText(message)
                        .setLights(Color.MAGENTA, 1000, 2000);

        NotificationManager mNotifyMgr =
                (NotificationManager) serviceContext.getSystemService(serviceContext.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(UUID.randomUUID().hashCode(), mBuilder.build());
    }

    class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            createNotification("onReceive");
            IMqttToken token = clientComms.checkForActivity();
            if (token != null) {
                createNotification("ping transmitted");
            }
        }
    }
}
