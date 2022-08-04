package com.jgr.game.vac.poller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.interfaces.PumpOnPoller;
import com.jgr.game.vac.interfaces.SmartThings;
import com.jgr.game.vac.service.DeviceNames;

//Done
public class PumpOnPollerImpl extends Poller implements PumpOnPoller {
	@Autowired private DeviceNames deviceNames;
	@Autowired private SmartThings smartThings;

	private Logger logger = LoggerFactory.getLogger(PumpOnPollerImpl.class);
	
	@Override
	public boolean doCheck() {
		return smartThings.isOn(smartThings.getSwitchState(deviceNames.getPumpSwitch())) || smartThings.isOn(smartThings.getSwitchState(deviceNames.getStatusLight()));
	}
	
	@Override
	public void init() {
		logger.info("Waitting for pump to turn on");
	}
}
