package com.github.jhaucke.smarthome.mylittlesmarthome.mqtt;

import java.text.SimpleDateFormat;
import java.util.Date;

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

		String newLine = System.getProperty("line.separator");
		final int qos = 2;
		final String broker = "tcp://localhost:1883";
		final String clientId = "MyLittleSmarthomeServer";
		MemoryPersistence persistence = new MemoryPersistence();

		try {
			MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			logger.info("Connecting to broker: " + broker);
			sampleClient.connect(connOpts);
			logger.info("Connected");
			logger.info("Publishing message: " + content);
			content = new SimpleDateFormat("HH:mm:ss").format(new Date()) + " - " + content;
			MqttMessage message = new MqttMessage(content.getBytes());
			message.setQos(qos);
			sampleClient.publish(topic, message);
			logger.info("Message published");
			sampleClient.disconnect();
			logger.info("Disconnected");
		} catch (MqttException me) {
			logger.error("Reason: " + me.getReasonCode() + newLine + "Msg: " + me.getMessage() + newLine + "Cause: "
					+ me.getCause() + newLine + "Exception: " + me);
		}
	}
}
