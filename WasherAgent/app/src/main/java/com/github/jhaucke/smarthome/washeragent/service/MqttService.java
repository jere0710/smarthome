package com.github.jhaucke.smarthome.washeragent.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.github.jhaucke.smarthome.washeragent.Constants;

public class MqttService extends Service {

    private static MyMqttClient client = null;
    ReconnectAlarmReceiver reconnectAlarmReceiver;
    ConnectivityChangReceiver connectivityChangReceiver;
    private Context serviceContext;
    private Handler toastHandler;
    private WifiManager.WifiLock wifiLock = null;

    public static MyMqttClient getClient() {
        return client;
    }

    @Override
    public void onCreate() {
        //android.os.Debug.waitForDebugger();
        serviceContext = getApplicationContext();
        if (wifiLock == null) {
            WifiManager wm = (WifiManager) serviceContext.getSystemService(Context.WIFI_SERVICE);
            wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, Constants.WIFI_LOCK_TAG_MQTT_CLIENT);
        }

        if (!wifiLock.isHeld()) {
            wifiLock.acquire();
        }
        registerReceiver();

        toastHandler = new Handler(Looper.getMainLooper());
        LogWriter.appendLog("MqttService started");
        toastHandler.post(new ToastRunnable("MqttService started"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String brokerHost = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE).getString(Constants.KEY_BROKER_HOST, "");
        client = new MyMqttClient(serviceContext, brokerHost);
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        client.closeConnection();
        unregisterReceiver();
        // release the WifiLock
        if (wifiLock != null) {
            if (wifiLock.isHeld()) {
                wifiLock.release();
            }
        }
        cancelNotification();
        LogWriter.appendLog("MqttService stopped");
        toastHandler.post(new ToastRunnable("MqttService stopped"));
    }

    private void cancelNotification() {
        NotificationManager notifyMgr = (NotificationManager) serviceContext.getSystemService(serviceContext.NOTIFICATION_SERVICE);
        notifyMgr.cancel(Constants.NOTIFICATION_ID);
    }

    private void registerReceiver() {
        reconnectAlarmReceiver = new ReconnectAlarmReceiver();
        serviceContext.registerReceiver(reconnectAlarmReceiver, new IntentFilter(Constants.RECONNECT_ALARM_ACTION));

        connectivityChangReceiver = new ConnectivityChangReceiver();
        serviceContext.registerReceiver(connectivityChangReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void unregisterReceiver() {
        serviceContext.unregisterReceiver(reconnectAlarmReceiver);
        AlarmManager alarmMgr = (AlarmManager) serviceContext.getSystemService(Service.ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(serviceContext, 0, new Intent(Constants.RECONNECT_ALARM_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.cancel(alarmIntent);

        serviceContext.unregisterReceiver(connectivityChangReceiver);
    }

    private class ToastRunnable implements Runnable {
        String message;

        public ToastRunnable(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
