package com.jgr.game.vac.poller;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.LightOnPoller;
import com.jgr.game.vac.service.DeviceMapperService;
import com.jgr.game.vac.service.DeviceUrl;

//Done
public class LightOnPollerImpl extends Poller implements LightOnPoller {
	@Autowired private DeviceMapperService deviceMapperService;

	@Value("${deviceUrl.status}") private String statusLightUrl;
	
	private InputDevice statusLight;
	
	@PostConstruct
	public void afterPropSet() {
		statusLight = deviceMapperService.getDevice(new DeviceUrl(statusLightUrl));
	}
	
	private Logger logger = LoggerFactory.getLogger(LightOnPollerImpl.class);
	
	@Override
	public boolean doCheck() {
		return statusLight.isOn();
	}
	
	@Override
	public void init() {
		logger.info("Waitting for light to turn on");
	}
}
