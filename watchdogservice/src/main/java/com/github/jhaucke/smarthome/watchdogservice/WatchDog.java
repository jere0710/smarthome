package com.github.jhaucke.smarthome.watchdogservice;

import java.io.IOException;

import com.github.jhaucke.smarthome.fritzboxconnector.FritzBoxConnector;
import com.github.jhaucke.smarthome.fritzboxconnector.HttpInterface;
import com.github.jhaucke.smarthome.watchdogservice.actuators.WashingMachine;

/**
 * This class is the entry point to start the watchdog-service.<br>
 * It starts threads for some actuators which continuously checks the collected
 * readings of that actuator and initiates desired actions if needed.
 */
public class WatchDog {

	public static void main(String[] args) throws IOException {

		FritzBoxConnector fritzBoxConnector = null;

		if (args.length == 2) {
			fritzBoxConnector = new FritzBoxConnector(args[0], args[1], null);
		} else if (args.length == 3) {
			fritzBoxConnector = new FritzBoxConnector(args[0], args[1], args[2]);
		}

		HttpInterface httpInterface = fritzBoxConnector.getHttpInterface();

		Thread washingMachineThread = new Thread(new WashingMachine(httpInterface));
		washingMachineThread.start();
	}
}
