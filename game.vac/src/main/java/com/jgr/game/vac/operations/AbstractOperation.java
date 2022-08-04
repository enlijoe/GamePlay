package com.jgr.game.vac.operations;

import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.interfaces.SmartThings;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.service.DeviceNames;
import com.jgr.game.vac.service.PropertyService;
import com.jgr.game.vac.service.WatchDog.WatchTimer;

public abstract class AbstractOperation implements Operation {
	protected WatchTimer timer;

	@Autowired protected DeviceNames deviceNames;
	@Autowired protected SmartThings smartThings;
	@Autowired protected PropertyService propertyService;
	@Autowired protected SystemTime systemTime;
	
	@Override
	public void setWatchDogTimer(WatchTimer timer) {
		this.timer = timer;
	}
}
