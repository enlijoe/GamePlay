package com.jgr.game.vac.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.interfaces.DoStimThread;
import com.jgr.game.vac.interfaces.SmartThings;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.service.DeviceNames;
import com.jgr.game.vac.service.PropertyService;
import com.jgr.game.vac.service.WatchDog;

public class DoStimThreadImpl implements DoStimThread {
	@Autowired private DeviceNames deviceNames;
	@Autowired private PropertyService propertyService;
	@Autowired private SmartThings smartThings;
	@Autowired private SystemTime systemTime;
	
	boolean cumAllowed;
	
	private Logger logger = LoggerFactory.getLogger(DoStimThreadImpl.class);

	public void run(WatchDog.WatchTimer timer) throws InterruptedException {
		int endCycle = propertyService.getNumCycles();

		
		logger.info("Cycling Stim Started");
		int onCycle = 0;
		while(true) {
			// turn on nipples
			logger.info("Stim started");

			changeState(timer, deviceNames.getNipplesSwitch(), propertyService.getNippleOnTime());
			changeState(timer, deviceNames.getVibe2Switch(), propertyService.getVibe2OnTime());
			changeState(timer, deviceNames.getProbeSwitch(), propertyService.getProbeOnTime());
			changeState(timer, deviceNames.getVibeSwitch(), propertyService.getVibeOnTime());
			
			// turn off everything
			turnOff(deviceNames.getNipplesSwitch(), propertyService.getNippleOnTime());
			turnOff(deviceNames.getProbeSwitch(), propertyService.getProbeOnTime());
			turnOff(deviceNames.getVibeSwitch(), propertyService.getVibeOnTime());
			turnOff(deviceNames.getVibe2Switch(), propertyService.getVibe2OnTime());

			onCycle++;

			logger.info("Stim Cycle " + onCycle + " Copmplete");
			if(onCycle >= endCycle) break;
			
			// time to rest
			systemTime.sleep(timer, propertyService.getStimRestTime() * propertyService.getTimeMutiple());
		}
		logger.info("Cycling Stim Ended");
	}
	
	private void turnOff(String device, long onTime) {
		if(onTime != 0) smartThings.setDeviceState(device, false);
	}

	private void changeState(WatchDog.WatchTimer timer, String device, long time) throws InterruptedException {
		if(time != 0) {
			smartThings.setDeviceState(device, true);
			systemTime.sleep(timer, time * propertyService.getTimeMutiple());
		}
	}
}
