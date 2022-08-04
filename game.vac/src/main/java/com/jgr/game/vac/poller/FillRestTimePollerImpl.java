package com.jgr.game.vac.poller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.interfaces.FillRestTimePoller;
import com.jgr.game.vac.interfaces.SmartThings;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.service.DeviceNames;
import com.jgr.game.vac.service.PropertyService;

public class FillRestTimePollerImpl extends Poller implements FillRestTimePoller {
	@Autowired private DeviceNames deviceNames;
	@Autowired private PropertyService propertyService;
	@Autowired private SmartThings smartThings;
	@Autowired private SystemTime systemTime;

	private long startTime;
	private boolean stimOn;
	
	private Logger logger = LoggerFactory.getLogger(FillRestTimePollerImpl.class);

	@Override
	public void init() {
		startTime = systemTime.currentTime();
		stimOn = false;
	}

	@Override
	public boolean doCheck() {
		if(propertyService.isHalfTimeAsStim() && !stimOn &&  systemTime.currentTime() - startTime > propertyService.getFillRestTime()*propertyService.getTimeMutiple()/2) {
			logger.info("Turned on Stim A");
			stimOn = true;
			smartThings.setDeviceState(deviceNames.getNipplesSwitch(), true);
		}
		return systemTime.currentTime() - startTime > propertyService.getFillRestTime()*propertyService.getTimeMutiple() || smartThings.isOn(smartThings.getSwitchState(deviceNames.getStatusLight()));
	}

}
