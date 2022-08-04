package com.jgr.game.vac.poller;

import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.interfaces.PressureSensor;
import com.jgr.game.vac.interfaces.SmartThings;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.interfaces.VaccumPoller;
import com.jgr.game.vac.service.DeviceNames;
import com.jgr.game.vac.service.PropertyService;

public class VaccumPollerImpl extends Poller implements VaccumPoller {
	@Autowired private DeviceNames deviceNames;
	@Autowired private PropertyService propertyService;
	@Autowired private SmartThings smartThings;
	@Autowired private SystemTime systemTime;
	@Autowired private PressureSensor externalPressure;

	private long restStart;
	
	@Override
	public boolean doCheck() {
		if(externalPressure.isAvailable()) {
			return externalPressure.getPressure("vacuum") >= propertyService.getMinVacuumPressure();
		} else {
			boolean timeExpired = systemTime.currentTime() - restStart > propertyService.getPumpRestTime() * propertyService.getTimeMutiple();
			boolean switchOn = smartThings.getSwitchState(deviceNames.getPumpSwitch()) != 0;
			return timeExpired || switchOn;
		}
	}
	
	@Override
	public void init() {
		restStart = systemTime.currentTime();
	}
	
	public int getPumpRestTime() {
		return propertyService.getPumpRestTime();
	}
	public void reset(int pumpRestTime) {
		this.propertyService.setPumpRestTime(pumpRestTime);
	}
}