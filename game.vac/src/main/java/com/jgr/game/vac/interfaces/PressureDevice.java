package com.jgr.game.vac.interfaces;

public interface PressureDevice extends Esp32SubDevice {
	public float readValue();
	public float getMinValue();
	public float getMaxValue();
}
