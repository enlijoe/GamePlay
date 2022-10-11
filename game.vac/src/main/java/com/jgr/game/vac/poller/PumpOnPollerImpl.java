package com.jgr.game.vac.poller;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.OutputDevice;
import com.jgr.game.vac.interfaces.PumpOnPoller;
import com.jgr.game.vac.service.DeviceMapperService;
import com.jgr.game.vac.service.DeviceUrl;

public class PumpOnPollerImpl extends Poller implements PumpOnPoller {
	private Logger logger = LoggerFactory.getLogger(PumpOnPollerImpl.class);

	@Autowired private DeviceMapperService deviceMapperService;
	
	@Value("${deviceUrl.pumpState}") private String pumpStartStateUrl;
	@Value("${deviceUrl.status}") private String statusDeviceUrl;
	@Value("${deviceUrl.pumpCheck}") private String pumpCheckUrl;
	
	private InputDevice pumpStateDevice;
	private InputDevice statusDevice;
	private OutputDevice pumpCheck;
	
	boolean phase2 = false;
	
	@PostConstruct
	public void afterPropsSet() {
		pumpStateDevice = deviceMapperService.getDevice(new DeviceUrl(pumpStartStateUrl));
		statusDevice = deviceMapperService.getDevice(new DeviceUrl(statusDeviceUrl));
		pumpCheck = deviceMapperService.getDevice(new DeviceUrl(pumpCheckUrl));
	}
	
	@Override
	public boolean doCheck() {
		if(!phase2 && pumpStateDevice.isOn()) {
			logger.trace("Entering phase 2");
			pumpCheck.setOff();
			phase2 = true;
		} else if(phase2 && pumpStateDevice.isOff()) {
			logger.trace("End phase 2");
			phase2 = false;
			return true;
		}
		
		return statusDevice.isOn();
	}
	
	@Override
	public void init() {
		logger.info("Waitting for pump to turn on");
		phase2 = false;
	}
}
