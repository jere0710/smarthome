package com.github.jhaucke.smarthome.fritzboxconnector;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.github.jhaucke.smarthome.fritzboxconnector.helper.HttpHelper;
import com.github.jhaucke.smarthome.fritzboxconnector.types.SessionInfo;

public class Authenticator {

	private static final String DEFAULT_INVALID_SID = "0000000000000000";

	public String fritzBoxHostName = "fritz.box";

	/**
	 * Default host name "fritz.box" will be used.
	 */
	public Authenticator() {
	}

	/**
	 * Constructor for {@link Authenticator}. If the FritzBox uses the default
	 * host name "fritz.box", {@code null} can be passed.
	 * 
	 * @param fritzBoxHostName
	 *            custom host name of the FritzBox, if existing
	 */
	public Authenticator(String fritzBoxHostName) {
		super();
		this.fritzBoxHostName = fritzBoxHostName;
	}


	public String getNewSessionId(final String username, final String password) throws IOException, JAXBException {

		SessionInfo sessionInfo = null;

		String responseWithoutCredentials = HttpHelper.executeHttpGet("http://" + fritzBoxHostName + "/login_sid.lua");
		sessionInfo = convertSessionInfoXML(responseWithoutCredentials);

		if (sessionInfo.getSid().equals(DEFAULT_INVALID_SID)) {

			String responseWithCredentials = HttpHelper
					.executeHttpGet("http://" + fritzBoxHostName + "/login_sid.lua?username="
					+ username + "&response=" + getResponse(sessionInfo.getChallenge(), password));
			sessionInfo = convertSessionInfoXML(responseWithCredentials);
		}

		return sessionInfo.getSid();
	}

	private String getResponse(String challenge, String password) {

		return challenge + "-" + getMD5Hash(challenge + "-" + password);
	}

	private String getMD5Hash(String stringToHash) {

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(stringToHash.getBytes("UTF-16LE"));
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			// TODO log Exception?
		}
		return null;
	}

	private SessionInfo convertSessionInfoXML(String xmlString) throws JAXBException {

		JAXBContext jaxbContext = JAXBContext.newInstance(SessionInfo.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

		StringReader reader = new StringReader(xmlString);
		return (SessionInfo) unmarshaller.unmarshal(reader);
	}

	/**
	 * Getter for the current host name of the FritzBox.
	 * 
	 * @return host name
	 */
	public String getFritzBoxHostName() {
		return fritzBoxHostName;
	}
}
