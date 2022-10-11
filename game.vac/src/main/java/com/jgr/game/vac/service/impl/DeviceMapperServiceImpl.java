package com.jgr.game.vac.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.jgr.game.vac.interfaces.Device;
import com.jgr.game.vac.service.DeviceManager;
import com.jgr.game.vac.service.DeviceMapperService;
import com.jgr.game.vac.service.DeviceUrl;

public class DeviceMapperServiceImpl implements DeviceMapperService, ApplicationContextAware {
	private Logger logger = LoggerFactory.getLogger(DeviceMapperServiceImpl.class);

	ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	@Override
	public <DeviceType extends Device> DeviceType getDevice(DeviceUrl deviceUrl) {
		DeviceManager deviceManager = applicationContext.getBean(deviceUrl.getDeviceManagerClass(), DeviceManager.class);
		DeviceType device = deviceManager.getDevice(deviceUrl);
		
		if(device == null) {
			logger.warn("Unable to find device for " + deviceUrl);
		}
		
		return device;
	}
}
