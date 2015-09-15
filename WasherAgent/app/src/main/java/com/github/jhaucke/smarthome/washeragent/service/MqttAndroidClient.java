package com.github.jhaucke.smarthome.washeragent.service;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.github.jhaucke.smarthome.washeragent.R;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;

public class MqttAndroidClient {

    private static MqttAndroidClient instance = null;
    private Context serviceContext = null;
    private Handler toastHandler;
    private int retryCount;

    MqttAsyncClient c;
    MqttConnectOptions conOptions;
    MqttCallbackImpl callback;

    private MqttAndroidClient(Context serviceContext) {
        super();

        this.serviceContext = serviceContext;
        toastHandler = new Handler(Looper.getMainLooper());
        startClient();
    }

    public static MqttAndroidClient startInstance(Context serviceContext) {
        if (instance == null) {
            instance = new MqttAndroidClient(serviceContext);
        }
        return instance;
    }

    private void startClient() {
        try {
            c = new MqttAsyncClient("tcp://iot.eclipse.org:1883", android.os.Build.MODEL, new MemoryPersistence());
            callback = new MqttCallbackImpl();
            c.setCallback(callback);
            conOptions = new MqttConnectOptions();
            conOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            conOptions.setCleanSession(false);
            connect();
            c.subscribe("test/#", 2);
        } catch (MqttException e) {
            toastHandler.post(new ToastRunnable("starting client ERROR"));
        }
    }

    private void connect() {
        boolean tryConnecting = true;
        retryCount = 0;
        while (tryConnecting) {
            try {
                c.connect(conOptions);
            } catch (Exception e) {
                toastHandler.post(new ToastRunnable("failed to connect!"));
            }
            if (c.isConnected()) {
                toastHandler.post(new ToastRunnable("connected"));
                tryConnecting = false;
            } else {
                pause(retryCount++);
            }
        }
    }

    private void pause(int retryCount) {
        try {
            if (retryCount < 10) {
                Thread.sleep(2000); // 2 s
            } else {
                Thread.sleep(300000); // 5 min
                retryCount = 0;
            }
        } catch (InterruptedException e) {
            // Error handling goes here...
        }
    }

    private class MqttCallbackImpl implements MqttCallback {

        public void connectionLost(Throwable cause) {
            toastHandler.post(new ToastRunnable("Connection lost - attempting reconnect."));
            connect();
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            // Not needed in this simple demo
        }

        @Override
        public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
            createNotification(new String(arg1.getPayload()));
        }
    }

    private class ToastRunnable implements Runnable {
        String message;

        public ToastRunnable(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            Toast.makeText(serviceContext, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void createNotification(String message) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] pattern = {500, 500, 500, 500, 500, 500, 500, 500, 500};
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(serviceContext)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("My little smarthome")
                        .setContentText(message)
                        .setLights(Color.CYAN, 1000, 2000)
                        .setVibrate(pattern)
                        .setSound(alarmSound);

        NotificationManager mNotifyMgr =
                (NotificationManager) serviceContext.getSystemService(serviceContext.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(UUID.randomUUID().hashCode(), mBuilder.build());
    }

    public void closeConnection() {
        try {
            c.close();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
