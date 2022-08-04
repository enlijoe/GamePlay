package com.jgr.game.vac.poller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.exceptions.AbortException;
import com.jgr.game.vac.interfaces.PressureSensor;
import com.jgr.game.vac.interfaces.SmartThings;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.interfaces.WaterFillPoller;
import com.jgr.game.vac.service.DeviceNames;
import com.jgr.game.vac.service.PropertyService;

public class WaterFillPollerImpl extends Poller implements WaterFillPoller {
	@Autowired private DeviceNames deviceNames;
	@Autowired private PropertyService propertyService;
	@Autowired private SmartThings smartThings;
	@Autowired private SystemTime systemTime;
	@Autowired private PressureSensor externalPressure;

	private Logger logger = LoggerFactory.getLogger(WaterFillPollerImpl.class);

	private long startTime;
	
	@Override
	public boolean doCheck() {
		int value = smartThings.getSwitchState(deviceNames.getStatusLight());
		if(smartThings.isOn(value)) {
			if(value >= 75) {
				logger.info("Aborting");
				throw new AbortException();
			}
			logger.info("Restarting");
			return true;
		}
		
		if(propertyService.isManualControl()) {
			return smartThings.isOn(smartThings.getSwitchState(deviceNames.getStatusLight())) ;
		} else {
			
			if(externalPressure.isAvailable()) {
				// need to check to see if the pressure has stopped falling
				return externalPressure.getPressure("bowelIn") <= propertyService.getEmptyPressure();
			} else {
				return systemTime.currentTime() - startTime > propertyService.getWaterFillTime()*propertyService.getTimeMutiple();
			}
		}
	}
	@Override
	public void init() {
		logger.info("Watting for fill");
		//smartThings.setDeviceState(bedRoomLight, manualControl);
		startTime = systemTime.currentTime();
	}
	
	public void setManualControl(boolean manualControl) {
		this.propertyService.setManualControl(manualControl);
	}
}
