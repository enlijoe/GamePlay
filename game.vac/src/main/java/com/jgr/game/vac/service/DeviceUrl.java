package com.jgr.game.vac.service;

import org.apache.commons.lang3.StringUtils;

public class DeviceUrl {
	private String deviceManager;
	private String deviceId;
	private String subDeviceId;
	
	public DeviceUrl(String deviceUrl) {
		int deviceClassSeperator = StringUtils.indexOf(deviceUrl, "://");
		if(deviceClassSeperator <= 0) {
			// error
		} else {
			deviceManager = deviceUrl.substring(0, deviceClassSeperator);
			int deviceIdSeperator = StringUtils.indexOf(deviceUrl, "/", deviceClassSeperator+3);
			if(deviceIdSeperator <= 0) {
				deviceId = deviceUrl.substring(deviceClassSeperator+3);
			} else {
				deviceId = deviceUrl.substring(deviceClassSeperator+3, deviceIdSeperator);
				subDeviceId = deviceUrl.substring(deviceIdSeperator+1);
			}
			
		}
	}
	
	public String getDeviceManagerClass() {
		return deviceManager;
	}
	
	public String getDeviceId() {
		return deviceId;
	}
	
	
	public String getSubDevice() {
		return subDeviceId;
	}
	
	@Override
	public String toString() {
		return deviceManager + "://" + deviceId + "/" + subDeviceId;
	}
}
