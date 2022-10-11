package com.jgr.game.vac.service;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.LightOffPoller;
import com.jgr.game.vac.interfaces.OutputDevice;
import com.jgr.game.vac.interfaces.PumpOnPoller;
import com.jgr.game.vac.interfaces.SealCompletePoller;

public class SleepMode {
	@Autowired private PumpOnPoller pumpOnPoller;
	@Autowired private SealCompletePoller sealCompletePoller;
	@Autowired private LightOffPoller lightOffPoller;
	@Autowired private WatchDog watchDog;
	@Autowired private DeviceMapperService deviceMapperService;

	private Logger logger = LoggerFactory.getLogger(SleepMode.class);
	private WatchDog.WatchTimer timer;
	
	@Value("${deviceUrl.status}") String statusDeviceUrl;
	@Value("${deviceUrl.pumpSwitch}") String pumpSwitchUrl;
	@Value("${deviceUrl.pumpCheck}") String pumpCheckUrl;
	@Value("${deviceUrl.statusLightCheck}") String statusCheckUrl;
	
	InputDevice statusDevice;
	OutputDevice pumpSwitch;
	OutputDevice pumpCheck;
	OutputDevice statusCheck;
	
	class WatchDogTimerExpired implements Runnable {
		Thread myThread;
		
		WatchDogTimerExpired() {
			myThread = Thread.currentThread();
		}
		
		@Override
		public void run() {
			try {
				pumpSwitch.setOff();
				myThread.interrupt();
			} catch(Exception ex) {
				logger.error("Unable to turn off valve", ex);
			}
		}
	}
	
	
	@PostConstruct
	public void afterPropsSet() {
		statusDevice = deviceMapperService.getDevice(new DeviceUrl(statusDeviceUrl));
		pumpSwitch = deviceMapperService.getDevice(new DeviceUrl(pumpSwitchUrl));
		pumpCheck = deviceMapperService.getDevice(new DeviceUrl(pumpCheckUrl));
		statusCheck = deviceMapperService.getDevice(new DeviceUrl(statusCheckUrl));
	}
	
	public void runProgram() throws InterruptedException {
		timer = watchDog.addTimer(new WatchDogTimerExpired(), "Main Process");

		try {
			do {
				timer.checkin();
				pumpSwitch.setOn();
				timer.checkin();
				pumpCheck.setOff();
				timer.checkin();
				
				// the light must be off for things to work
				lightOffPoller.run(timer);
				statusCheck.setOn();
				timer.checkin();

				// wait for the pump to turn on
				pumpOnPoller.run(timer);
				if(statusDevice.isOn()) {
					continue;
				}
				pumpSwitch.setOn();
				timer.checkin();
				logger.info("Pump running");
	
				statusCheck.setOff();
				timer.checkin();
				sealCompletePoller.run(timer);
				logger.info("Seal release restarting program.");
				
				// turn off pump now and restart
				pumpSwitch.setOff();
				timer.checkin();
				pumpCheck.setOff();
				timer.checkin();
			} while(true);
		} catch(InterruptedException iex) {
			// we have been shutdown so exit
		} finally {
			watchDog.removeTimer(timer);
		}
	}
}
