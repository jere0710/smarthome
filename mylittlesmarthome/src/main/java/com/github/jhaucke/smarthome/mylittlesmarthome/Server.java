package com.github.jhaucke.smarthome.mylittlesmarthome;

import java.io.IOException;

import com.github.jhaucke.smarthome.fritzboxconnector.FritzBoxConnector;
import com.github.jhaucke.smarthome.fritzboxconnector.HttpInterface;
import com.github.jhaucke.smarthome.mylittlesmarthome.datacollector.WashingMachineDC;
import com.github.jhaucke.smarthome.mylittlesmarthome.watchdog.WashingMachineWD;

/**
 * Hello world!
 *
 */
public class Server {
	public static void main(String[] args) throws IOException {
		FritzBoxConnector fritzBoxConnector = null;

		if (args.length == 2) {
			fritzBoxConnector = new FritzBoxConnector(args[0], args[1], null);
		} else if (args.length == 3) {
			fritzBoxConnector = new FritzBoxConnector(args[0], args[1], args[2]);
		}

		HttpInterface httpInterface = fritzBoxConnector.getHttpInterface();

		Thread threadWashingMachineDC = new Thread(new WashingMachineDC(httpInterface));
		threadWashingMachineDC.start();
		Thread threadWashingMachineWD = new Thread(new WashingMachineWD(httpInterface));
		threadWashingMachineWD.start();
	}
}
