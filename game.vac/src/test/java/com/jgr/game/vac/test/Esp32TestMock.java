package com.jgr.game.vac.test;

import com.jgr.game.vac.interfaces.Esp32;

public class Esp32TestMock implements Esp32 {

	boolean valveStatus = false;
	
	@Override
	public void turnOnValve() {
		valveStatus = true;
	}

	@Override
	public void turnOffValve() {
		valveStatus = false;
	}

	@Override
	public boolean getValveStatus() {
		return valveStatus;
	}
	
	public boolean readValveStatus() {
		return valveStatus;
	}
	
	public void reset() {
		valveStatus = false;
	}

}
