package com.github.jhaucke.smarthome.washeragent.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 * Created by jeremias.haucke on 09.09.2015.
 */
public class MqttService extends Service {

    private Handler toastHandler;

    @Override
    public void onCreate() {
        //android.os.Debug.waitForDebugger();
        toastHandler = new Handler(Looper.getMainLooper());
        toastHandler.post(new ToastRunnable("MqttService started"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MqttAndroidClient.startInstance(getApplicationContext());
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
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
