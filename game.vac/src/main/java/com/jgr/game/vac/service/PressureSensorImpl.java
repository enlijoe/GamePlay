package com.jgr.game.vac.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.interfaces.PressureSensor;
import com.jgr.game.vac.interfaces.SystemTime;

public class PressureSensorImpl implements PressureSensor {
	
	@Autowired private PropertyService propertyService;
	@Autowired SystemTime systemTime;
	
	private String url;
	long[] baseLine = null;
	long[] lastReading = null;
	List<String> deviceList = null;
	long lastReadingTime;
	
	boolean readingInProgress = false;
	
	public PressureSensorImpl(String url) {
		this.url = url;
	}
	
	@Override
	public boolean isAvailable() {
		return StringUtils.isNoneEmpty(url) && deviceList != null;
	}
	
	@Override
	public long getPressure(String device) {
		readData();
		return lastReading[deviceList.indexOf(device)];
	}
	
	@Override
	public long getBaseLine(String device) {
		return baseLine[deviceList.indexOf(device)];
	}
	
	@Override
	public void recordBaseLine(String device) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public List<String> getDeviceList() {
		return deviceList;
	}
	
	private void readData() {
		if(lastReadingTime + propertyService.getMaxPresureReadTime() < systemTime.currentTime()) {
			synchronized (this) {
				if(lastReadingTime + propertyService.getMaxPresureReadTime() < systemTime.currentTime()) {
					// read the data from the esp32
				}
			}
		}
	}
}
