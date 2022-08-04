package com.jgr.game.vac.operations;

import com.jgr.game.vac.service.WatchDog;

public interface Operation {
	public boolean run() throws InterruptedException;
	public void setWatchDogTimer(WatchDog.WatchTimer timer);
}
