package com.jgr.game.vac.poller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.interfaces.SealCompletePoller;
import com.jgr.game.vac.interfaces.SmartThings;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.service.DeviceNames;
import com.jgr.game.vac.service.PropertyService;
import com.jgr.game.vac.service.WatchDog;

public class SealCompletePollerImpl extends Poller implements SealCompletePoller {
	@Autowired private DeviceNames deviceNames;
	@Autowired private PropertyService propertyService;
	@Autowired private SmartThings smartThings;
	@Autowired private SystemTime systemTime;
	@Autowired private WatchDog watchDog;

	private Logger logger = LoggerFactory.getLogger(SealCompletePollerImpl.class);
	
	private long sealStartTime;

	@Override
	public boolean doCheck() {
		if(smartThings.isOn(smartThings.getSwitchState(deviceNames.getStatusLight()))) {
			logger.info("Aborting");
//			if(lightState >= 75) {
//				throw new AbortException();
//			}
			return true;
		}
		
		if(propertyService.getAutoSealTime() != 0) {
			return propertyService.isDelayedStart() || ((systemTime.currentTime() - sealStartTime) > propertyService.getAutoSealTime()*propertyService.getTimeMutiple());
		} else {
			return smartThings.isOff(smartThings.getSwitchState(deviceNames.getPumpCheck()));
		}
	}
	
	@Override
	public void init() {
		if(propertyService.getAutoSealTime() != 0) {
			smartThings.setDeviceState(deviceNames.getPumpCheck(), false);
			sealStartTime = systemTime.currentTime();
		} else {
			smartThings.setDeviceState(deviceNames.getPumpCheck(), true);
		}
		logger.info("Pump On");
		if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getPumpSwitch(), true);
		watchDog.setSaftyValveState(true);
	}
}
