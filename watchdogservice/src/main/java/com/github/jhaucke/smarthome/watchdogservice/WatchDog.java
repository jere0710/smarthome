package com.github.jhaucke.smarthome.watchdogservice;

import com.github.jhaucke.smarthome.watchdogservice.actuators.WashingMachine;

/**
 * This class is the entry point to start the watchdog-service.<br>
 * It starts threads for some actuators which continuously checks the collected
 * readings of that actuator and initiates desired actions if needed.
 */
public class WatchDog {

	public static void main(String[] args) {

		Thread washingMachineThread = new Thread(new WashingMachine());
		washingMachineThread.start();
	}
}
