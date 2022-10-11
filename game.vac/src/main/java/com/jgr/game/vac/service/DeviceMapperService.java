package com.jgr.game.vac.service;

import com.jgr.game.vac.interfaces.Device;

public interface DeviceMapperService {
	public <DeviceType extends Device> DeviceType getDevice(DeviceUrl deviceUrl);
}
