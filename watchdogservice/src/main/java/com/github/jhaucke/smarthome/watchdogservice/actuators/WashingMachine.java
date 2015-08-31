package com.github.jhaucke.smarthome.watchdogservice.actuators;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jhaucke.smarthome.database.SQLiteJDBC;
import com.github.jhaucke.smarthome.database.constants.Actuator;
import com.github.jhaucke.smarthome.database.constants.ActuatorState;
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

			Integer currentState = db.selectStateOfActuator(Actuator.WASHING_MACHINE.getValue());
			if (currentState != null) {
				if (currentState.intValue() == ActuatorState.OFF.getValue() && isWashingMachineActive) {
					publisher.sendMessage("smarthome/devices/washingmachine/state", ActuatorState.ON.toString());
					db.updateStateOfActuator(Actuator.WASHING_MACHINE.getValue(), ActuatorState.ON.getValue());
				}
				if (currentState.intValue() == ActuatorState.ON.getValue() && !isWashingMachineActive) {
					publisher.sendMessage("smarthome/devices/washingmachine/state", ActuatorState.FINISHED.toString());
					db.updateStateOfActuator(Actuator.WASHING_MACHINE.getValue(), ActuatorState.FINISHED.getValue());
				}
			}

			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
		}

	}

}
