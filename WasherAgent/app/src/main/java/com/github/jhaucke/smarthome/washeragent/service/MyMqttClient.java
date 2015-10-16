package com.github.jhaucke.smarthome.washeragent.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.github.jhaucke.smarthome.washeragent.Constants;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MyMqttClient {

    private static String brokerHost;
    MqttAsyncClient c;
    MqttConnectOptions conOptions;
    MqttCallbackImpl callback;
    private Context serviceContext = null;
    private int retryCount = 0;
    private KeepAlivePingSender pingSender;
    private boolean isManualClosed;
    private PowerManager.WakeLock wakelock = null;

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
            LogWriter.appendLog("UniqueDeviceId: " + getUniqueDeviceId());
            c = new MqttAsyncClient("tcp://" + brokerHost + ":1883", getUniqueDeviceId(), new MemoryPersistence(), pingSender);
            callback = new MqttCallbackImpl();
            c.setCallback(callback);
            conOptions = new MqttConnectOptions();
            conOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            conOptions.setKeepAliveInterval(Constants.KEEP_ALIVE_INTERVAL);
            conOptions.setCleanSession(false);
            connect();
        } catch (MqttException e) {
            LogWriter.appendLog("createClient ERROR");
        }
    }

    private String getUniqueDeviceId() {
        final TelephonyManager tm = (TelephonyManager) serviceContext.getSystemService(Context.TELEPHONY_SERVICE);
        String androidId = Settings.Secure.getString(serviceContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        return String.valueOf(androidId.hashCode());
    }

    public synchronized void connect() {
        LogWriter.appendLog("do connect");
        if (!c.isConnected() && isConnectedToInternet()) {
            LogWriter.appendLog("try connecting...");
            try {
                c.connect(conOptions, serviceContext, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken iMqttToken) {
                        try {
                            c.subscribe("smarthome/#", 2);
                            LogWriter.appendLog("connected");
                            retryCount = 0;
                        } catch (MqttException e) {
                            LogWriter.appendLog("subscribe ERROR");
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                        if (pause(retryCount++)) {
                            LogWriter.appendLog("connect failed - retry " + retryCount);
                            connect();
                        }
                    }
                });
            } catch (MqttException e) {
                if (e.getReasonCode() != 32110) {
                    LogWriter.appendLog("connect ERROR");
                }
            }
        }
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) serviceContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private boolean pause(int retryCount) {
        try {
            if (retryCount < 3) {
                Thread.sleep(1000);
                return true;
            } else {
                LogWriter.appendLog("failed to connect - retry in one minute!");
                scheduleReconnectAlarm();
                return false;
            }
        } catch (InterruptedException e) {
            return true;
        }
    }

    private void scheduleReconnectAlarm() {
        acquireWakeLock();
        retryCount = 0;
        LogWriter.appendLog("schedule Reconnect Alarm");
        AlarmManager alarmMgr = (AlarmManager) serviceContext.getSystemService(Service.ALARM_SERVICE);
        Intent intent = new Intent(Constants.RECONNECT_ALARM_ACTION);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(serviceContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000, alarmIntent);
        releaseWakeLock();
    }

    public void closeConnection() {
        try {
            isManualClosed = true;
            c.disconnectForcibly();
            c.close();
        } catch (MqttException e) {
            LogWriter.appendLog("closeConnection ERROR");
        }
    }

    /**
     * Acquires a partial wake lock for the client
     */
    private void acquireWakeLock() {
        if (wakelock == null) {
            PowerManager pm = (PowerManager) serviceContext.getSystemService(Service.POWER_SERVICE);
            wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.WAKE_LOCK_TAG_MQTT_CLIENT);
        }
        wakelock.acquire();

    }

    /**
     * Releases the currently held wake lock for the client
     */
    private void releaseWakeLock() {
        if (wakelock != null && wakelock.isHeld()) {
            wakelock.release();
        }
    }

    private class MqttCallbackImpl implements MqttCallback {

        @Override
        public void connectionLost(Throwable cause) {
            LogWriter.appendLog("connection lost");
            if (!isManualClosed) {
                connect();
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
        }

        @Override
        public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
            acquireWakeLock();
            pingSender.schedule(Long.MIN_VALUE);
            LogWriter.appendLog(new String(arg1.getPayload()));
            NotificationHelper.createNotificationWithSound(serviceContext, new String(arg1.getPayload()));
            releaseWakeLock();
        }
    }
}
