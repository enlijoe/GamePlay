package com.jgr.game.vac.poller;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.OutputDevice;
import com.jgr.game.vac.interfaces.StartTimePoller;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.service.DeviceMapperService;
import com.jgr.game.vac.service.DeviceUrl;
import com.jgr.game.vac.thread.MaintainVacuumThreadImpl;

public class StartTimePollerImpl extends Poller implements StartTimePoller {
	private Logger logger = LoggerFactory.getLogger(StartTimePollerImpl.class);

	@Autowired private SystemTime systemTime;
	@Autowired private MaintainVacuumThreadImpl mintainVacuumRunable;
	@Autowired private DeviceMapperService deviceMapperService;
	
	
	@Value("${game.timeMutiple}") private long timeMutiple;
	@Value("${game.delaySeconds}") private long delaySeconds;
	@Value("${game.startHour}") private int startHour;
	@Value("${game.smartMin}") private int smartMin;
	@Value("${game.allowEarlyStart}") private boolean allowEarlyStart;
	@Value("${game.delayed}") private boolean delayedStart;
	
	@Value("${deviceUrl.status}") private String statusDeviceUrl;
	@Value("${deviceUrl.pumpState}") private String pumpStatusDeviceUrl;
	@Value("${deviceUrl.pumpCheck}") private String pumpCheckUrl;
	
	private InputDevice statusDevice;
	private InputDevice pumpStatusDevice;
	private OutputDevice pumpCheck;
	
	
	@PostConstruct
	public void afterPropsSet() {
		statusDevice = deviceMapperService.getDevice(new DeviceUrl(statusDeviceUrl));
		pumpStatusDevice = deviceMapperService.getDevice(new DeviceUrl(pumpStatusDeviceUrl));
		pumpCheck = deviceMapperService.getDevice(new DeviceUrl(pumpCheckUrl));
	}
	
	Calendar now = null;
	Calendar start = null;
	private long startTime;
	SimpleDateFormat formatter = new SimpleDateFormat();

	@Override
	public void displayStartTime() {
		if(delaySeconds != 0) {
			logger.info("Start time will be delayed by " + delaySeconds + " seconds.");
		} else {
			if(start == null || now == null) {
				now = GregorianCalendar.getInstance();
				start = GregorianCalendar.getInstance();
		
		
				start.set(Calendar.AM_PM, 0);
				start.set(Calendar.HOUR, startHour);
				start.set(Calendar.MINUTE, smartMin);
				start.set(Calendar.SECOND, 0);
				start.set(Calendar.MILLISECOND, 0);
				
				if(start.before(now)) {
					start.add(Calendar.DAY_OF_YEAR, 1);
				}
			}
			if(delayedStart) {
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
		if(allowEarlyStart && pumpStatusDevice.isOn()) {
			pumpCheck.setOff();
			return true;
		}
		
		try {
			mintainVacuumRunable.pause(false);
		} catch(Exception ex) {
			mintainVacuumRunable.pause(true);
		}
		
		if(delaySeconds != 0) {
			return systemTime.currentTime() - startTime > delaySeconds*timeMutiple || statusDevice.isOn();
		} else {
			return GregorianCalendar.getInstance().after(start) || statusDevice.isOn();
		}
	}

}
