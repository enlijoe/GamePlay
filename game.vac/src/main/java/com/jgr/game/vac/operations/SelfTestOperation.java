package com.jgr.game.vac.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelfTestOperation extends AbstractOperation {
	private Logger logger = LoggerFactory.getLogger(SelfTestOperation.class);

	@Override
	public boolean run() throws InterruptedException {
		logger.info("Running self test");
		
		// run self test 2 secs each device
		smartThings.setDeviceState(deviceNames.getNipplesSwitch(), true);
		if(systemTime.safeSleep(timer, 2*propertyService.getTimeMutiple())) return true;
		smartThings.setDeviceState(deviceNames.getNipplesSwitch(), false);

		smartThings.setDeviceState(deviceNames.getProbeSwitch(), true);
		if(systemTime.safeSleep(timer, 2*propertyService.getTimeMutiple())) return true;
		smartThings.setDeviceState(deviceNames.getProbeSwitch(), false);

		smartThings.setDeviceState(deviceNames.getVibeSwitch(), true);
		if(systemTime.safeSleep(timer, 2*propertyService.getTimeMutiple())) return true;
		smartThings.setDeviceState(deviceNames.getVibeSwitch(), false);
		
		return false;
	}
	
}
