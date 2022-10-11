package com.jgr.game.vac.poller;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.PressureDevice;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.interfaces.WaterFillPoller;
import com.jgr.game.vac.service.DeviceMapperService;
import com.jgr.game.vac.service.DeviceUrl;

public class WaterFillPollerImpl extends Poller implements WaterFillPoller {
	@Autowired private SystemTime systemTime;
	@Autowired private DeviceMapperService deviceMapperService;

	private Logger logger = LoggerFactory.getLogger(WaterFillPollerImpl.class);

	@Value("${game.timeMutiple}") long timeMutiple;
	long emptyPressure = 0;
	@Value("${game.manualControl}") boolean manualControl;
	@Value("${game.waterFillTime}") long waterFillTime;
	
	private long startTime;
	
	@Value("${deviceUrl.status}") private String statusDeviceUrl;
	@Value("${deviceUrl.externalPressure}") private String externalPressureUrl;
	
	private InputDevice statusDevice;
	private PressureDevice externalPressure;
	
	@PostConstruct
	public void afterPropsSet() {
		statusDevice = deviceMapperService.getDevice(new DeviceUrl(statusDeviceUrl));
		externalPressure = deviceMapperService.getDevice(new DeviceUrl(externalPressureUrl));
	}
	
	@Override
	public boolean doCheck() {
		if(statusDevice.isOn()) {
			logger.info("Restarting");
			return true;
		}
		
		if(manualControl) {
			return statusDevice.isOn();
		} else {
			
			if(externalPressure != null) {
				// need to check to see if the pressure has stopped falling
				return externalPressure.readValue() <= emptyPressure;
			} else {
				return systemTime.currentTime() - startTime > waterFillTime*timeMutiple;
			}
		}
	}
	@Override
	public void init() {
		logger.info("Watting for fill");
		//smartThings.setDeviceState(bedRoomLight, manualControl);
		startTime = systemTime.currentTime();
	}
	
}
