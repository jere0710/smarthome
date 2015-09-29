package com.github.jhaucke.smarthome.mylittlesmarthome.datacollector;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jhaucke.smarthome.fritzboxconnector.HttpInterface;
import com.github.jhaucke.smarthome.mylittlesmarthome.database.SQLiteJDBC;
import com.github.jhaucke.smarthome.mylittlesmarthome.database.constants.Actuator;
import com.github.jhaucke.smarthome.mylittlesmarthome.mqtt.MqttPublisher;

/**
 * {@link Runnable} to collect the power consumption of the washing machine.
 */
public class WashingMachineDC implements Runnable {

	private final Logger logger;

	private HttpInterface httpInterface;

	/**
	 * Constructor for {@link WashingMachineDC}.
	 */
	public WashingMachineDC(HttpInterface httpInterface) {
		super();
		logger = LoggerFactory.getLogger(WashingMachineDC.class);

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
