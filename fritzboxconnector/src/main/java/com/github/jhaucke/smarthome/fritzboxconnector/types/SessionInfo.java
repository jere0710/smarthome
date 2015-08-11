package com.github.jhaucke.smarthome.fritzboxconnector.types;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.github.jhaucke.smarthome.fritzboxconnector.types.Rights.Right;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SessionInfo")
public class SessionInfo {

	@XmlElement(name = "SID")
	private String sid;
	@XmlElement(name = "Challenge")
	private String challenge;
	@XmlElement(name = "BlockTime")
	private String blockTime;
	@XmlElement(name = "Rights")
	private Rights rights;

	public String getSid() {
		return sid;
	}

	public String getChallenge() {
		return challenge;
	}

	public String getBlockTime() {
		return blockTime;
	}

	public List<Right> getRights() {
		return rights.getRights();
	}

}
