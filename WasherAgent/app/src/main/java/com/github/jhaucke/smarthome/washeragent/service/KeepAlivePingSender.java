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
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;

import com.github.jhaucke.smarthome.washeragent.Constants;
import com.github.jhaucke.smarthome.washeragent.R;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
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
    private WakeLock wakelock;

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

        action = "KeepAlivePingSender.ClientID." + clientComms.getClient().getClientId();

        alarmMgr = (AlarmManager) serviceContext.getSystemService(Service.ALARM_SERVICE);
        Intent intent = new Intent(action);
        alarmIntent = PendingIntent.getBroadcast(serviceContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void start() {
        createNotification("start");
        serviceContext.registerReceiver(alarmReceiver, new IntentFilter(action));
        schedule(Long.MIN_VALUE);
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
        if (delayInMilliseconds == Long.MIN_VALUE) {
            delayInMilliseconds = clientComms.getKeepAlive();
        }
        long nextAlarmInMilliseconds = System.currentTimeMillis()
                + delayInMilliseconds;
        alarmMgr.set(AlarmManager.RTC_WAKEUP, nextAlarmInMilliseconds,
                alarmIntent);
        //createNotification("schedule " + delayInMilliseconds);
    }

    private void createNotification(String message) {
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
            // Assign new callback to token to execute code after PingResq
            // arrives. Get another wakelock even receiver already has one,
            // release it until ping response returns.
            acquireWakeLock();

            IMqttToken token = clientComms.checkForActivity();
//            if (token != null) {
//                //createNotification("ping transmitted");
//            } else {
//                //createNotification("ping NOT transmitted");
//            }

            // No ping has been sent.
            if (token == null) {
                releaseWakeLock();
                return;
            }

            token.setActionCallback(new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //Release wakelock when it is done.
                    releaseWakeLock();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    //Release wakelock when it is done.
                    releaseWakeLock();
                }
            });
        }

    }

    /**
     * Acquires a partial wake lock for this client
     */
    private void acquireWakeLock() {
        if (wakelock == null) {
            PowerManager pm = (PowerManager) serviceContext
                    .getSystemService(Service.POWER_SERVICE);
            wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    Constants.WAKELOCK_KEEP_ALIVE_PING);
        }
        wakelock.acquire();

    }

    /**
     * Releases the currently held wake lock for this client
     */
    private void releaseWakeLock() {
        if (wakelock != null && wakelock.isHeld()) {
            wakelock.release();
        }
    }
}
