package com.github.jhaucke.smarthome.washeragent.service;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by jeremias.haucke on 09.09.2015.
 */
public class MqttAndroidClient {

    private static MqttAndroidClient instance;

    MqttClient c;
    MqttConnectOptions conOptions;
    int messageId = 1;
    MqttCallbackImpl callback;

    private MqttAndroidClient() {
    }

    public static MqttAndroidClient getInstance(){
        if (instance != null){
            instance = new MqttAndroidClient();
        }
        return instance;
    }

    public void startClient(){
        try {
            System.out.println("Starting demo.");
            c = new MqttClient("tcp://localhost:1883", MqttClient.generateClientId(), new MemoryPersistence());
            callback = new MqttCallbackImpl();
            c.setCallback(callback);
            conOptions = new MqttConnectOptions();
            conOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            conOptions.setCleanSession(false);
            connect();
            c.subscribe("#", 2);
        } catch (MqttException e) {
            // Error handling goes here...
        }
    }

    private void connect() {
        // Let's try a cycle of reconnects. We rely on Paho's built-in HA code to hunt out
        // the primary appliance for us.
        boolean tryConnecting = true;
        while (tryConnecting) {
            try {
                c.connect(conOptions);
            } catch (Exception e1) {
                System.out.println("Connection attempt failed with '"+e1.getCause()+
                        "'. Retrying.");
        /* We'll do nothing as we'll shortly try connecting again. You may wish to track
         * the number of attempts to guard against long-term or permanent issues,
         * for example, misconfigured URIs.
         */
            }
            if (c.isConnected()) {
                System.out.println("Connected.");
                tryConnecting = false;
            } else {
                pause();
            }
        }
    }

    private void pause() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Error handling goes here...
        }
    }

    private class MqttCallbackImpl implements MqttCallback {

        public void connectionLost(Throwable cause) {
            System.out.println("Connection lost - attempting reconnect.");
            connect();
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            // Not needed in this simple demo
        }

        @Override
        public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
            // Not needed in this simple demo
        }
    }
}
