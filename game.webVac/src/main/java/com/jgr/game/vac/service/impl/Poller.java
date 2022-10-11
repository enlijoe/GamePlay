package com.jgr.game.vac.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.service.WatchDog;

//Done
public abstract class Poller {
	private final long checkTime = 2000;

	@Autowired private SystemTime systemTime;
	
	public void run(WatchDog.WatchTimer timer) throws InterruptedException {
		init();
		while(!doCheck()) {
			systemTime.sleep(checkTime);
			if(timer != null) {
				timer.checkin();
			}
		} 
	}
	
	abstract public void init();
	
	abstract public boolean doCheck();
}
