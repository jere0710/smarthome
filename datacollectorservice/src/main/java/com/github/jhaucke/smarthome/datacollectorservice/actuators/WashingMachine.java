package com.github.jhaucke.smarthome.datacollectorservice.actuators;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jhaucke.smarthome.basic.database.SQLiteJDBC;
import com.github.jhaucke.smarthome.basic.database.constants.Actuator;
import com.github.jhaucke.smarthome.basic.mqtt.MqttPublisher;
import com.github.jhaucke.smarthome.fritzboxconnector.HttpInterface;

/**
 * {@link Runnable} to collect the power consumption of the washing machine.
 */
public class WashingMachine implements Runnable {

	private final Logger logger;

	private HttpInterface httpInterface;

	/**
	 * Constructor for {@link WashingMachine}.
	 */
	public WashingMachine(HttpInterface httpInterface) {
		super();
		logger = LoggerFactory.getLogger(WashingMachine.class);

		this.httpInterface = httpInterface;
	}

	@Override
	public void run() {

		SQLiteJDBC db = new SQLiteJDBC();
		String ainWashingMachine = Actuator.WASHING_MACHINE.getAIN();
		MqttPublisher publisher = new MqttPublisher();

		publisher.sendMessage("smarthome/server/info/datacollector/washingmachine", getClass().getName() + " started");

		while (true) {
			try {
				String switchState = httpInterface.getSwitchState(ainWashingMachine);
				if (switchState.equals("1")) {
					String switchPower = httpInterface.getSwitchPower(ainWashingMachine);
					db.insertPowerData(Integer.valueOf(switchPower));
				}
				Thread.sleep(10000);
			} catch (NumberFormatException | InterruptedException | IOException e) {
				logger.error(e.getMessage());
			}
		}
	}

}
