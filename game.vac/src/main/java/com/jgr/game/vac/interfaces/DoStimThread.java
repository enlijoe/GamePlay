package com.jgr.game.vac.interfaces;

import com.jgr.game.vac.service.WatchDog;

public interface DoStimThread {
	public void run(WatchDog.WatchTimer timer) throws InterruptedException;
}