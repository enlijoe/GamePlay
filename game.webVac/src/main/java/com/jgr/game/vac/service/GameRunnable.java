package com.jgr.game.vac.service;

public interface GameRunnable extends GameOperation {
	public void shutdown();
	public void init();
}
