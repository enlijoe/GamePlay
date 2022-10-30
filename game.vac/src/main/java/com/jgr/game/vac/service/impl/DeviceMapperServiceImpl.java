package com.jgr.game.vac.service.impl;

import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.jgr.game.vac.interfaces.Device;
import com.jgr.game.vac.service.DeviceManager;
import com.jgr.game.vac.service.DeviceMapperService;
import com.jgr.game.vac.service.DeviceUrl;
import com.jgr.game.vac.stereotype.InjectDevice;

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
		
		if(deviceManager == null) {
			logger.warn("Unable to find device manager for "+ deviceUrl);
			return null;
		}
		
		DeviceType device = deviceManager.getDevice(deviceUrl);
		
		if(device == null) {
			logger.warn("Unable to find device for " + deviceUrl);
		}
		
		return device;
	}
	
	public <BeanType> BeanType injectDevices(BeanType bean) {
		Class<?> beanClass = bean.getClass();
		
		for(Field field:beanClass.getDeclaredFields()) {
			InjectDevice injectDevice = field.getAnnotation(InjectDevice.class);
			if(injectDevice == null) continue;
			String deviceUrl = injectDevice.value();
			if(StringUtils.isEmpty(deviceUrl)) continue;
			deviceUrl = deviceUrl.strip();
			if(StringUtils.isEmpty(deviceUrl)) continue;
			if(deviceUrl.startsWith("${") && deviceUrl.endsWith("}")) {
				deviceUrl = applicationContext.getEnvironment().getProperty(deviceUrl.substring(2, deviceUrl.length()-1));
				if(StringUtils.isEmpty(deviceUrl)) {
					String errorMsg = "Error looking up device " + injectDevice.value() + " into field " + field.getName() + " for bean of type " + beanClass.getName(); 
					logger.error(errorMsg);
					throw new RuntimeException(errorMsg);
				}
			}
			Device device = getDevice(new DeviceUrl(deviceUrl));
			try {
				field.set(bean, device);
			} catch (IllegalArgumentException | IllegalAccessException ex) {
				String errorMsg = "Error injecting device " + deviceUrl + " into field " + field.getName() + " for bean of type " + beanClass.getName(); 
				logger.error(errorMsg, ex);
				throw new RuntimeException(errorMsg, ex);
			}
		}
		return bean;
	}
	
}
