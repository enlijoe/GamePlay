package com.jgr.game.vac.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.interfaces.LightOffPoller;
import com.jgr.game.vac.interfaces.PumpOnPoller;
import com.jgr.game.vac.interfaces.SealCompletePoller;
import com.jgr.game.vac.interfaces.SmartThings;

public class SleepMode {
	@Autowired private SmartThings smartThings;
	@Autowired private PumpOnPoller pumpOnPoller;
	@Autowired private SealCompletePoller sealCompletePoller;
	@Autowired private LightOffPoller lightOffPoller;
	@Autowired private WatchDog watchDog;
	@Autowired private DeviceNames deviceNames;

	private Logger logger = LoggerFactory.getLogger(SleepMode.class);
	private WatchDog.WatchTimer timer;
	
	class WatchDogTimerExpired implements Runnable {
		Thread myThread;
		
		WatchDogTimerExpired() {
			myThread = Thread.currentThread();
		}
		
		@Override
		public void run() {
			try {
				watchDog.setSaftyValveState(false);
				smartThings.setDeviceState(deviceNames.getPumpSwitch(), false);
				myThread.interrupt();
			} catch(Exception ex) {
				logger.error("Unable to turn off valve", ex);
			}
		}
	}
	
	
	public void runProgram() throws InterruptedException {
		timer = watchDog.addTimer(new WatchDogTimerExpired(), "Main Process");

		try {
			do {
				timer.checkin();
				smartThings.setDeviceState(deviceNames.getPumpSwitch(), false);
				timer.checkin();
				smartThings.setDeviceState(deviceNames.getPumpCheck(), false);
				timer.checkin();
				
				// the light must be off for things to work
				lightOffPoller.run(timer);
	
				smartThings.setDeviceState(deviceNames.getStatusLight(), true);
				timer.checkin();

				// wait for the pump to turn on
				pumpOnPoller.run(timer);
				if(smartThings.isOn(smartThings.getSwitchState(deviceNames.getStatusLight()))) {
					continue;
				}
				smartThings.setDeviceState(deviceNames.getPumpSwitch(), true);
				timer.checkin();
				logger.info("Pump running");
	
				smartThings.setDeviceState(deviceNames.getStatusLight(), false);
				timer.checkin();
				sealCompletePoller.run(timer);
				logger.info("Seal release restarting program.");
				
				// turn off pump now and restart
				smartThings.setDeviceState(deviceNames.getPumpSwitch(), false);
				timer.checkin();
				smartThings.setDeviceState(deviceNames.getPumpCheck(), false);
				timer.checkin();
				try {
					watchDog.setSaftyValveState(false);
				} catch(Exception ex) {
					logger.error("Unable to turn off valve", ex);
				}
			} while(true);
		} catch(InterruptedException iex) {
			// we have been shutdown so exit
		} finally {
			watchDog.removeTimer(timer);
		}
	}
}
