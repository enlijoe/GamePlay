package com.jgr.game.vac.interfaces;

import com.jgr.game.vac.service.WatchDog;

public interface PumpOnPoller {
	boolean doCheck();
	void run(WatchDog.WatchTimer timer) throws InterruptedException;
	void init();
}