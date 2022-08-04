package com.jgr.game.vac.poller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.interfaces.LightOffPoller;
import com.jgr.game.vac.interfaces.SmartThings;
import com.jgr.game.vac.service.DeviceNames;

// Done
public class LightOffPollerImpl extends Poller implements LightOffPoller {
	@Autowired private DeviceNames deviceNames;
	@Autowired private SmartThings smartThings;

	private Logger logger = LoggerFactory.getLogger(LightOffPollerImpl.class);

	@Override
	public boolean doCheck() {
		return smartThings.isOff(smartThings.getSwitchState(deviceNames.getStatusLight()));
	}
	
	@Override
	public void init() {
		logger.info("Waitting for light to turn off");
	}
}
