package com.github.jhaucke.smarthome.fritzboxconnector.helper;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 * Helper class to handle http operations.
 */
public class HttpHelper {

	/**
	 * This method executes the given uri as a {@link HttpGet} and returns the
	 * response as string.
	 * 
	 * @param uri
	 *            the requested url
	 * @return the response as string
	 * @throws IOException
	 */
	public static String executeHttpGet(String uri) throws IOException {

		CloseableHttpResponse response = null;

		try {
			CloseableHttpClient client = HttpClientBuilder.create().build();
			response = client.execute(new HttpGet(uri));
			HttpEntity entity = response.getEntity();
			String responseAsString = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			return responseAsString;
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}
}
