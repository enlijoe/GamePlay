package com.jgr.game.vac.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.interfaces.SmartThings;
import com.jgr.game.vac.interfaces.SystemTime;

public class SystemTimeImpl implements SystemTime {
	@Autowired private PropertyService propertyService;
	@Autowired private DeviceNames deviceNames;
	@Autowired private SmartThings smartThings;
	
	private Logger logger = LoggerFactory.getLogger(MainLine.class);

	@Override
	public long currentTime() {
		return System.currentTimeMillis();
	}
	
	@Override
	public void sleep(long time) throws InterruptedException {
		Thread.sleep(time);
	}

	@Override
	public void sleep(WatchDog.WatchTimer timer, long time) throws InterruptedException {
		long curTime = currentTime();
		long endTIme =  curTime + time;
		long timeLeft = endTIme-curTime; 
		
		while(timeLeft > 0) {
			timer.checkin();
			if(timeLeft > 5 * propertyService.getTimeMutiple()) {
				sleep(5 * propertyService.getTimeMutiple());
			} else {
				sleep(timeLeft);
			}
			curTime = currentTime();
			timeLeft = endTIme-curTime;  
		}
	}
	
	public boolean safeSleep(WatchDog.WatchTimer timer, long time) throws InterruptedException {
		long curTime = currentTime();
		long endTIme =  curTime+ time;
		long timeLeft = endTIme-curTime; 
		
		while(timeLeft > 0) {
			timer.checkin();
			if(timeLeft > 5000) {
				sleep(5000);
			} else {
				sleep(timeLeft);
			}
			curTime = currentTime();
			timeLeft = endTIme-curTime;  
			
			int value = smartThings.getSwitchState(deviceNames.getStatusLight());
			if(smartThings.isOn(value)) {
//				if(value >= 75) {
//					logger.info("Aborting");
//					throw new AbortException();
//				}
				logger.info("Restarting");
				return true;
			}
		}
		return false;
	}
	
}
