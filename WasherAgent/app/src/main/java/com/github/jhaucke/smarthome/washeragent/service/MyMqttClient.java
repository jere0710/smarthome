package com.github.jhaucke.smarthome.washeragent.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.provider.Settings;

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

    private MqttAsyncClient c;
    private MqttConnectOptions conOptions;
    private MqttCallbackImpl callback;
    private Context serviceContext = null;
    private int retryCount = 0;
    private KeepAlivePingSender pingSender;
    private boolean isManualClosed = false;
    private PowerManager.WakeLock wakelock = null;
    private NotificationHelper notificationHelper = null;
    private boolean isConnectedToBrokerLAN;
    private boolean currentClientIsForBrokerLAN;

    public MyMqttClient(Context serviceContext) {
        super();

        this.serviceContext = serviceContext;
        createConnectOptions();
        connect();
        notificationHelper = new NotificationHelper(serviceContext);
    }

    public synchronized void connect() {
        if (isConnectedToInternet()) {
            createClientIfNeeded();
            LogWriter.appendLog("do connect");
            if (!c.isConnected()) {
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
    }

    private void createClientIfNeeded() {
        String keyBrokerHost = null;
        if (((isConnectedToBrokerLAN && currentClientIsForBrokerLAN)
                || (!isConnectedToBrokerLAN && !currentClientIsForBrokerLAN)) && c != null) {
            return;
        }
        if (isConnectedToBrokerLAN && !currentClientIsForBrokerLAN) {
            LogWriter.appendLog("KEY_BROKER_HOST_LAN");
            keyBrokerHost = Constants.KEY_BROKER_HOST_LAN;
            currentClientIsForBrokerLAN = true;
        } else {
            LogWriter.appendLog("KEY_BROKER_HOST");
            keyBrokerHost = Constants.KEY_BROKER_HOST;
            currentClientIsForBrokerLAN = false;
        }
        try {
            pingSender = new KeepAlivePingSender(serviceContext);
            LogWriter.appendLog("UniqueDeviceId: " + getUniqueDeviceId());
            String brokerHost = serviceContext.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, serviceContext.MODE_PRIVATE).getString(keyBrokerHost, "");
            c = new MqttAsyncClient("tcp://" + brokerHost + ":1883", getUniqueDeviceId(), new MemoryPersistence(), pingSender);
            callback = new MqttCallbackImpl();
            c.setCallback(callback);
        } catch (MqttException e) {
            LogWriter.appendLog("createClient ERROR");
        }
    }

    private void createConnectOptions() {
        conOptions = new MqttConnectOptions();
        conOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        conOptions.setKeepAliveInterval(Constants.KEEP_ALIVE_INTERVAL);
        conOptions.setCleanSession(false);
    }

    private String getUniqueDeviceId() {
        String androidId = Settings.Secure.getString(serviceContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        return String.valueOf(androidId.hashCode());
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) serviceContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            checkIfConnectedToBrockerLAN(cm);
            return true;
        }
        return false;
    }

    private void checkIfConnectedToBrockerLAN(ConnectivityManager cm) {
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (wifi.isConnectedOrConnecting()) {
            final WifiManager wifiManager = (WifiManager) serviceContext.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null) {
                if (connectionInfo.getSSID().equals("\"WLAN Router 135\"")) {
                    isConnectedToBrokerLAN = true;
                } else {
                    isConnectedToBrokerLAN = false;
                }
            }
        } else {
            isConnectedToBrokerLAN = false;
        }
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
        public void deliveryComplete(IMqttDeliveryToken token) {
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            acquireWakeLock();
            pingSender.schedule(Long.MIN_VALUE);
            String msgText = new String(message.getPayload());
            LogWriter.appendLog(msgText);
            notificationHelper.createNotification(msgText);
            releaseWakeLock();
        }
    }
}
