package com.jgr.game.vac.poller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.OutputDevice;
import com.jgr.game.vac.interfaces.PumpOnPoller;
import com.jgr.game.vac.stereotype.InjectDevice;

public class PumpOnPollerImpl extends Poller implements PumpOnPoller {
	private Logger logger = LoggerFactory.getLogger(PumpOnPollerImpl.class);

	@InjectDevice("${deviceUrl.pumpState}") private InputDevice pumpStateDevice;
	@InjectDevice("${deviceUrl.status}") private InputDevice statusDevice;
	@InjectDevice("${deviceUrl.pumpCheck}") private OutputDevice pumpCheck;
	
	boolean phase2 = false;
	
	@Override
	public boolean doCheck() {
		if(!phase2 && pumpStateDevice.isOn()) {
			logger.trace("Entering phase 2");
			pumpCheck.setOff();
			phase2 = true;
		} else if(phase2 && pumpStateDevice.isOff()) {
			logger.trace("End phase 2");
			phase2 = false;
			return true;
		}
		
		return statusDevice.isOn();
	}
	
	@Override
	public void init() {
		logger.info("Waitting for pump to turn on");
		phase2 = false;
	}
}
