package com.github.jhaucke.smarthome.fritzboxconnector;

import java.io.IOException;

import javax.xml.bind.JAXBException;

/**
 * Entry point to use the library.
 */
public class FritzBoxConnector {

	private String fritzBoxHostName = "fritz.box";
	private HttpInterface httpInterface;

	/**
	 * Constructor for {@link FritzBoxConnector}.
	 * 
	 * @param username
	 * @param password
	 * @param fritzBoxHostName
	 *            If the FritzBox use the default host name "fritz.box",
	 *            {@code null} can be passed.
	 * @throws IOException
	 * @throws JAXBException
	 */
	public FritzBoxConnector(final String username, final String password, final String fritzBoxHostName)
			throws IOException, JAXBException {
		super();

		if (fritzBoxHostName != null) {
			this.fritzBoxHostName = fritzBoxHostName;
		}

		Authenticator authenticator = new Authenticator(this.fritzBoxHostName);
		httpInterface = new HttpInterface(authenticator.getNewSessionId(username, password), this.fritzBoxHostName);
	}

	public HttpInterface getHttpInterface() {
		return httpInterface;
	}
}
