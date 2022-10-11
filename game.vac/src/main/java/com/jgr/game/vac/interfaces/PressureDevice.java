package com.jgr.game.vac.interfaces;

public interface PressureDevice extends Device {
	public float readValue();
	public float getMinValue();
	public float getMaxValue();
}
