package com.jgr.game.vac.service;

import org.springframework.beans.factory.BeanNameAware;

public interface InputDevice extends BeanNameAware {
	public int readValue();
	public String getName();
	public String getBeanName();
}
