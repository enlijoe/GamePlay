package com.jgr.game.vac.poller;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.PressureDevice;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.interfaces.VaccumPoller;
import com.jgr.game.vac.service.DeviceMapperService;
import com.jgr.game.vac.service.DeviceUrl;

public class VaccumPollerImpl extends Poller implements VaccumPoller {
	@Autowired private SystemTime systemTime;
	@Autowired private DeviceMapperService deviceMapperService;

	private long restStart;
	
	@Value("${game.minVacPressure}") private float minVacuumPressure;
	@Value("${game.timeMutiple}") private long timeMutiple;
	@Value("${game.pumpAutoseal}") private long pumpAutoseal;
	
	
	@Value("${deviceUrl.pumpCheck}") private String pumpStatusDeviceUrl;
	@Value("${deviceUrl.vaccumPressure}") private String vaccumPressureUrl;
	
	private InputDevice pumpStatusDevice;
	private PressureDevice vacuumPressure;
	
	@PostConstruct
	public void afterPropsSet() {
		pumpStatusDevice = deviceMapperService.getDevice(new DeviceUrl(pumpStatusDeviceUrl));
		vacuumPressure = deviceMapperService.getDevice(new DeviceUrl(vaccumPressureUrl));
	}
	
	@Override
	public boolean doCheck() {
		if(vacuumPressure != null) {
			return vacuumPressure.readValue() >= minVacuumPressure;
		} else {
			boolean timeExpired = systemTime.currentTime() - restStart > pumpAutoseal * timeMutiple;
			boolean switchOn = pumpStatusDevice.isOn();
			return timeExpired || switchOn;
		}
	}
	
	@Override
	public void init() {
		restStart = systemTime.currentTime();
	}
	
}