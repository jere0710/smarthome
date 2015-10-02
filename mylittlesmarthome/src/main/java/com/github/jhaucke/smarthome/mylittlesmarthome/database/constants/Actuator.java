package com.github.jhaucke.smarthome.mylittlesmarthome.database.constants;

public enum Actuator {

	WASHING_MACHINE(1);

	private int value;

	private Actuator(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public String getAIN() {
		switch (value) {
		case 1:
			return "087610196692";
		default:
			return "";
		}
	}

	public int getPowerThreshold() {
		switch (value) {
		case 1:
			return 1500;
		default:
			return 0;
		}
	}
}
