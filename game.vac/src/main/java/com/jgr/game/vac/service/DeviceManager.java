package com.jgr.game.vac.service;

public interface DeviceManager {
	public <DeviceType> DeviceType getDevice(DeviceUrl deviceUrl);
}
