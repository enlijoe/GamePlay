package com.jgr.game.vac.interfaces;

import java.util.List;

public interface PressureSensor {
	public boolean isAvailable();
	public long getPressure(String device);
	public void recordBaseLine(String device);
	public long getBaseLine(String device);
	List<String> getDeviceList();
}
