package de.jere0710.smarthome.fritzboxconnector;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import de.jere0710.smarthome.fritzboxconnector.jaxb.SessionInfo;

public class Authenticator {

	private static final String DEFAULT_INVALID_SID = "0000000000000000";

	public String getSessionId() throws IOException, JAXBException {

		String username = "";
		String password = "";
		
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpGet httpGetWithoutCredentials = new HttpGet("http://fritz.box/login_sid.lua");
		CloseableHttpResponse response = null;
		try {
			response = client.execute(httpGetWithoutCredentials);
			String xmlString = EntityUtils.toString(response.getEntity());

			SessionInfo sessionInfo = convertSessionInfoXML(xmlString);

			if (sessionInfo.getSid().equals(DEFAULT_INVALID_SID)) {

				HttpGet httpGetWithCredentials = new HttpGet(
						"http://fritz.box/login_sid.lua?username=" + username + "&response="
								+ getResponse(sessionInfo.getChallenge(), password));
				response = client.execute(httpGetWithCredentials);
				SessionInfo activeSessionInfo = convertSessionInfoXML(EntityUtils.toString(response.getEntity()));
				
				
				//HttpGet httpGetPower = new HttpGet("https://fritz.box/webservices/homeautoswitch.lua?ain=087610196692&switchcmd=getswitchpower&sid=" + activeSessionInfo.getSid());
				//client.close();
				//client = HttpClientBuilder.create().build();
				//response = client.execute(httpGetPower);
				//String xmlStringPower = EntityUtils.toString(response.getEntity());
				System.out.println(activeSessionInfo.getSid());
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			//client.close();
		}

		return null;
	}

	private String getResponse(String challenge, String password) {
		return challenge + "-" + getMD5Hash(challenge + "-" + password);
	}

	public String getMD5Hash(String stringToHash) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(stringToHash.getBytes("UTF-16LE"));
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			// TODO log Exception
		}
		return null;
	}

	private SessionInfo convertSessionInfoXML(String xmlString) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(SessionInfo.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

		StringReader reader = new StringReader(xmlString);
		return (SessionInfo) unmarshaller.unmarshal(reader);
	}
}
