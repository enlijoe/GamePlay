package com.jgr.game.vac.service;

public interface GamePoller {
	public GameStatus run();
	public long getTimeOut();
}
