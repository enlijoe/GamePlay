package com.jgr.game.vac.interfaces;

public interface SmartThings {
	int getSwitchState(String id);
	void setDeviceState(String id, boolean state);
	boolean isOn(int value);
	boolean isOff(int value);
	public void listAllDevices();
}