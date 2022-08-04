package com.jgr.game.vac.service;

public interface GameThread extends GameRunnable {
	public boolean isAlive();
	public void start();
	Throwable getExitError();
}
