package com.github.jhaucke.smarthome.mylittlesmarthome.watchdog;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jhaucke.smarthome.fritzboxconnector.HttpInterface;
import com.github.jhaucke.smarthome.mylittlesmarthome.database.SQLiteJDBC;
import com.github.jhaucke.smarthome.mylittlesmarthome.database.constants.Actuator;
import com.github.jhaucke.smarthome.mylittlesmarthome.database.constants.ActuatorState;
import com.github.jhaucke.smarthome.mylittlesmarthome.mqtt.MqttPublisher;

/**
 * {@link Runnable} to monitor the power consumption of the washing machine to
 * determine in which state it is.
 */
public class WashingMachineWD implements Runnable {

	private final Logger logger;

	private HttpInterface httpInterface;
	private Actuator washingMachine;

	/**
	 * Constructor for {@link WashingMachineWD}.
	 * 
	 * @param httpInterface
	 *            The interface to get connection to the actuator
	 * @param washingMachine
	 *            The specific actuator
	 */
	public WashingMachineWD(HttpInterface httpInterface, Actuator washingMachine) {
		super();
		logger = LoggerFactory.getLogger(WashingMachineWD.class);

		this.httpInterface = httpInterface;
		this.washingMachine = washingMachine;
	}

	@Override
	public void run() {

		SQLiteJDBC db = new SQLiteJDBC();
		MqttPublisher publisher = new MqttPublisher();

		publisher.sendMessage("smarthome/server/info/watchdog/washingmachine", getClass().getName() + " started");

		while (true) {
			boolean isWashingMachineActive = false;

			List<Integer> selectTheLast2Minutes = db.selectTheLast2Minutes(washingMachine.getValue());
			for (Integer power : selectTheLast2Minutes) {
				if (power > washingMachine.getPowerThreshold()) {
					isWashingMachineActive = true;
				}
			}
			String switchState = null;
			try {
				switchState = httpInterface.getSwitchState(washingMachine.getAIN());
			} catch (IOException ioe) {
				logger.error(ioe.getMessage());
			}
			Integer currentActuatorState = db.selectStateOfActuator(washingMachine.getValue());

			if (currentActuatorState != null && switchState != null) {
				if (switchState.equals("0") && currentActuatorState.intValue() != ActuatorState.OFF.getValue()) {
					publisher.sendMessage("smarthome/devices/washingmachine/state", ActuatorState.OFF.toString());
					db.updateStateOfActuator(washingMachine.getValue(), ActuatorState.OFF.getValue());
				}
				if (switchState.equals("1") && currentActuatorState.intValue() == ActuatorState.OFF.getValue()) {
					publisher.sendMessage("smarthome/devices/washingmachine/state", ActuatorState.ON.toString());
					db.updateStateOfActuator(washingMachine.getValue(), ActuatorState.ON.getValue());
				}
				if (switchState.equals("1") && currentActuatorState.intValue() != ActuatorState.ACTIVE.getValue()
						&& isWashingMachineActive) {
					publisher.sendMessage("smarthome/devices/washingmachine/state", ActuatorState.ACTIVE.toString());
					db.updateStateOfActuator(washingMachine.getValue(), ActuatorState.ACTIVE.getValue());
				}
				if (switchState.equals("1") && currentActuatorState.intValue() == ActuatorState.ACTIVE.getValue()
						&& !isWashingMachineActive) {
					publisher.sendMessage("smarthome/devices/washingmachine/state", ActuatorState.FINISHED.toString());
					db.updateStateOfActuator(washingMachine.getValue(), ActuatorState.FINISHED.getValue());
				}
			}

			try {
				Thread.sleep(15000);
			} catch (InterruptedException ie) {
				logger.error(ie.getMessage());
			}
		}

	}

}
