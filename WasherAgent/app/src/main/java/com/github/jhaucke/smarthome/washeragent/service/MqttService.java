package com.github.jhaucke.smarthome.washeragent.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;

public class MqttService extends Service {

    private static MyMqttClient client = null;
    ConnectivityChangReceiver connectivityChangReceiver;
    private Context serviceContext;
    private Handler toastHandler;

    public static MyMqttClient getClient() {
        return client;
    }

    @Override
    public void onCreate() {
        //android.os.Debug.waitForDebugger();
        serviceContext = getApplicationContext();
        connectivityChangReceiver = new ConnectivityChangReceiver();
        serviceContext.registerReceiver(connectivityChangReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        toastHandler = new Handler(Looper.getMainLooper());
        toastHandler.post(new ToastRunnable("MqttService started"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        client = new MyMqttClient(serviceContext, intent.getStringExtra("BrokerHost"));
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
        serviceContext.unregisterReceiver(connectivityChangReceiver);
        toastHandler.post(new ToastRunnable("MqttService stopped"));
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
