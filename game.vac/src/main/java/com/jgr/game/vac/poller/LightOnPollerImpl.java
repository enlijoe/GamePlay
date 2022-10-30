package com.jgr.game.vac.poller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.LightOnPoller;
import com.jgr.game.vac.stereotype.InjectDevice;

//Done
public class LightOnPollerImpl extends Poller implements LightOnPoller {
	@InjectDevice("${deviceUrl.status}") private InputDevice statusLight;
	
	private Logger logger = LoggerFactory.getLogger(LightOnPollerImpl.class);
	
	@Override
	public boolean doCheck() {
		return statusLight.isOn();
	}
	
	@Override
	public void init() {
		logger.info("Waitting for light to turn on");
	}
}
