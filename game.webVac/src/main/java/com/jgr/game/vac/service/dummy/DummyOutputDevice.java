package com.jgr.game.vac.service.dummy;

import com.jgr.game.vac.service.OutputDevice;

public class DummyOutputDevice implements OutputDevice {
	String beanName;
	
	@Override
	public void setBeanName(String name) {
		beanName = name;
	}
	
	@Override
	public String getBeanName() {
		return beanName;
	}
	
	@Override
	public boolean isWatchDogControled() {
		return true;
	}
	
	@Override
	public void setDefaultState() {
	}
	
	@Override
	public void setValue(int value) {
	}
	
	@Override
	public String getName() {
		return "Dummy";
	}
}
