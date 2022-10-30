package com.jgr.game.vac.operations;

import com.jgr.game.vac.service.WatchDog;

public interface Operation {
	/**
	 * 
	 * @param timer
	 * @return true = operation interrupted
	 *         false = everything OK
	 * @throws InterruptedException
	 */
	public boolean run(WatchDog.WatchTimer timer) throws InterruptedException;
}
