package com.jgr.game.vac.poller;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.interfaces.SmartThings;
import com.jgr.game.vac.interfaces.StartTimePoller;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.service.DeviceNames;
import com.jgr.game.vac.service.PropertyService;
import com.jgr.game.vac.thread.MaintainVacuumThreadImpl;

public class StartTimePollerImpl extends Poller implements StartTimePoller {
	private Logger logger = LoggerFactory.getLogger(StartTimePollerImpl.class);

	@Autowired private DeviceNames deviceNames;
	@Autowired private PropertyService propertyService;
	@Autowired private SmartThings smartThings;
	@Autowired private SystemTime systemTime;
	@Autowired private MaintainVacuumThreadImpl mintainVacuumRunable;
	
	Calendar now = null;
	Calendar start = null;
	private long startTime;
	boolean lightOffLine = false;
	SimpleDateFormat formatter = new SimpleDateFormat();

	@Override
	public void displayStartTime() {
		if(propertyService.getDelaySeconds() != 0) {
			logger.info("Start time will be delayed by " + propertyService.getDelaySeconds() + " seconds.");
		} else {
			if(start == null || now == null) {
				now = GregorianCalendar.getInstance();
				start = GregorianCalendar.getInstance();
		
		
				start.set(Calendar.AM_PM, 0);
				start.set(Calendar.HOUR, propertyService.getStartHour());
				start.set(Calendar.MINUTE, propertyService.getSmartMin());
				start.set(Calendar.SECOND, 0);
				start.set(Calendar.MILLISECOND, 0);
				
				if(start.before(now)) {
					start.add(Calendar.DAY_OF_YEAR, 1);
				}
			}
			if(propertyService.isDelayedStart()) {
				logger.info("Start time at " + formatter.format(start.getTime()));
			}
		}
	}
	
	@Override
	public void init() {
		startTime = systemTime.currentTime();
	}

	
	@Override
	public boolean doCheck() {
		if(propertyService.isAllowEarlyStart() && 0 != smartThings.getSwitchState(deviceNames.getPumpCheck())) {
			smartThings.setDeviceState(deviceNames.getPumpCheck(), false);
			return true;
		}

		int lightState = 0;
		
		try {
			lightState = smartThings.getSwitchState(deviceNames.getStatusLight());
			lightOffLine = false;
			mintainVacuumRunable.pause(false);
		} catch(Exception ex) {
			lightOffLine = true;
			mintainVacuumRunable.pause(true);
		}
		
		if(propertyService.getDelaySeconds() != 0) {
			return systemTime.currentTime() - startTime > propertyService.getDelaySeconds()*propertyService.getTimeMutiple() || smartThings.isOn(lightState);
		} else {
			return GregorianCalendar.getInstance().after(start) || smartThings.isOn(lightState);
		}
	}

}
