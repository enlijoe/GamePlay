package com.jgr.game.vac.service;

import org.springframework.beans.factory.BeanNameAware;

public interface PressureDevice extends BeanNameAware {
	public float getValue();
	public float getMinValue();
	public float getMaxValue();
	public String getName();
	public String getBeanName();
}
