package com.jgr.game.vac.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;

public class SmartThingsDeviceFactory implements ApplicationContextAware {
	ConfigurableListableBeanFactory  beanFactory;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if(applicationContext instanceof GenericApplicationContext) {
			beanFactory = ((GenericApplicationContext)applicationContext).getBeanFactory();
		}
	}
	
	@PostConstruct
	public void init() {
	}
	
}
