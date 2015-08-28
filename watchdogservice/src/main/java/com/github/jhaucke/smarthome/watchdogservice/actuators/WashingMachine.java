package com.github.jhaucke.smarthome.watchdogservice.actuators;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jhaucke.smarthome.database.SQLiteJDBC;
import com.github.jhaucke.smarthome.watchdogservice.mqtt.MqttPublisher;

/**
 * {@link Runnable} to monitor the power consumption of the washing machine to
 * determine in which state it is.
 */
public class WashingMachine implements Runnable {

	private final Logger logger;

	/**
	 * Constructor for {@link WashingMachine}.
	 */
	public WashingMachine() {
		super();
		logger = LoggerFactory.getLogger(WashingMachine.class);
	}

	@Override
	public void run() {

		SQLiteJDBC db = new SQLiteJDBC();
		MqttPublisher publisher = new MqttPublisher();

		while (true) {
			boolean isWashingMachineActive = false;

			List<Integer> selectTheLast5Minutes = db.selectTheLast5Minutes();
			for (Integer power : selectTheLast5Minutes) {
				if (power > 1000) {
					isWashingMachineActive = true;
				}
			}

			if (isWashingMachineActive) {
				logger.info("washing machine is active");
				publisher.sendMessage("washing machine", "washing machine is active");
			}

			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
		}

	}

}
