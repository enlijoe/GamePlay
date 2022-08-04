package com.jgr.game.vac.service;

public interface RemoteWatchDog {
	public void checkIn();
	public void enable();
	public int getCheckInFrequency();
	public void disable();
	public void enterErroredState();
	public void resetState();
}
