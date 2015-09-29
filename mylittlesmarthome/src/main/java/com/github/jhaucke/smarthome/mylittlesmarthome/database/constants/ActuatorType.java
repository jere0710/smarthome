package com.github.jhaucke.smarthome.mylittlesmarthome.database.constants;

public enum ActuatorType {

	FRITZ_DECT_200(1);

	private int value;

	private ActuatorType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}
}
