package com.jgr.game.vac.interfaces;

import com.jgr.game.vac.service.WatchDog;

public interface SystemTime {
	long currentTime();
	void sleep(long time) throws InterruptedException;
	public void sleep(WatchDog.WatchTimer timer, long time) throws InterruptedException;
	public boolean safeSleep(WatchDog.WatchTimer timer, long time) throws InterruptedException;
}