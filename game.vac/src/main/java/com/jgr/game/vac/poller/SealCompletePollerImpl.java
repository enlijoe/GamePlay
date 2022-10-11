package com.jgr.game.vac.poller;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.OutputDevice;
import com.jgr.game.vac.interfaces.SealCompletePoller;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.service.DeviceMapperService;
import com.jgr.game.vac.service.DeviceUrl;

public class SealCompletePollerImpl extends Poller implements SealCompletePoller {
	@Autowired private SystemTime systemTime;
	@Autowired private DeviceMapperService deviceMapperService;

	@Value("${game.timeMutiple}") long timeMutiple;
	@Value("${game.pumpAutoseal}") long autoSealTime;
	@Value("${game.delayed}") boolean delayedStart;
	@Value("${game.simulate}") boolean simulate;
	
	private Logger logger = LoggerFactory.getLogger(SealCompletePollerImpl.class);
	
	
	@Value("${deviceUrl.status}") private String statusDeviceUrl;
	@Value("${deviceUrl.pumpCheck}") private String pumpStateDeviceUrl;
	@Value("${deviceUrl.pumpCheck}") private String pumpCheckUrl;
	@Value("${deviceUrl.pumpSwitch}") private String pumpSwitchUrl;
	@Value("${deviceUrl.saftyValve}") private String saftyValveUrl;
	
	private InputDevice statusDevice;
	private InputDevice pumpStateDevice;
	private OutputDevice pumpCheck;
	private OutputDevice pumpSwitch;
	private OutputDevice saftyValve;
	
	
	private long sealStartTime;

	@PostConstruct
	public void afterPropsSet() {
		statusDevice = deviceMapperService.getDevice(new DeviceUrl(statusDeviceUrl));
		pumpStateDevice = deviceMapperService.getDevice(new DeviceUrl(pumpStateDeviceUrl));
		pumpCheck = deviceMapperService.getDevice(new DeviceUrl(pumpCheckUrl));
		pumpSwitch = deviceMapperService.getDevice(new DeviceUrl(pumpSwitchUrl));
		saftyValve = deviceMapperService.getDevice(new DeviceUrl(saftyValveUrl));
	}
	
	@Override
	public boolean doCheck() {
		if(statusDevice.isOn()) {
			logger.info("Aborting");
			return true;
		}
		
		if(autoSealTime != 0) {
			return delayedStart || ((systemTime.currentTime() - sealStartTime) > autoSealTime*timeMutiple);
		} else {
			return pumpStateDevice.isOff();
		}
	}
	
	@Override
	public void init() {
		if(autoSealTime != 0) {
			pumpCheck.setOff();
			sealStartTime = systemTime.currentTime();
		} else {
			pumpCheck.setOn();
		}
		logger.info("Pump On");

		if(!simulate) {
			saftyValve.setOn();
			pumpSwitch.setOn();
		}
	}
}
