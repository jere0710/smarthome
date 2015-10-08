package com.github.jhaucke.smarthome.washeragent.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;

import com.github.jhaucke.smarthome.washeragent.Constants;
import com.github.jhaucke.smarthome.washeragent.R;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;

public class MyMqttClient {

    private static String brokerHost;
    MqttAsyncClient c;
    MqttConnectOptions conOptions;
    MqttCallbackImpl callback;
    private Context serviceContext = null;
    private int retryCount = 0;
    private KeepAlivePingSender pingSender;
    private WakeLock wakelock;
    private boolean isManualClosed;

    public MyMqttClient(Context serviceContext, String brokerHost) {
        super();

        isManualClosed = false;
        this.serviceContext = serviceContext;
        this.brokerHost = brokerHost;
        createClient();
    }

    private void createClient() {
        try {
            pingSender = new KeepAlivePingSender(serviceContext);
            c = new MqttAsyncClient("tcp://" + brokerHost + ":1883", getUniqueDeviceId(), new MemoryPersistence(), pingSender);
            callback = new MqttCallbackImpl();
            c.setCallback(callback);
            conOptions = new MqttConnectOptions();
            conOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            conOptions.setKeepAliveInterval(1200);
            conOptions.setCleanSession(false);
            connect();
        } catch (MqttException e) {
            createNotification("createClient ERROR");
        }
    }

    private String getUniqueDeviceId() {
        final TelephonyManager tm = (TelephonyManager) serviceContext.getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + Settings.Secure.getString(serviceContext.getContentResolver(), Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        return deviceUuid.toString();
    }

    public synchronized void connect() {
        if (!c.isConnected() && isConnectedToInternet()) {
            //acquireWakeLock();
            try {
                c.connect(conOptions, serviceContext, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken iMqttToken) {
                        try {
                            c.subscribe("smarthome/#", 2);
                            createNotification("connected");
                            retryCount = 0;
                        } catch (MqttException e) {
                            createNotification("subscribe ERROR");
                        } finally {
                            //releaseWakeLock();
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                        if (pause(retryCount++)) {
                            //releaseWakeLock();
                            connect();
                        }
                    }
                });
            } catch (MqttException e) {
                if (e.getReasonCode() != 32110) {
                    createNotification("connect ERROR");
                }
            }
        }
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager cm =
                (ConnectivityManager) serviceContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    private boolean pause(int retryCount) {
        try {
            if (retryCount < 5) {
                Thread.sleep(1000);
                return true;
            } else {
                createNotification("failed to connect - retry in one minute!");
                scheduleReconnectAlarm();
                return false;
            }
        } catch (InterruptedException e) {
            // we do nothing
        } finally {
            return true;
        }
    }

    private void scheduleReconnectAlarm() {
        AlarmManager alarmMgr = (AlarmManager) serviceContext.getSystemService(Service.ALARM_SERVICE);
        Intent intent = new Intent(Constants.RECONNECT_ALARM_ACTION);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(serviceContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000, alarmIntent);
    }

    private void createNotification(String message) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(serviceContext)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("My little smarthome")
                        .setContentText(message)
                        .setLights(Color.YELLOW, 1000, 2000);

        NotificationManager mNotifyMgr =
                (NotificationManager) serviceContext.getSystemService(serviceContext.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(UUID.randomUUID().hashCode(), mBuilder.build());
    }

    private void createNotificationWithSound(String message) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] pattern = {500, 500, 500, 500, 500, 500, 500};
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(serviceContext)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("My little smarthome")
                        .setContentText(message)
                        .setSound(alarmSound)
                        .setVibrate(pattern)
                        .setLights(Color.YELLOW, 1000, 2000);

        NotificationManager mNotifyMgr =
                (NotificationManager) serviceContext.getSystemService(serviceContext.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(UUID.randomUUID().hashCode(), mBuilder.build());
    }

    public void closeConnection() {
        try {
            isManualClosed = true;
            c.disconnectForcibly();
            c.close();
        } catch (MqttException e) {
            createNotification("closeConnection ERROR");
        }
    }

//    /**
//     * Acquires a partial wake lock for this client
//     */
//    private void acquireWakeLock() {
//        if (wakelock == null) {
//            PowerManager pm = (PowerManager) serviceContext
//                    .getSystemService(Service.POWER_SERVICE);
//            wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
//                    Constants.WAKELOCK_CONNECT);
//        }
//        wakelock.acquire();
//
//    }
//
//    /**
//     * Releases the currently held wake lock for this client
//     */
//    private void releaseWakeLock() {
//        if (wakelock != null && wakelock.isHeld()) {
//            wakelock.release();
//        }
//    }

    private class MqttCallbackImpl implements MqttCallback {

        @Override
        public void connectionLost(Throwable cause) {
            //android.os.Debug.waitForDebugger();
            //createNotification("connection lost");
            if (!isManualClosed) {
                connect();
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
        }

        @Override
        public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
            createNotificationWithSound(new String(arg1.getPayload()));
            pingSender.schedule(Long.MIN_VALUE);
        }
    }
}
