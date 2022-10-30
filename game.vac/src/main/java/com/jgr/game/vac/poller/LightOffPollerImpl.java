package com.jgr.game.vac.poller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.LightOffPoller;
import com.jgr.game.vac.stereotype.InjectDevice;

// Done
public class LightOffPollerImpl extends Poller implements LightOffPoller {
	private Logger logger = LoggerFactory.getLogger(LightOffPollerImpl.class);

	@InjectDevice("${deviceUrl.status}") InputDevice statusDevice;
	
	@Override
	public boolean doCheck() {
		return statusDevice.isOff();
	}
	
	@Override
	public void init() {
		logger.info("Waitting for light to turn off");
	}
}
