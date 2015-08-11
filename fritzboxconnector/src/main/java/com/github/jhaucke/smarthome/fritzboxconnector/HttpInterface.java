package com.github.jhaucke.smarthome.fritzboxconnector;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * AVM Home Automation HTTP Interface
 * 
 * @author Jere
 */
public class HttpInterface {

	private static final String FRITZ_BOX_HOST_NAME = "fritz.box";
	private final String sid;

	public HttpInterface(String sid) {
		super();
		this.sid = sid;
	}

	public void getSwitchList() {

		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpGet httpGetSwitchList = new HttpGet(getCommandURL(null, "getswitchlist"));
	}

	private String getCommandURL(String ain, String cmd) {

		if (ain == null) {
			return "http://" + FRITZ_BOX_HOST_NAME + "/webservices/homeautoswitch.lua?switchcmd=" + cmd + "&sid=" + sid;
		} else {
			return "http://" + FRITZ_BOX_HOST_NAME + "/webservices/homeautoswitch.lua?ain=" + ain + "&switchcmd=" + cmd
					+ "&sid=" + sid;
		}
	}
}
