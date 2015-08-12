package com.github.jhaucke.smarthome.datacollectorservice;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import com.github.jhaucke.smarthome.fritzboxconnector.FritzBoxConnector;
import com.github.jhaucke.smarthome.fritzboxconnector.HttpInterface;

/**
 * Hello world!
 *
 */
public class Collector {
	public static void main(String[] args) throws IOException, JAXBException {

		FritzBoxConnector fritzBoxConnector = null;

		if (args.length == 2) {
			fritzBoxConnector = new FritzBoxConnector(args[0], args[1], null);
		} else if (args.length == 3) {
			fritzBoxConnector = new FritzBoxConnector(args[0], args[1], args[2]);
		}

		HttpInterface httpInterface = fritzBoxConnector.getHttpInterface();

		System.out.println(httpInterface.getSwitchList());
	}
}
