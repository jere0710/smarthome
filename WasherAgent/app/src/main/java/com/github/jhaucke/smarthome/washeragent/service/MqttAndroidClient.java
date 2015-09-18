package com.github.jhaucke.smarthome.washeragent.service;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
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
    private static String brokerHost;
    MqttAsyncClient c;
    MqttConnectOptions conOptions;
    MqttCallbackImpl callback;
    private Context serviceContext = null;
    private Handler toastHandler;
    private int retryCount;
    private KeepAlivePingSender pingSender;

    private MqttAndroidClient(Context serviceContext, String brokerHost) {
        super();

        this.serviceContext = serviceContext;
        this.brokerHost = brokerHost;
        toastHandler = new Handler(Looper.getMainLooper());
        startClient();
    }

    public static MqttAndroidClient startInstance(Context serviceContext, String newBrokerHost) {
        if (instance == null || !brokerHost.equals(newBrokerHost)) {
            if (instance != null) {
                instance.closeConnection();
            }
            instance = new MqttAndroidClient(serviceContext, newBrokerHost);
        } else {
            instance.connect();
        }
        return instance;
    }

    private void startClient() {
        try {
            pingSender = new KeepAlivePingSender(serviceContext);
            c = new MqttAsyncClient("tcp://" + brokerHost + ":1883", Settings.Secure.ANDROID_ID, new MemoryPersistence(), pingSender);
            callback = new MqttCallbackImpl();
            c.setCallback(callback);
            conOptions = new MqttConnectOptions();
            conOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            conOptions.setKeepAliveInterval(1200);
            conOptions.setCleanSession(false);
            connect();
            c.subscribe("smarthome/#", 2);
        } catch (MqttException e) {
            createNotification("starting client ERROR");
        }
    }

    private void connect() {
        boolean tryConnecting = !c.isConnected();
        retryCount = 0;
        while (tryConnecting) {
            try {
                c.connect(conOptions);
            } catch (Exception e) {
                //toastHandler.post(new ToastRunnable("failed to connect!"));
            }
            if (c.isConnected()) {
                createNotification("connected");
                tryConnecting = false;
            } else {
                tryConnecting = pause(retryCount++);
            }
        }
    }

    private boolean pause(int retryCount) {
        try {
            if (retryCount < 5) {
                Thread.sleep(1000); // 1 s
                return true;
            } else {
                createNotification("failed to connect!");
                return false;
            }
        } catch (InterruptedException e) {
            // Error handling goes here...
        }
        return false;
    }

    private void createNotification(String message) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] pattern = {500, 500, 500, 500, 500, 500, 500, 500, 500};
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(serviceContext)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("My little smarthome")
                        .setContentText(message)
                        .setLights(Color.YELLOW, 1000, 2000)
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

    private class MqttCallbackImpl implements MqttCallback {

        public void connectionLost(Throwable cause) {
            createNotification("Connection lost");
            //connect();
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            // Not needed in this simple demo
        }

        @Override
        public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
            pingSender.schedule(0);
            createNotification(new String(arg1.getPayload()));
        }
    }
}
