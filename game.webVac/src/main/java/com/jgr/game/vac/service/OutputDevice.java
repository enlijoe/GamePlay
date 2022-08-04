package com.jgr.game.vac.service;

import org.springframework.beans.factory.BeanNameAware;

public interface OutputDevice extends BeanNameAware {
	public void setValue(int value);
	public boolean isWatchDogControled();
	public void setDefaultState();
	public String getName();
	public String getBeanName();
}
