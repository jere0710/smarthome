package com.github.jhaucke.smarthome.basic.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class allows to publish a mqtt message to a mqtt broker.
 */
public class MqttPublisher {

	private final Logger logger;

	/**
	 * Constructor for {@link MqttPublisher}.
	 */
	public MqttPublisher() {
		super();
		logger = LoggerFactory.getLogger(MqttPublisher.class);
	}

	/**
	 * Publish a mqtt message to a mqtt broker.
	 * 
	 * @param topic
	 *            The topic of the message
	 * @param content
	 *            The message
	 */
	public void sendMessage(String topic, String content) {

		final int qos = 2;
		final String broker = "tcp://localhost:1883";
		final String clientId = "WatchDogService";
		MemoryPersistence persistence = new MemoryPersistence();

		try {
			MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			System.out.println("Connecting to broker: " + broker);
			sampleClient.connect(connOpts);
			System.out.println("Connected");
			System.out.println("Publishing message: " + content);
			MqttMessage message = new MqttMessage(content.getBytes());
			message.setQos(qos);
			sampleClient.publish(topic, message);
			System.out.println("Message published");
			sampleClient.disconnect();
			System.out.println("Disconnected");
		} catch (MqttException me) {
			System.out.println("reason " + me.getReasonCode());
			System.out.println("msg " + me.getMessage());
			System.out.println("loc " + me.getLocalizedMessage());
			System.out.println("cause " + me.getCause());
			System.out.println("excep " + me);
			me.printStackTrace();
			logger.error(me.getMessage());
		}
	}
}
