package com.jgr.game.vac.poller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.OutputDevice;
import com.jgr.game.vac.interfaces.SealCompletePoller;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.stereotype.InjectDevice;

public class SealCompletePollerImpl extends Poller implements SealCompletePoller {
	private Logger logger = LoggerFactory.getLogger(SealCompletePollerImpl.class);

	@Autowired private SystemTime systemTime;

	@Value("${game.timeMutiple}") long timeMutiple;
	@Value("${game.pumpAutoseal}") long autoSealTime;
	@Value("${game.delayed}") boolean delayedStart;
	@Value("${game.simulate}") boolean simulate;
	
	@InjectDevice("${deviceUrl.status}") private InputDevice statusDevice;
	@InjectDevice("${deviceUrl.pumpCheck}") private InputDevice pumpStateDevice;
	@InjectDevice("${deviceUrl.pumpCheck}") private OutputDevice pumpCheck;
	@InjectDevice("${deviceUrl.pumpSwitch}") private OutputDevice pumpSwitch;
	@InjectDevice("${deviceUrl.saftyValve}") private OutputDevice saftyValve;
	
	private long sealStartTime;

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
