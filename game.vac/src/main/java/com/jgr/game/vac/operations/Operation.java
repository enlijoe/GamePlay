package com.jgr.game.vac.operations;

import com.jgr.game.vac.service.WatchDog;

public interface Operation {
	public boolean run(WatchDog.WatchTimer timer) throws InterruptedException;
}
