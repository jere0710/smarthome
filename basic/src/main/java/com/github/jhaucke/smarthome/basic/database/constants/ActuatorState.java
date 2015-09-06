package com.github.jhaucke.smarthome.basic.database.constants;

public enum ActuatorState {

	OFF(1), ON(2), ACTIVE(3), FINISHED(4);

	private int value;

	private ActuatorState(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}
}
