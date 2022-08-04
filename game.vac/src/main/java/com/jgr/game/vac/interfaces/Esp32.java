package com.jgr.game.vac.interfaces;

public interface Esp32 {
	void turnOnValve();
	void turnOffValve();
	boolean getValveStatus();
}