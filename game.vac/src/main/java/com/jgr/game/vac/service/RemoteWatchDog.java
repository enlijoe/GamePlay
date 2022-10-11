package com.jgr.game.vac.service;

public interface RemoteWatchDog {
	public void checkIn();
	public boolean getStatus();
	public void reset();
	public void errorState();
	public void disable();
	public void enable();
	public String getDescription();
}
