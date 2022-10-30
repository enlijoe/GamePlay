package com.jgr.game.vac.poller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.PressureDevice;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.interfaces.VaccumPoller;
import com.jgr.game.vac.stereotype.InjectDevice;

public class VaccumPollerImpl extends Poller implements VaccumPoller {
	@Autowired private SystemTime systemTime;

	private long restStart;
	
	@Value("${game.minVacPressure}") private float minVacuumPressure;
	@Value("${game.timeMutiple}") private long timeMutiple;
	@Value("${game.pumpAutoseal}") private long pumpAutoseal;
	
	@InjectDevice("${deviceUrl.pumpCheck}") private InputDevice pumpStatusDevice;
	@InjectDevice("${deviceUrl.vaccumPressure}") private PressureDevice vacuumPressure;
	
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