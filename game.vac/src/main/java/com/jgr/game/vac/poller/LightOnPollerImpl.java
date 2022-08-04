package com.jgr.game.vac.poller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.interfaces.LightOnPoller;
import com.jgr.game.vac.interfaces.SmartThings;
import com.jgr.game.vac.service.DeviceNames;

//Done
public class LightOnPollerImpl extends Poller implements LightOnPoller {
	@Autowired private DeviceNames deviceNames;
	@Autowired private SmartThings smartThings;

	private Logger logger = LoggerFactory.getLogger(LightOnPollerImpl.class);
	
	@Override
	public boolean doCheck() {
		return smartThings.isOn(smartThings.getSwitchState(deviceNames.getStatusLight()));
	}
	
	@Override
	public void init() {
		logger.info("Waitting for light to turn on");
	}
}
