package com.jgr.game.vac.poller;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.LightOffPoller;
import com.jgr.game.vac.service.DeviceMapperService;
import com.jgr.game.vac.service.DeviceUrl;

// Done
public class LightOffPollerImpl extends Poller implements LightOffPoller {
	private Logger logger = LoggerFactory.getLogger(LightOffPollerImpl.class);

	@Autowired DeviceMapperService deviceMapperService;

	@Value("${deviceUrl.status}") String statusDeviceUrl;
	
	InputDevice statusDevice;
	
	@PostConstruct
	public void afterPropsSet() {
		statusDevice = deviceMapperService.getDevice(new DeviceUrl(statusDeviceUrl));
	}
	
	@Override
	public boolean doCheck() {
		return statusDevice.isOff();
	}
	
	@Override
	public void init() {
		logger.info("Waitting for light to turn off");
	}
}
