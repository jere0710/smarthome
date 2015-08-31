package com.github.jhaucke.smarthome.database.constants;

public enum ActuatorState {

	OFF(1), ON(2), FINISHED(3);

	private int value;

	private ActuatorState(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}
}
