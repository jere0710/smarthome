package com.github.jhaucke.smarthome.fritzboxconnector;

import java.io.IOException;

import javax.xml.bind.JAXBException;

/**
 * Hello world!
 *
 */
public class FritzBoxConnector {

	private HttpInterface httpInterface;

	public FritzBoxConnector(final String username, final String password, final String fritzBoxHostName)
			throws IOException, JAXBException {
		super();

		Authenticator authenticator = new Authenticator(fritzBoxHostName);
		httpInterface = new HttpInterface(authenticator.getNewSessionId(username, password),
				authenticator.getFritzBoxHostName());
	}

	public HttpInterface getHttpInterface() {
		return httpInterface;
	}
}
