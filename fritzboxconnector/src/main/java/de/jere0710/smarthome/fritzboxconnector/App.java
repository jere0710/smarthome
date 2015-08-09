package de.jere0710.smarthome.fritzboxconnector;

import java.io.IOException;

import javax.xml.bind.JAXBException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException, JAXBException
    {
        new Authenticator().getSessionId();
    }
}
