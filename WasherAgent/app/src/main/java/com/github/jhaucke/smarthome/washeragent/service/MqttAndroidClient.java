package com.github.jhaucke.smarthome.washeragent.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttAndroidClient {

    private static MqttAndroidClient instance = null;
    private Context serviceContext = null;
    private Handler toastHandler;

    MqttClient c;
    MqttConnectOptions conOptions;
    MqttCallbackImpl callback;

    private MqttAndroidClient(Context serviceContext) {
        super();

        this.serviceContext = serviceContext;
        toastHandler = new Handler(Looper.getMainLooper());
        startClient();
    }

    public static void startInstance(Context serviceContext) {
        if (instance == null) {
            instance = new MqttAndroidClient(serviceContext);
        }
    }

    private void startClient() {
        try {
            toastHandler.post(new ToastRunnable("starting client"));
            c = new MqttClient("tcp://iot.eclipse.org:1883", android.os.Build.MODEL, new MemoryPersistence());
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
                pause();
            }
        }
    }

    private void pause() {
        try {
            Thread.sleep(2000);
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
            toastHandler.post(new ToastRunnable("Message: " + new String(arg1.getPayload())));
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
}
