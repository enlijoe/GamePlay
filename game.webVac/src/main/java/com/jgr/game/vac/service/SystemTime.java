package com.jgr.game.vac.service;

public interface SystemTime {
	long currentTime();
	void sleep(long time) throws InterruptedException;
}