package com.github.jhaucke.smarthome.fritzboxconnector.types;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Rights")
public class Rights {

	@XmlElement(name = "Name")
	private List<String> nameList;
	@XmlElement(name = "Access")
	private List<String> accessList;

	public List<Right> getRights() {

		List<Right> rights = new ArrayList<Right>();

		for (int i = 0; nameList.size() > i; i++) {
			rights.add(new Right(nameList.get(i), accessList.get(i)));
		}

		return rights;
	}

	class Right {

		private String name;
		private String access;

		public Right(String name, String access) {
			super();
			this.name = name;
			this.access = access;
		}

		public String getName() {
			return name;
		}

		public String getAccess() {
			return access;
		}
	}
}
