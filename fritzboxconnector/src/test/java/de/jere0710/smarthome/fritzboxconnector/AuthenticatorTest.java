package de.jere0710.smarthome.fritzboxconnector;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AuthenticatorTest {

	@Test
	public void testGetMD5Hash() {

		Authenticator auth = new Authenticator();
		String md5 = auth.getMD5Hash("1234567z-äbc");

		assertEquals("9e224a41eeefa284df7bb0f26c2913e2", md5);
	}
}
