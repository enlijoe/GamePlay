package com.jgr.game.vac.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.LightOffPoller;
import com.jgr.game.vac.interfaces.OutputDevice;
import com.jgr.game.vac.interfaces.PumpOnPoller;
import com.jgr.game.vac.interfaces.SealCompletePoller;
import com.jgr.game.vac.stereotype.InjectDevice;

public class SleepMode {
	private Logger logger = LoggerFactory.getLogger(SleepMode.class);

	@Autowired private PumpOnPoller pumpOnPoller;
	@Autowired private SealCompletePoller sealCompletePoller;
	@Autowired private LightOffPoller lightOffPoller;
	@Autowired private WatchDog watchDog;

	@InjectDevice("${deviceUrl.status}") InputDevice statusDevice;
	@InjectDevice("${deviceUrl.pumpSwitch}") OutputDevice pumpSwitch;
	@InjectDevice("${deviceUrl.pumpCheck}") OutputDevice pumpCheck;
	@InjectDevice("${deviceUrl.statusLightCheck}") OutputDevice statusCheck;
	
	private WatchDog.WatchTimer timer;

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
