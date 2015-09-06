package com.github.jhaucke.smarthome.datacollectorservice;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jhaucke.smarthome.basic.database.SQLiteJDBC;
import com.github.jhaucke.smarthome.basic.database.constants.Actuator;
import com.github.jhaucke.smarthome.fritzboxconnector.FritzBoxConnector;
import com.github.jhaucke.smarthome.fritzboxconnector.HttpInterface;

/**
 * This class is the entry point to start the data-collector-service.<br>
 * It continuously collects readings from the actuators.
 */
public class Collector {

	public static void main(String[] args) throws IOException, JAXBException, InterruptedException {

		Logger logger = LoggerFactory.getLogger(Collector.class);
		FritzBoxConnector fritzBoxConnector = null;

		if (args.length == 2) {
			fritzBoxConnector = new FritzBoxConnector(args[0], args[1], null);
		} else if (args.length == 3) {
			fritzBoxConnector = new FritzBoxConnector(args[0], args[1], args[2]);
		}

		HttpInterface httpInterface = fritzBoxConnector.getHttpInterface();
		SQLiteJDBC db = new SQLiteJDBC();
		String ainWashingMachine = Actuator.WASHING_MACHINE.getAIN();

		while (true) {
			String switchState = httpInterface.getSwitchState(ainWashingMachine);
			if (switchState.equals("1")) {
				String switchPower = httpInterface.getSwitchPower(ainWashingMachine);
				try {
					db.insertPowerData(Integer.valueOf(switchPower));
				} catch (NumberFormatException nfe) {
					logger.error(nfe.getMessage());
				}
			}
			Thread.sleep(10000);
		}
	}
}
