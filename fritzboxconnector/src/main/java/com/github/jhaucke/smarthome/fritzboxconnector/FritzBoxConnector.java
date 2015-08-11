package com.github.jhaucke.smarthome.fritzboxconnector;

import java.io.IOException;

import javax.xml.bind.JAXBException;

/**
 * Hello world!
 *
 */
public class FritzBoxConnector {

	private final HttpInterface httpInterface;

	public FritzBoxConnector(final String username, final String password) throws IOException, JAXBException {
		super();

		httpInterface = new HttpInterface(new Authenticator().getNewSessionId(username, password));
	}

	public HttpInterface getHttpInterface() {
		return httpInterface;
	}
}
